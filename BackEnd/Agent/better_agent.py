import json
import os
import time
import requests
from typing import TypedDict, Annotated, Literal
from langgraph.graph import StateGraph, START, END
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import HumanMessage, SystemMessage
from pydantic import BaseModel, Field
from dotenv import load_dotenv
import google.generativeai as genai
from langchain_core.tools import tool

# --- Import Reusable Tools ---
from tools import (
    download_video_file,
    extract_video_id,
    get_youtube_transcript,
    get_youtube_description,
    get_ingredient_image_url,
    find_by_ingredients,
    extract_recipe_from_url
)

load_dotenv()

# --- Data Models ---

class Ingredient(BaseModel):
    name: str = Field(description="Name of the ingredient, e.g. 'onions'")
    amount: str = Field(description="Quantity and unit, e.g. '1 cup'")
    imageUrl: str | None = Field(default=None, description="URL of the ingredient image")

class Recipe(BaseModel):
    name: str = Field(description="Name of the recipe")
    steps: list[str] = Field(description="List of cooking steps")
    ingredients: list[Ingredient] = Field(description="List of ingredients")

# --- State Definition ---

class AgentState(TypedDict):
    url: str
    video_id: str
    description: str
    transcript: str
    text_content: str
    video_file_path: str
    image_file_path: str 
    
    # Internal state for passing data between nodes
    ingredients_detected: list[str] 
    dish_description: str
    raw_recipe_text: str # Intermediate text before formatting
    
    recipe: Recipe

# Initialize LLM
llm = ChatGoogleGenerativeAI(model="gemini-3-flash-preview")
recipe_llm = llm.with_structured_output(Recipe)

# --- Router Logic ---

def determine_source_type(state: AgentState):
    url = state["url"]
    if not url: return "website"
    lower_url = url.lower()
    
    # 1. YouTube
    if any(x in url for x in ["youtube.com", "youtu.be"]):
        return "youtube"
    
    # 2. Video File & Social Media (Instagram, TikTok, etc.)
    video_exts = ['.mp4', '.mov', '.avi', '.webm']
    social_domains = ['instagram.com', 'tiktok.com', 'facebook.com', 'x.com', 'twitter.com']
    
    if any(lower_url.endswith(ext) for ext in video_exts) or any(x in lower_url for x in social_domains):
        return "video_file"

    # 3. Image File
    image_exts = ['.jpg', '.jpeg', '.png', '.webp', '.heic']
    if any(lower_url.endswith(ext) for ext in image_exts):
        return "image_file"
        
    return "website"

# --- Input Processing Nodes ---

@tool
def node_process_image_file(state: AgentState):
    """Downloads an image file or passes local path."""
    url = state["url"]
    print(f"--- Downloading Image: {url} ---")
    
    # Check if this is already a local file path
    if os.path.exists(url):
         return {"image_file_path": os.path.abspath(url)}

    try:
        filename = "temp_agent_image.jpg"
        with requests.get(url, stream=True) as r:
            r.raise_for_status()
            with open(filename, 'wb') as f:
                for chunk in r.iter_content(chunk_size=8192):
                    f.write(chunk)
        return {"image_file_path": os.path.abspath(filename)}
    except Exception as e:
        print(f"Error downloading image: {e}")
        return {"image_file_path": None}

def node_process_video_file(state: AgentState):
    """Downloads a video file."""
    url = state["url"]
    print(f"--- Downloading Video: {url} ---")
    path = download_video_file.invoke({"url": url, "filename": "temp_agent_video.mp4"})
    if "Error" in path:
         return {"video_file_path": None}
    return {"video_file_path": path}

def node_scrape_website(state: AgentState):
    """Scrapes text from website."""
    url = state["url"]
    print(f"--- Scraping Website: {url} ---")
    
    # 1. invoke() returns the tool output. 
    # If you modified tools.py to return the dict, this works.
    response = extract_recipe_from_url.invoke(url)
    
    # Check for error string
    if isinstance(response, str) and "Error" in response:
         return {"text_content": response} # Fallback to text flow if error

    # 2. Parse DICTIONARY keys, not object attributes
    name = response.get('title', 'Unknown Recipe')
    
    # Create the Recipe object
    base_img_url = "https://img.spoonacular.com/ingredients_100x100/"
    
    ingredients = []
    for item in response.get('extendedIngredients', []):
        img_file = item.get('image', '')
        full_img_url = f"{base_img_url}{img_file}" if img_file else None
        
        ingredients.append(Ingredient(
            name=item.get('name', 'unknown'), 
            amount=f"{item.get('amount', 0)} {item.get('unit', '')}", 
            imageUrl=full_img_url
        ))

    steps = []
    analyzed = response.get('analyzedInstructions', [])
    if analyzed:
        for step in analyzed[0].get('steps', []):
            steps.append(step.get('step', ''))
            
    # Create the Recipe object
    recipe = Recipe(name=name, steps=steps, ingredients=ingredients)
    
    # Return the key 'recipe' to update the state
    return {"recipe": recipe} 

    
def node_get_youtube_data(state: AgentState):
    """Fetches YouTube data."""
    url = state["url"]
    print(f"--- Processing YouTube: {url} ---")
    video_id = extract_video_id.invoke(url)
    if not video_id: raise ValueError("Could not extract YouTube ID")
    
    transcript = get_youtube_transcript.invoke(video_id)
    description = get_youtube_description.invoke(video_id)
    
    if "No transcript detected" in transcript: transcript = ""
    
    return {"video_id": video_id, "transcript": transcript, "description": description}


# --- Extraction Logic Nodes ---

def node_extract_text_from_video(state: AgentState):
    """Extracts raw recipe text from video file using Gemini."""
    video_path = state.get("video_file_path")
    print(f"DEBUG: Video path from state: {video_path}")
    
    if not video_path: 
        print("DEBUG: No video path found.")
        return {}
    
    if not os.path.exists(video_path):
        print(f"DEBUG: File does not exist at path: {video_path}")
        return {}
    
    print("--- 🎥 Extracting Text from Video ---")
    
    api_key = os.getenv("GOOGLE_API_KEY")
    genai.configure(api_key=api_key)
    
    try:
        print(f"DEBUG: Uploading file {video_path}...")
        video_file = genai.upload_file(path=video_path)
        print(f"DEBUG: Uploaded. Name: {video_file.name}")
        
        while video_file.state.name == "PROCESSING":
            print(f"DEBUG: State is {video_file.state.name}, waiting...")
            time.sleep(2)
            video_file = genai.get_file(video_file.name)

        print(f"DEBUG: Final state: {video_file.state.name}")
        if video_file.state.name == "FAILED":
            print("DEBUG: Video processing failed on Google's side.")
            return {}

        print("DEBUG: Generating content...")
        # Keeping user's requested model
        model = genai.GenerativeModel('gemini-3-flash-preview') 
        prompt = """
        You are an expert chef. Watch this video and write down the full recipe.
        """
        result = model.generate_content([video_file, prompt])
        print(f"DEBUG: Generation finished. Text length: {len(result.text) if result.text else 0}")
        print(f"DEBUG: Preview: {result.text[:100] if result.text else 'None'}")
        
        genai.delete_file(video_file.name)
        if os.path.exists(video_path): os.remove(video_path)
        
        return {"raw_recipe_text": result.text}
    except Exception as e:
        print(f"Video extraction error: {e}")
        import traceback
        traceback.print_exc()
        return {}

def node_format_recipe(state: AgentState):
    """Formats raw text or transcript into a structured Recipe model."""
    # Prioritize raw_recipe_text, then transcript/description
    text_to_format = state.get("raw_recipe_text") or state.get("transcript") or state.get("description")
    
    print(f"DEBUG: text_to_format present? {bool(text_to_format)}")
    if text_to_format:
        print(f"DEBUG: text_to_format length: {len(text_to_format)}")
    
    if not text_to_format: return {}
    
    print("--- 📝 Formatting Recipe ---")
    try:
        # We wrap in messages to ensure the system instruction (from declaration) applies effectively
        response = recipe_llm.invoke([
             SystemMessage(content="You are a data extractor. Convert the following recipe text into the required JSON schema."),
             HumanMessage(content=text_to_format)
        ])
        return {"recipe": response}
    except Exception as e:
        print(f"Formatting error: {e}")
        import traceback
        traceback.print_exc()
        return {}


def node_analyze_image_type(state: AgentState):
    """Decides if image is 'ingredients' or 'dish'."""
    image_path = state.get("image_file_path")
    if not image_path: return {}
    
    print("--- 🖼️ Analyzing Image Type ---")
    
    api_key = os.getenv("GOOGLE_API_KEY")
    genai.configure(api_key=api_key)
    
    try:
        image_file = genai.upload_file(path=image_path)
        model = genai.GenerativeModel('gemini-2.0-flash-exp')
        
        prompt = """
        Analyze this image. 
        1. Is this a picture of raw ingredients (e.g. items on a counter/fridge)?
        2. Or is it a finished, cooked dish?
        
        Return JSON: 
        {
          "type": "ingredients" or "dish", 
          "content": "list of ingredients comma separated" OR "description of the dish"
        }
        """
        result = model.generate_content([image_file, prompt])
        
        genai.delete_file(image_file.name)
        
        text_clean = result.text.replace("```json", "").replace("```", "")
        analysis = json.loads(text_clean)
        
        if analysis["type"] == "ingredients":
            raw_content = analysis["content"]
            # Basic Python split logic
            items = raw_content.split(',')
            cleaned_items = []
            for item in items:
                cleaned_items.append(item.strip())
                
            return {
                "ingredients_detected": cleaned_items,
                "dish_description": ""
            }
        else:
             return {
                "ingredients_detected": [],
                "dish_description": analysis["content"]
            }
            
    except Exception as e:
        print(f"Image analysis error: {e}")
        return {"dish_description": "Unknown dish"} # Default fallback


def node_recipe_from_ingredients(state: AgentState):
    """Logic for Ingredients -> Search -> Recipe."""
    ingredients = state.get("ingredients_detected", [])
    if not ingredients: return {}
    
    print(f"--- 🥕 Processing Ingredients: {ingredients} ---")
    
    ing_str = ", ".join(ingredients)
    
    # 1. Search Spoonacular
    search_results = find_by_ingredients.invoke(ing_str)
    
    # 2. Prepare Context for Synthesis
    if search_results and "No recipes found" not in search_results:
        print(" -> Found matching recipes.")
        context = f"Ingredients available: {ing_str}.\n\nPotential Recipes Found:\n{search_results}"
        prompt = "Using the available ingredients and valid matches, create a full detailed recipe for the best match."
    else:
        print(" -> No matches found. Generating creative recipe.")
        context = f"Ingredients available: {ing_str}."
        prompt = "Create a creative and delicious recipe using ONLY these ingredients (and basic pantry items)."
        
    # Generate text
    result = llm.invoke([
        SystemMessage(content="You are an expert chef."),
        HumanMessage(content=f"{context}\n\n{prompt}")
    ])
    
    return {"raw_recipe_text": result.content}


def node_recipe_from_dish_image(state: AgentState):
    """Logic for Dish Image Description -> Recipe."""
    description = state.get("dish_description")
    if not description: return {}
    
    print(f"--- 🍲 Processing Dish Description: {description} ---")
    
    prompt = f"The user provided an image of: {description}. Provide a complete, authentic recipe for this dish."
    
    result = llm.invoke([
        SystemMessage(content="You are an expert chef."),
        HumanMessage(content=prompt)
    ])
    
    return {"raw_recipe_text": result.content}


def node_extract_from_text(state: AgentState):
    """Standard extraction for text/transcript."""
    content = ""
    if state.get("text_content"):
        content = f"Website: {state['text_content']}"
    elif state.get("transcript"):
        content = f"Transcript: {state['transcript']} \n Descr: {state['description']}"
    
    if not content: return {}
    
    print("--- 📄 Processing Text Content ---")
    
    result = llm.invoke([
        SystemMessage(content="You are an expert chef."),
        HumanMessage(content=f"Based on: {content}. Create a detailed recipe.")
    ])
    return {"raw_recipe_text": result.content}


def node_format_recipe(state: AgentState):
    """Final formatting to strict JSON."""
    raw_text = state.get("raw_recipe_text")
    if not raw_text: 
        print("Error: No recipe text generated.")
        return {"recipe": None}
    
    print("--- ✨ Formatting Recipe ---")
    
    try:
        response = recipe_llm.invoke([
            SystemMessage(content="Extract the recipe data into the specific JSON format required."),
            HumanMessage(content=raw_text)
        ])
        return {"recipe": response}
    except Exception as e:
        print(f"Formatting error: {e}")
        return {}
        
def enrich_ingredients(state: AgentState):
    """Enriches ingredients with images."""
    recipe = state.get('recipe')
    if not recipe: return {}
    
    print("--- 🎨 Enriching Ingredients ---")
    updated = []
    for ing in recipe.ingredients:
        # Check if we already have a valid image (and it's not a generic placeholder/filename)
        # Spoonacular sometimes returns just filenames like "apple.jpg" which need base path, 
        # but if we have a full http link from elsewhere, we keep it.
        
        current_img = ing.imageUrl
        
        # If no image, or if it looks like a relative filename (no http), fetch a new one
        if not current_img or "http" not in current_img:
             print(f" -> Fetching image for: {ing.name}")
             url = get_ingredient_image_url.invoke(ing.name)
             updated.append(Ingredient(name=ing.name, amount=ing.amount, imageUrl=url))
        else:
             # Keep existing
             updated.append(ing)
        
    return {"recipe": recipe.model_copy(update={"ingredients": updated})}


# --- Graph Construction ---

graph = StateGraph(AgentState)

# 1. Processing Nodes
graph.add_node("process_video_file", node_process_video_file)
graph.add_node("process_image_file", node_process_image_file)
graph.add_node("scrape_website", node_scrape_website)
graph.add_node("get_youtube_data", node_get_youtube_data)

# 2. Logic Nodes
graph.add_node("extract_text_from_video", node_extract_text_from_video)
graph.add_node("analyze_image_type", node_analyze_image_type)
graph.add_node("recipe_from_ingredients", node_recipe_from_ingredients)
graph.add_node("recipe_from_dish_image", node_recipe_from_dish_image)
graph.add_node("extract_from_text", node_extract_from_text)

# 3. Formatting Nodes
graph.add_node("format_recipe", node_format_recipe)
graph.add_node("enrich_ingredients", enrich_ingredients)

# --- Edges ---

def route_input(state):
    return determine_source_type(state)

def route_image_logic(state):
    if state.get("ingredients_detected"):
        return "ingredients"
    return "dish"

def route_scrape_logic(state):
    if state.get("recipe"):
        return "formatted"
    return "raw_text"

# Start -> Process Input
graph.add_conditional_edges(START, route_input, {
    "youtube": "get_youtube_data",
    "video_file": "process_video_file",
    "image_file": "process_image_file",
    "website": "scrape_website"
})

# Process Input -> Logic
graph.add_edge("get_youtube_data", "extract_from_text")

graph.add_conditional_edges("scrape_website", route_scrape_logic, {
    "formatted": "enrich_ingredients",
    "raw_text": "extract_from_text"
})

graph.add_edge("process_video_file", "extract_text_from_video")
graph.add_edge("process_image_file", "analyze_image_type")

# Image Logic Branching
graph.add_conditional_edges("analyze_image_type", route_image_logic, {
    "ingredients": "recipe_from_ingredients",
    "dish": "recipe_from_dish_image"
})

# Convergence to Format
graph.add_edge("extract_from_text", "format_recipe")
graph.add_edge("extract_text_from_video", "format_recipe")
graph.add_edge("recipe_from_ingredients", "format_recipe")
graph.add_edge("recipe_from_dish_image", "format_recipe")

# Final Polish
graph.add_edge("format_recipe", "enrich_ingredients")
graph.add_edge("enrich_ingredients", END)

workflow = graph.compile()

if __name__ == "__main__":
    print("\n=== PlateIt Recipe Agent (Modular) ===")
    while True:
        url = input("\nEnter URL (or 'q'): ").strip()
        if url == 'q': break
        try:
            res = workflow.invoke({"url": url})
            if res.get('recipe'):
                r = res['recipe']
                print(f"\nSuccessfully extracted: {r.name}")
                print(r)
            else:
                print("Failed.")
        except Exception as e:
            print(f"Error: {e}")
