from tools import (
    search_recipes, search_by_nutrients, find_by_ingredients,
    get_recipe_information, find_similar_recipes, get_random_recipes,
    extract_recipe_from_url, search_ingredients, get_ingredient_information,
    create_recipe_card
)
from dotenv import load_dotenv

load_dotenv()

def test_spoonacular():
    print("--- Testing Spoonacular Tools ---")
    
    print("\n1. Searching for PASTA...")
    print(search_recipes.invoke({"query": "pasta", "number": 1}))
    
    print("\n2. Finding by Nutrients (High Protein)...")
    print(search_by_nutrients.invoke({"min_protein": 30, "number": 1}))
    
    print("\n3. Finding by Ingredients (Chicken, Rice)...")
    print(find_by_ingredients.invoke({"ingredients": "chicken, rice", "number": 1}))
    
    # We need a valid ID for details. Let's assume the searches above found one, 
    # but to be deterministic I will search again or just use a known one if possible.
    # Searching for 'avocado' to get an ID for ingredient test
    print("\n4. Searching Ingredient 'avocado'...")
    ing_result = search_ingredients.invoke({"query": "avocado", "number": 1})
    print(ing_result)
    
    # Extract ID from string if possible or just handle error visually
    # For now, just printing results proves the API call worked.

if __name__ == "__main__":
    test_spoonacular()
