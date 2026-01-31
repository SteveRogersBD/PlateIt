
from sqlmodel import SQLModel, create_engine, Session
from dotenv import load_dotenv
import os

# Load .env
load_dotenv()

# Get DB URL
DATABASE_URL = os.getenv("DATABASE_URL")

# Supabase requires "postgresql://" but sometimes provides "postgres://"
# SQLAlchemy needs "postgresql://"
if DATABASE_URL and DATABASE_URL.startswith("postgres://"):
    DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://", 1)

# Create Engine
# echo=True means it prints SQL to console (good for debugging)
engine = create_engine(DATABASE_URL, echo=True)

def get_session():
    """Dependency for FastAPI to get DB session"""
    with Session(engine) as session:
        yield session

def create_db_and_tables():
    """Creates the tables in Supabase if they don't exist"""
    # Import models here so SQLModel knows about them
    from models import User, PantryItem, Recipe, ChatSession, Message
    SQLModel.metadata.create_all(engine)
