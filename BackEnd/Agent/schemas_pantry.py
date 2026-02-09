from pydantic import BaseModel
from typing import List, Optional

class IngredientSearchRequest(BaseModel):
    ingredients: List[str]
    number: int = 10

class RecipeSummary(BaseModel):
    id: int
    title: str
    image: Optional[str] = None
    usedIngredientCount: int
    missedIngredientCount: int
    likes: int
