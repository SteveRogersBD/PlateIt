import json
import os
from langgraph.constants import START
from langgraph.graph import StateGraph, END
from typing import TypedDict, Annotated, final
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import HumanMessage, AIMessage, BaseMessage, SystemMessage
from langgraph.graph.message import add_messages
from pydantic import BaseModel, Field
import requests
from dotenv import load_dotenv

load_dotenv()

class Ingredient(BaseModel):
    name: str = Field(description="Name of the ingredient, e.g. 'onions'")
    amount: str = Field(description="Quantity and unit, e.g. '1 cup'")
    imageUrl: str | None = Field(default=None, description="URL of the ingredient image")

class Recipe(BaseModel):
    name: str = Field("Name of the recipe")
    steps: list[str] = Field("List of steps that must be taken from the recipe")
    ingredients: list[Ingredient] = Field("List of ingredients extracted from the recipe")

class Video(TypedDict):
    title: str
    video_id: str
    video_url: str
    description: str
    transcript: list[str]
    recipe: Recipe


llm = ChatGoogleGenerativeAI(model="gemini-3-flash-preview")

from urllib.parse import urlparse, parse_qs

def get_video_id(state: Video):
    url = state["video_url"]
    if "watch" in url:
        parsed_url = urlparse(url)
        video_id = parse_qs(parsed_url.query)['v'][0]
        return {"video_id": video_id}
    elif "shorts" in url:
        video_id = url.split("shorts/")[1].split("?")[0]
        return {"video_id": video_id}
    elif "youtu.be" in url:
        video_id = url.split("/")[-1].split("?")[0]
        return {"video_id": video_id}
    return {"video_id": ""}

# ... (get_transcript and get_description remain unchanged, I will skip them for brevity in this replace block if possible, but replace_file_content replaces contiguous blocks. I need to be careful.
# The user's file is small enough (~260 lines). I will target the Models and the enrich function.)

# ... skipping get_transcript/description/recipe_llm setup ... 
# Actually, I need to redefine Recipe so I have to touch the top of the file.
# And I need to update enrich_ingredients so I have to touch the bottom.
# This might require two edits or one large one. 
# Attempting to start from line 12 (Model definitions) down to line 206 (enrich_ingredients).
# This is too risky for one block if I don't paste the middle.
# I will make 2 calls. Call 1: Update Models. Call 2: Update enrich_ingredients.

# CALL 1: Models



llm = ChatGoogleGenerativeAI(model="gemini-3-flash-preview")



from urllib.parse import urlparse, parse_qs

def get_video_id(state: Video):
    url = state["video_url"]
    # Case 1: Standard URL (e.g., youtube.com/watch?v=VIDEO_ID)
    if "watch" in url:
        parsed_url = urlparse(url)
        # Grab the 'v' parameter from the query string
        video_id = parse_qs(parsed_url.query)['v'][0]
        return {"video_id": video_id}

    # Case 2: Shorts (e.g., youtube.com/shorts/VIDEO_ID)
    elif "shorts" in url:
        # Split the text by "shorts/" and take the part after it
        video_id = url.split("shorts/")[1].split("?")[0] # .split("?")[0] removes any extra params
        return {"video_id": video_id}

    # Case 3: Share links (e.g., youtu.be/VIDEO_ID)
    elif "youtu.be" in url:
        # Split by "/" and take the very last piece
        video_id = url.split("/")[-1].split("?")[0]
        return {"video_id": video_id}

    return {"video_id": ""}

# first node: get the transcript
def get_transcript(state: Video):
    # The base endpoint without parameters
    url = "https://serpapi.com/search"

    # We pass the parameters as a dictionary.
    # This automatically handles adding the '?' and '&' characters for you.
    params = {
        "engine": "youtube_video_transcript",
        "v": state["video_id"],
        "api_key": "5430f234ebc964dd1ca39b4c98572751c4ddadf23fb5157fce32e354e7aa9811",
    }

    try:
        response = requests.get(url, params=params)

        # Check for errors
        response.raise_for_status()

        # Parse the JSON
        data = response.json()

        if "transcript" in data:
            transcript_list = data["transcript"]
            transcripts = []
            for transcript in transcript_list:
                transcripts.append(transcript["snippet"])
            return {"transcript": transcripts}
        else:
            return {"transcript": []}

    except requests.exceptions.HTTPError as e:
        print(f"HTTP error: {e}")
    except Exception as e:
        print(f"Error: {e}")

def get_description(state: Video):
    # The base endpoint without parameters
    url = "https://serpapi.com/search"

    # We pass the parameters as a dictionary.
    # This automatically handles adding the '?' and '&' characters for you.
    params = {
        "engine": "youtube_video",
        "v": state["video_id"],
        "api_key": "5430f234ebc964dd1ca39b4c98572751c4ddadf23fb5157fce32e354e7aa9811",
    }

    try:
        response = requests.get(url, params=params)

        # Check for errors
        response.raise_for_status()

        # Parse the JSON
        data = response.json()

        desc = data["description"]["content"]

        return {"description": desc}

    except requests.exceptions.HTTPError as e:
        print(f"HTTP error: {e}")
    except Exception as e:
        print(f"Error: {e}")


recipe_llm = llm.with_structured_output(Recipe)
def extract_recipe(state: Video):
    prompt = [
        SystemMessage(content="You are a cooking expert."),
        HumanMessage(content=f"""
                Based on the video transcript: \n{state['transcript']}
                \n and the description: \n{state['description']}

                Create a detailed recipe for a classic dish. Include:
                - Full recipe description
                - Clear cooking instructions
                - List of ingredients (Name and Amount separated)
                - Step-by-step instructions
                - Output format: Provide the recipe content in Markdown format.

                Please provide a comprehensive recipe.
                """)
    ]
    response = recipe_llm.invoke(prompt)
    if not response:
        raise ValueError(f"Failed to extract recipe from URL: "
                         f"{state['video_url']}. Ending execution.")
    return {
        'recipe': response
    }

# Helper function to get image from Spoonacular
def get_ingredient_image(ingredient_name):
    api_key = os.getenv("SPOONACULAR_API_KEY")
    if not api_key:
        print("Error: SPOONACULAR_API_KEY is not set.")
        return None
    
    url = "https://api.spoonacular.com/food/ingredients/search"
    params = {
        "query": ingredient_name,
        "apiKey": api_key,
        "number": 1
    }
    
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        if data.get("results"):
            # Spoonacular base URL for ingredients is usually:
            # https://img.spoonacular.com/ingredients_{SIZE}/{image_filename}
            image_server_base = "https://img.spoonacular.com/ingredients_100x100/"
            return f"{image_server_base}{data['results'][0]['image']}"
    except Exception as e:
        print(f"Error fetching image for {ingredient_name}: {e}")
    
    return None

def enrich_ingredients(state: Video):
    recipe = state['recipe']
    
    print("--- Enriching Ingredients ---")
    
    # We will modify the ingredients in-place or create a new list of valid Ingredient objects
    # to ensure Pydantic validation passes nicely.
    updated_ingredients = []
    
    for ingredient in recipe.ingredients:
        # ingredient is now an Ingredient object, not a string
        image_url = get_ingredient_image(ingredient.name)
        
        # Update the image field
        # Create a new Ingredient object to be safe/clean
        updated_ingredient = Ingredient(
            name=ingredient.name,
            amount=ingredient.amount,
            imageUrl=image_url if image_url else None
        )
        updated_ingredients.append(updated_ingredient)
    
    # Update the existing recipe object with enriched ingredients
    new_recipe = recipe.model_copy(update={"ingredients": updated_ingredients})
    
    return {"recipe": new_recipe}

def pass_node(state: Video):
    return {}

graph = StateGraph(Video)

graph.add_node("get_video_id",get_video_id)
graph.add_node("get_transcript",get_transcript)
graph.add_node("get_description",get_description)
graph.add_node("merge_results", pass_node)
graph.add_node("extract_recipe",extract_recipe)
graph.add_node("enrich_ingredients", enrich_ingredients)

graph.add_edge(START,"get_video_id")
graph.add_edge("get_video_id","get_transcript")
graph.add_edge("get_video_id","get_description")
graph.add_edge("get_transcript","merge_results")
graph.add_edge("get_description","merge_results")
graph.add_edge("merge_results","extract_recipe")
graph.add_edge("extract_recipe", "enrich_ingredients")
graph.add_edge("enrich_ingredients",END)

workflow = graph.compile()

if __name__ == "__main__":


    attempts = 0
    max_retries = 3
    final_state = None

    while attempts < max_retries:
        url = input("Enter the URL: ")
        initial_state = {"video_url": url}
        print(f"--- Attempt {attempts + 1} ---")
        try:
            # Invoke the graph
            final_state = workflow.invoke(initial_state)

            # Check if we got valid results
            recipe = final_state.get('recipe')

            # Condition: Check if 'recipe' exists and has non-empty steps/ingredients
            if recipe and recipe.steps and recipe.ingredients:
                print("Success! Recipe extracted.")
                break  # Exit the loop on success
            else:
                print("Partial result: Missing steps or ingredients. Retrying...")

        except Exception as e:
            print(f"Error occurred: {e}")

        attempts += 1

    if final_state and final_state.get('recipe'):
        print("\n--- Final State ---")
        print(json.dumps(final_state.get('recipe', {}).model_dump(), indent=2))
    else:
        print("Failed to extract complete recipe after all attempts.")
