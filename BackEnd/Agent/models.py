
import uuid
from datetime import datetime
from typing import Optional, Dict, List
from sqlmodel import SQLModel, Field, Column, JSON

# 1. Users
class User(SQLModel, table=True):
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    email: str = Field(index=True, unique=True)
    full_name: Optional[str] = None
    password: str = Field(index=True)
    username: str = Field(index=True, unique=True)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)
    dp_url: Optional[str] = None


# 2. PantryItems
class PantryItem(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    user_id: uuid.UUID = Field(foreign_key="user.id", index=True)
    item_text: List[str] = Field(default=[], sa_column=Column(JSON))
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)

# 3. Recipes
class Recipe(SQLModel, table=True):
    id: int = Field(default=None, primary_key=True)
    user_id: uuid.UUID = Field(foreign_key="user.id", index=True)
    title: str
    # Detailed data (Ingredients/Steps) stored as JSON for flexibility
    content_json: Dict = Field(default={}, sa_column=Column(JSON)) 
    source_url: Optional[str] = None # Youtube link if applicable
    image_url: Optional[str] = None
    created_at: datetime = Field(default_factory=datetime.utcnow)

# 4. ChatSessions (The "Thread")
class ChatSession(SQLModel, table=True):
    id: str = Field(primary_key=True) # This is the 'thread_id' used by LangGraph
    user_id: uuid.UUID = Field(foreign_key="user.id", index=True)
    title: str = Field(default="New Chat")
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)

# 5. Messages (The actual content)
class Message(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    session_id: str = Field(foreign_key="chatsession.id", index=True)
    sender: str # "user" or "ai"
    content: str # The text content
    created_at: datetime = Field(default_factory=datetime.utcnow)
