import os
import requests
from langchain_core.tools import tool

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
    
    title = data.get("title")
    ingredients = [f"- {i['original']}" for i in data.get("extendedIngredients", [])]
    instructions = data.get("instructions")
    
    return f"Title: {title}\nIngredients:\n{chr(10).join(ingredients)}\nInstructions:\n{instructions}"

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
