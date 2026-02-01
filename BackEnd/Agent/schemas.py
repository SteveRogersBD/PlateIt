from typing import List, Optional, Union, Literal
from pydantic import BaseModel, Field

# --- Sub-Components ---

class RecipeCard(BaseModel):
    id: int = Field(description="The unique ID of the recipe")
    title: str = Field(description="The name of the dish")
    image_url: Optional[str] = Field(None, description="URL of the recipe image")
    ready_in_minutes: Optional[int] = Field(None, description="Preparation time in minutes")
    missed_ingredient_count: Optional[int] = Field(None, description="Number of missing ingredients (if applicable)")

class IngredientItem(BaseModel):
    id: int
    name: str = Field(description="Name of the ingredient")
    image: Optional[str] = Field(None, description="URL of the ingredient image")
    amount: Optional[str] = Field(None, description="Amount needed/available (e.g. '2 cups')")

class VideoItem(BaseModel):
    title: str
    url: str = Field(description="Youtube or Video URL")
    thumbnail: Optional[str] = Field(None)

# --- Payloads ---

class RecipeListPayload(BaseModel):
    items: List[RecipeCard]

class IngredientListPayload(BaseModel):
    items: List[IngredientItem]
    
class VideoListPayload(BaseModel):
    items: List[VideoItem]

# --- The Master Response ---

class AgentResponse(BaseModel):
    chat_bubble: str = Field(description="The friendly text response to the user. Use emojis where appropriate!")
    
    ui_type: Literal["recipe_list", "ingredient_list", "video_list", "none"] = Field(
        "none", 
        description="The type of UI widget to display below the text."
    )
    
    # We use Optional here because 'none' type won't have data
    recipe_data: Optional[RecipeListPayload] = None
    ingredient_data: Optional[IngredientListPayload] = None
    video_data: Optional[VideoListPayload] = None
