from fastapi import FastAPI, Depends, HTTPException
from pydantic import BaseModel
from sqlmodel import Session, select
from typing import Optional
import uuid

from better_agent import workflow as recipe_workflow
from database import get_session, create_db_and_tables
from models import User

app = FastAPI()

@app.on_event("startup")
def on_startup():
    create_db_and_tables()

# --- Auth Models ---
class SignupRequest(BaseModel):
    full_name: Optional[str] = None
    username: str
    email: str
    password: str

class SigninRequest(BaseModel):
    email: str
    password: str

class AuthResponse(BaseModel):
    user_id: uuid.UUID
    email: str
    username: str
    message: str

# --- Auth Endpoints ---
@app.post("/signup", response_model=AuthResponse)
def signup(request: SignupRequest, session: Session = Depends(get_session)):
    # Check if user exists
    existing_user = session.exec(select(User).where((User.email == request.email) | (User.username == request.username))).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="User with this email or username already exists")
    
    # Create new user (Storing password as plain text as requested)
    new_user = User(
        email=request.email,
        username=request.username,
        full_name=request.full_name,
        password=request.password 
    )
    session.add(new_user)
    session.commit()
    session.refresh(new_user)
    
    return AuthResponse(
        user_id=new_user.id,
        email=new_user.email,
        username=new_user.username,
        message="User created successfully"
    )

@app.post("/signin", response_model=AuthResponse)
def signin(request: SigninRequest, session: Session = Depends(get_session)):
    statement = select(User).where(User.email == request.email)
    user = session.exec(statement).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    # Simple plain text password check
    if user.password != request.password:
        raise HTTPException(status_code=401, detail="Invalid credentials")
        
    return AuthResponse(
        user_id=user.id,
        email=user.email,
        username=user.username,
        message="Login successful"
    )

# --- Existing Recipe Extraction ---
class VideoRequest(BaseModel):
    video_url: str

@app.post("/extract_recipe")
def extract_recipe(request: VideoRequest):
    initial_state = {"video_url": request.video_url}
    try:
        final_state = recipe_workflow.invoke(initial_state)
        return final_state.get('recipe',{})
    except Exception as e:
        print(f"Error executing workflow: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# --- New Cooking Chat ---
class ChatRequest(BaseModel):
    message: str
    thread_id: str

@app.post("/chat")
def chat_endpoint(request: ChatRequest):
    return None

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)