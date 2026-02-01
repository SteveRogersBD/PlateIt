# PlateIt Agent Tools

This document lists all the tools available to the PlateIt AI Chef.

## 1. Research & Knowledge (Google / SerpApi)

| Tool Name | Engine / Type | Function |
| :--- | :--- | :--- |
| **`google_search`** | `google` | Performs a general web search for cooking questions, history, or non-recipe inquiries (e.g., "Best wine for steak"). Returns text snippets. |
| **`google_image_search`** | `google_images` | Finds an image URL for a food item or dish to provide visual context in the chat. |

## 2. Recipe Discovery (Spoonacular)

| Tool Name | Endpoint | Function |
| :--- | :--- | :--- |
| **`search_recipes`** | `/recipes/complexSearch` | The main search engine. Supports queries (text), cuisine, diet (keto, vegan), and tolerances. Returns ID, Title, Image. |
| **`search_by_nutrients`** | `/recipes/findByNutrients` | Finds recipes purely based on macro targets (min/max calories, protein, carbs, fat). |
| **`find_by_ingredients`** | `/recipes/findByIngredients` | Takes a list of ingredients (e.g., "chicken, rice") and returns recipes that use them, sorted by least missing ingredients. |
| **`find_similar_recipes`** | `/recipes/{id}/similar` | Finds recipes visually or conceptually similar to a given recipe ID. |
| **`get_random_recipes`** | `/recipes/random` | Returns random recipes. Can be constrained by tags (e.g., "dessert", "vegetarian"). |

## 3. Recipe Details (Spoonacular)

| Tool Name | Endpoint | Function |
| :--- | :--- | :--- |
| **`get_recipe_information`** | `/recipes/{id}/information` | The "Do It" tool. Fetches the full instructions, detailed ingredient amounts, and ready time for a specific Recipe ID. |
| **`extract_recipe_from_url`** | `/recipes/extract` | Analyzes a given URL (blog, website) and attempts to structure it into ingredients and instructions. |
| **`create_recipe_card`** | `/recipes/{id}/card` | Generates a shareable image card (JPEG) containing the recipe title and summary. |

## 4. Ingredients & Nutrition (Spoonacular)

| Tool Name | Endpoint | Function |
| :--- | :--- | :--- |
| **`search_ingredients`** | `/food/ingredients/search` | Searches for an ingredient name to get its canonical ID and basic image. |
| **`get_ingredient_information`** | `/food/ingredients/{id}/information` | Fetches detailed nutritional info (calories per gram, macros) and category for a specific ingredient ID. |
