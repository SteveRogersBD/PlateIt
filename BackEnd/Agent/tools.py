import os
import requests
from langchain_core.tools import tool
from bs4 import BeautifulSoup
from urllib.parse import urlparse, parse_qs
import time
import google.generativeai as genai

# --- HARDCODED GEMINI KEY ---
os.environ["GOOGLE_API_KEY"] = "AIzaSyCGBOPpSv6YWuR1xWyMIEMI6OqKGTzUmYM"
os.environ["GEMINI_API_KEY"] = "AIzaSyCGBOPpSv6YWuR1xWyMIEMI6OqKGTzUmYM"
# ----------------------------

# --- Google / SerpAPI Tools ---

@tool
def google_search(query: str):
    """
    Performs a general web search using Google (via SerpApi).
    Useful for finding cooking tips, food history, or general questions not covered by Spoonacular.
    """
    api_key = os.getenv("SERP_API_KEY")
    if not api_key:
        return "Error: SERP_API_KEY not configured."
    
    url = "https://serpapi.com/search"
    params = {
        "engine": "google",
        "q": query,
        "api_key": api_key,
        "num": 5
    }
    
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        
        results = []
        if "organic_results" in data:
            for item in data["organic_results"]:
                title = item.get('title', 'No Title')
                link = item.get('link', 'No Link')
                snippet = item.get('snippet', 'No Snippet')
                results.append(f"Title: {title}\nLink: {link}\nSnippet: {snippet}")
        
        if not results:
            return "No good search results found."
            
        return "\n\n".join(results)
    except Exception as e:
        return f"Error performing search: {e}"

@tool
def google_image_search(query: str):
    """
    Finds an image URL for a specific food item or dish using Google Images.
    """
    api_key = os.getenv("SERP_API_KEY")
    if not api_key:
        return "Error: SERP_API_KEY not configured."

    url = "https://serpapi.com/search"
    params = {
        "engine": "google_images",
        "q": query,
        "api_key": api_key,
        "num": 1
    }
    
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        
        if "images_results" in data and len(data["images_results"]) > 0:
            # Return the original image URL
            return data["images_results"][0].get("original")
        else:
            return "No image found."
    except Exception as e:
        # Graceful failure
        return f"Error searching images: {e}"

# --- Spoonacular Tools ---

def _spoonacular_get(endpoint: str, params: dict):
    """Helper to call Spoonacular API"""
    api_key = os.getenv("SPOONACULAR_API_KEY")
    if not api_key:
        return {"error": "SPOONACULAR_API_KEY not configured."}
    
    base_url = "https://api.spoonacular.com"
    params["apiKey"] = api_key
    
    try:
        response = requests.get(f"{base_url}{endpoint}", params=params)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        return {"error": str(e)}

@tool
def search_recipes(query: str, cuisine: str = None, diet: str = None, number: int = 5):
    """
    Search for recipes by query, cuisine, and diet.
    Applies logic to find the best matches.
    """
    params = {
        "query": query,
        "number": number,
        "addRecipeInformation": True,
        "instructionsRequired": True
    }
    if cuisine:
        params["cuisine"] = cuisine
    if diet:
        params["diet"] = diet
        
    data = _spoonacular_get("/recipes/complexSearch", params)
    if "error" in data: return data["error"]
    
    results = []
    for r in data.get("results", []):
        results.append(f"ID: {r['id']} | Title: {r['title']} | Time: {r.get('readyInMinutes')}m")
        
    return "\n".join(results) if results else "No recipes found."

@tool
def search_by_nutrients(min_protein: int = 0, max_calories: int = 1000, number: int = 5):
    """
    Find recipes with specific nutrient requirements.
    """
    params = {
        "minProtein": min_protein,
        "maxCalories": max_calories,
        "number": number,
        "random": True 
    }
    data = _spoonacular_get("/recipes/findByNutrients", params)
    if "error" in data: return data["error"]
    
    results = []
    for r in data:
        results.append(f"ID: {r['id']} | Title: {r['title']} | Cal: {r['calories']} | Protein: {r['protein']}")
    return "\n".join(results) if results else "No recipes found."

@tool
def find_by_ingredients(ingredients: str, number: int = 5):
    """
    Find recipes that use the given ingredients.
    ingredients: Comma-separated list (e.g. "apples, flour, sugar")
    """
    params = {
        "ingredients": ingredients,
        "number": number,
        "ranking": 2, # Minimize missing ingredients
        "ignorePantry": True
    }
    data = _spoonacular_get("/recipes/findByIngredients", params)
    if "error" in data: return data["error"]
    
    results = []
    for r in data:
        missing = [i["name"] for i in r.get("missedIngredients", [])]
        results.append(f"ID: {r['id']} | Title: {r['title']} | Missing: {', '.join(missing)}")
    return "\n".join(results) if results else "No recipes found."

@tool
def get_recipe_information(recipe_id: int):
    """
    Get full details for a specific recipe ID (instructions, ingredients).
    """
    data = _spoonacular_get(f"/recipes/{recipe_id}/information", {"includeNutrition": False})
    if "error" in data: return data["error"]
    
    title = data.get("title")
    servings = data.get("servings")
    ready_in = data.get("readyInMinutes")
    url = data.get("sourceUrl")
    
    ingredients = [f"- {i['original']}" for i in data.get("extendedIngredients", [])]
    
    instructions = data.get("instructions")
    if not instructions and data.get("analyzedInstructions"):
        steps = []
        for step in data["analyzedInstructions"][0]["steps"]:
            steps.append(f"{step['number']}. {step['step']}")
        instructions = "\n".join(steps)
        
    return f"""Title: {title}
Servings: {servings} | Time: {ready_in}m
Source: {url}
Ingredients:
{chr(10).join(ingredients)}
Instructions:
{instructions}"""

@tool
def find_similar_recipes(recipe_id: int, number: int = 3):
    """Find recipes similar to the given ID."""
    data = _spoonacular_get(f"/recipes/{recipe_id}/similar", {"number": number})
    if "error" in data: return data["error"]
    
    results = []
    for r in data:
        results.append(f"ID: {r['id']} | Title: {r['title']}")
    return "\n".join(results) if results else "No similar recipes found."

@tool
def get_random_recipes(tags: str = None, number: int = 3):
    """
    Get random recipes.
    tags: comma separated types (e.g. "vegetarian, dessert")
    """
    params = {"number": number}
    if tags: params["tags"] = tags
    
    data = _spoonacular_get("/recipes/random", params)
    if "error" in data: return data["error"]
    
    results = []
    for r in data.get("recipes", []):
        results.append(f"ID: {r['id']} | Title: {r['title']}")
    return "\n".join(results)

@tool
def extract_recipe_from_url(url: str):
    """Extract recipe data from a website URL."""
    data = _spoonacular_get("/recipes/extract", {"url": url})
    if "error" in data: return data["error"]
    
    return data

@tool
def search_ingredients(query: str, number: int = 5):
    """Search for an ingredient to get its ID."""
    data = _spoonacular_get("/food/ingredients/search", {"query": query, "number": number})
    if "error" in data: return data["error"]
    
    results = []
    for r in data.get("results", []):
        results.append(f"ID: {r['id']} | Name: {r['name']}")
    return "\n".join(results)

@tool
def get_ingredient_information(ingredient_id: int):
    """Get nutritional info for an ingredient ID."""
    data = _spoonacular_get(f"/food/ingredients/{ingredient_id}/information", {"amount": 100, "unit": "grams"})
    if "error" in data: return data["error"]
    
    name = data.get("name")
    nutrition = data.get("nutrition", {}).get("nutrients", [])
    
    key_nutrients = []
    for n in nutrition:
        if n["name"] in ["Calories", "Fat", "Protein", "Carbohydrates"]:
            key_nutrients.append(f"{n['name']}: {n['amount']}{n['unit']}")
            
    return f"Ingredient: {name}\nNutrition (per 100g):\n" + "\n".join(key_nutrients)

@tool
def create_recipe_card(recipe_id: int):
    """
    Get a URL to an image card for the recipe.
    Do NOT call this unless the user specifically asks for a visual card.
    """
    data = _spoonacular_get(f"/recipes/{recipe_id}/card", {})
    if "error" in data: return data["error"]
    
    return data.get("url", "No card URL returned.")

# --- Content Extraction Tools ---

@tool
def scrape_website_text(url: str):
    """
    Scrapes the text content from a given website URL.
    Useful for extracting recipes or articles from blogs/websites.
    """
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Remove script and style elements
        for script in soup(["script", "style", "nav", "footer"]):
            script.decompose()
            
        text = soup.get_text()
        
        # Clean up text
        lines = (line.strip() for line in text.splitlines())
        chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
        text = '\n'.join(chunk for chunk in chunks if chunk)
        
        return text
        
    except Exception as e:
        return f"Error scraping website: {e}"

@tool
def download_video_file(url: str, filename: str = "temp_video_recipe.mp4"):
    """
    Downloads a video file from a URL using yt-dlp (supports YouTube, Instagram, TikTok, etc.)
    or direct HTTP download.
    Returns the absolute path of the downloaded file.
    """
    import yt_dlp
    
    # Absolute path for the output
    abs_filename = os.path.abspath(filename)
    
    # 1. Try yt-dlp first (handles most social media + direct links often)
    ydl_opts = {
        'outtmpl': abs_filename, # Force filename
        'format': 'best[ext=mp4]/best', # Prefer mp4
        'quiet': True,
        'overwrites': True,
    }
    
    print(f" -> Attempting download with yt-dlp: {url}")
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])
        return abs_filename
    except Exception as e:
        print(f" -> yt-dlp failed: {e}. Falling back to requests.")
        
    # 2. Fallback to direct request (for simple file servers)
    try:
        with requests.get(url, stream=True) as r:
            r.raise_for_status()
            with open(filename, 'wb') as f:
                for chunk in r.iter_content(chunk_size=8192): 
                    f.write(chunk)
        return abs_filename
    except Exception as e:
        return f"Error downloading video: {e}"

# --- YouTube Tools ---

@tool
def extract_video_id(url: str):
    """
    Extracts the YouTube Video ID from a given URL.
    Supports standard watch URLs, Shorts, and Share links.
    """
    if "watch" in url:
        parsed_url = urlparse(url)
        if 'v' in parse_qs(parsed_url.query):
            return parse_qs(parsed_url.query)['v'][0]
    elif "shorts" in url:
        return url.split("shorts/")[1].split("?")[0]
    elif "youtu.be" in url:
        return url.split("/")[-1].split("?")[0]
    
    return ""

@tool
def get_youtube_transcript(video_id: str):
    """
    Fetches the transcript of a YouTube video using SerpApi.
    """
    api_key = os.getenv("SERP_API_KEY")
    if not api_key:
        return "Error: SERP_API_KEY not set."
        
    url = "https://serpapi.com/search"
    params = {
        "engine": "youtube_video_transcript",
        "v": video_id,
        "api_key": api_key,
    }
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        if "transcript" in data:
            transcripts = [t["snippet"] for t in data["transcript"]]
            return "\n".join(transcripts)
        else:
            return "No transcript found."
    except Exception as e:
        return f"Error fetching transcript: {e}"

@tool
def get_youtube_description(video_id: str):
    """
    Fetches the description of a YouTube video using SerpApi.
    """
    api_key = os.getenv("SERP_API_KEY")
    if not api_key:
        return "Error: SERP_API_KEY not set."

    url = "https://serpapi.com/search"
    params = {
        "engine": "youtube_video",
        "v": video_id,
        "api_key": api_key,
    }
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        return data.get("description", {}).get("content", "No description found.")
    except Exception as e:
        return f"Error fetching description: {e}"

# --- Helper Tools ---

@tool
def get_ingredient_image_url(ingredient_name: str):
    """
    Fetches the image URL for a given ingredient name using Spoonacular.
    """
    api_key = os.getenv("SPOONACULAR_API_KEY")
    if not api_key:
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
            # Spoonacular base URL for ingredients
            image_server_base = "https://img.spoonacular.com/ingredients_100x100/"
            return f"{image_server_base}{data['results'][0]['image']}"
    except Exception:
        pass
    
    return None

def search_youtube_videos(query: str, limit: int = 5):
    """
    Searches YouTube via SerpAPI and returns a list of video objects.
    Reuses the SERP_API_KEY from environment variables.
    """
    api_key = os.getenv("SERP_API_KEY")
    if not api_key:
        print("Error: SERP_API_KEY not configured.")
        return []

    url = "https://serpapi.com/search"
    params = {
        "engine": "youtube",
        "search_query": query,
        "api_key": api_key,
        "num": limit 
    }
    
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        
        videos = []
        if "video_results" in data:
            for item in data["video_results"]:
                videos.append({
                    "title": item.get("title"),
                    "link": item.get("link"),
                    "thumbnail": item.get("thumbnail", {}).get("static"),
                    "channel": item.get("channel", {}).get("name"),
                    "views": item.get("views"),
                    "length": item.get("length")
                })
        return videos
    except Exception as e:
        print(f"Error searching YouTube for '{query}': {e}")
        return []

def search_google_blogs(query: str, limit: int = 5):
    """
    Searches Google via SerpAPI for blog posts/articles.
    Returns a list of blog objects.
    """
    api_key = os.getenv("SERP_API_KEY")
    if not api_key:
        print("Error: SERP_API_KEY not configured.")
        return []

    url = "https://serpapi.com/search"
    params = {
        "engine": "google",
        "q": query,
        "api_key": api_key,
        "num": limit
    }
    
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        
        blogs = []
        if "organic_results" in data:
            for item in data["organic_results"]:
                
                # Check for "recipe" intent in title or snippet to filter out generic articles
                title = item.get("title", "").lower()
                snippet = item.get("snippet", "").lower()
                if "recipe" not in title and "how to cook" not in title and "recipe" not in snippet:
                    continue

                # robust image extraction
                thumbnail = item.get("thumbnail") 
                
                # Check pagemap for cse_image
                if not thumbnail and "pagemap" in item:
                    cse_images = item["pagemap"].get("cse_image")
                    if cse_images and isinstance(cse_images, list) and len(cse_images) > 0:
                        thumbnail = cse_images[0].get("src")
                    
                    # Check pagemap for cse_thumbnail
                    if not thumbnail:
                        cse_thumbs = item["pagemap"].get("cse_thumbnail")
                        if cse_thumbs and isinstance(cse_thumbs, list) and len(cse_thumbs) > 0:
                            thumbnail = cse_thumbs[0].get("src")

                # Check rich_snippet (common in SerpApi)
                if not thumbnail and "rich_snippet" in item:
                    top = item["rich_snippet"].get("top", {})
                    if "detected_extensions" in top:
                        thumbnail = top["detected_extensions"].get("thumbnail")
                    if not thumbnail and "extensions" in top:
                         # Sometimes it's a list
                         exts = top.get("extensions", [])
                         if isinstance(exts, list) and len(exts) > 0 and isinstance(exts[0], str):
                             # This is usually text, ignore
                             pass
                
                blogs.append({
                    "title": item.get("title"),
                    "link": item.get("link"),
                    "snippet": item.get("snippet"),
                    "source": item.get("source"),
                    "thumbnail": thumbnail
                })
        return blogs
    except Exception as e:
        print(f"Error searching Google for '{query}': {e}")
        return []

