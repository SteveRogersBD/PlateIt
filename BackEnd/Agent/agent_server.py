from fastapi import FastAPI, Depends, HTTPException
from pydantic import BaseModel
from sqlmodel import Session, select
from typing import Optional
import uuid
import os
from dotenv import load_dotenv

load_dotenv()

from better_agent import workflow as recipe_workflow
from database import get_session, create_db_and_tables
from models import User
from tools import search_youtube_videos
import random

app = FastAPI()

# --- HARDCODED GEMINI KEY ---
os.environ["GOOGLE_API_KEY"] = "AIzaSyDDpI0d1hz9Bz0nbe7vS936FZ0IDbvTsqo"
os.environ["GEMINI_API_KEY"] = "AIzaSyDDpI0d1hz9Bz0nbe7vS936FZ0IDbvTsqo"
# ----------------------------

# --- Auth Models ---
class SignupRequest(BaseModel):
    full_name: Optional[str] = None
    username: str
    email: str
    password: str

class SigninRequest(BaseModel):
    email: str
    password: str

class UpdatePreferencesRequest(BaseModel):
    user_id: uuid.UUID
    preferences: list[str]

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

@app.post("/preferences/update")
def update_preferences(request: UpdatePreferencesRequest, session: Session = Depends(get_session)):
    user = session.get(User, request.user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    user.preferences = request.preferences
    session.add(user)
    session.commit()
    
    return {"message": "Preferences updated", "preferences": user.preferences}

@app.get("/preferences/{user_id}")
def get_preferences(user_id: uuid.UUID, session: Session = Depends(get_session)):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
        
    return {"preferences": user.preferences}

@app.post("/preferences/update")
def update_preferences(request: UpdatePreferencesRequest, session: Session = Depends(get_session)):
    user = session.get(User, request.user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    user.preferences = request.preferences
    session.add(user)
    session.commit()
    
    return {"message": "Preferences updated", "preferences": user.preferences}

@app.get("/preferences/{user_id}")
def get_preferences(user_id: uuid.UUID, session: Session = Depends(get_session)):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
        
    return {"preferences": user.preferences}

# --- Video Recommendation Endpoint ---
@app.get("/recommendations/videos/{user_id}")
def get_video_recommendations(user_id: uuid.UUID, session: Session = Depends(get_session)):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    preferences = user.preferences if user.preferences else []
    all_videos = []
    seen_links = set()

    # Strategy: diverse sampling
    # If user has > 3 preferences, pick 3 random ones to mix.
    # If <= 3, use all of them.
    if len(preferences) > 3:
        target_prefs = random.sample(preferences, 3)
    else:
        target_prefs = preferences

    # Prepare queries
    queries = []
    if not target_prefs:
        queries.append("trending cooking recipes")
    else:
        for p in target_prefs:
            queries.append(f"{p} recipes")

    print(f"Fetching videos for topics: {queries}")

    # Fetch and Aggregate
    for q in queries:
        # We fetch ~5 videos per topic
        videos = search_youtube_videos(q, limit=5)
        
        # Check if output is a list (tool returns list on success)
        if isinstance(videos, list):
            for v in videos:
                if v.get('link') and v['link'] not in seen_links:
                    all_videos.append(v)
                    seen_links.add(v['link'])
    
    # Shuffle results to mix "Italian" and "Dessert" videos together
    random.shuffle(all_videos)
    
    return {"videos": all_videos}

# --- Blog Recommendation Endpoint ---
from tools import search_google_blogs

@app.get("/recommendations/blogs/{user_id}")
def get_blog_recommendations(user_id: uuid.UUID, session: Session = Depends(get_session)):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    preferences = user.preferences if user.preferences else []
    all_blogs = []
    seen_links = set()

    # Strategy: diverse sampling
    if len(preferences) > 3:
        target_prefs = random.sample(preferences, 3)
    else:
        target_prefs = preferences

    # Prepare queries
    queries = []
    if not target_prefs:
        queries.append("popular food blogs recipes")
    else:
        for p in target_prefs:
            queries.append(f"best {p} food blog recipe -site:youtube.com") # Exclude YouTube

    print(f"Fetching blogs for topics: {queries}")

    # Fetch and Aggregate
    for q in queries:
        # We fetch ~5 blogs per topic
        blogs = search_google_blogs(q, limit=5)
        
        if isinstance(blogs, list):
            for b in blogs:
                if b.get('link') and b['link'] not in seen_links:
                    all_blogs.append(b)
                    seen_links.add(b['link'])
    
    # Shuffle results
    random.shuffle(all_blogs)
    
    return {"blogs": all_blogs}

# --- Existing Recipe Extraction ---
class VideoRequest(BaseModel):
    video_url: str

@app.post("/extract_recipe")
def extract_recipe(request: VideoRequest):
    initial_state = {"url": request.video_url}
    try:
        final_state = recipe_workflow.invoke(initial_state)
        return final_state.get('recipe',{})
    except Exception as e:
        print(f"Error executing workflow: {e}")
        raise HTTPException(status_code=500, detail=str(e))

from fastapi import File, UploadFile
import shutil

@app.post("/extract_recipe_image")
def extract_recipe_image(file: UploadFile = File(...)):
    try:
        # Save the uploaded file temporarily
        temp_filename = f"temp_upload_{file.filename}"
        with open(temp_filename, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
            
        abs_path = os.path.abspath(temp_filename)
        
        # Pass the local file path as the URL
        initial_state = {"url": abs_path}
        
        # Invoke agent
        final_state = recipe_workflow.invoke(initial_state)
        
        # Clean up is done by agent usually, but we can verify later
        
        return final_state.get('recipe', {})
    except Exception as e:
         print(f"Error processing image: {e}")
         raise HTTPException(status_code=500, detail=str(e))

# --- New Cooking Chat ---
from chef_agent import graph as chef_workflow
from langchain_core.messages import HumanMessage
from typing import Dict, Any

# --- New Cooking Chat ---
class ChatRequest(BaseModel):
    message: str
    thread_id: str
    recipe: Optional[Dict[str, Any]] = None # Full recipe object, optional for general chat
    current_step: int # 0-indexed step
    image_data: Optional[str] = None # Base64 encoded image

@app.post("/chat")
def chat_endpoint(request: ChatRequest):
    print(f"--- Chat Request: {request.message} (Step {request.current_step}) ---")
    
    # 1. Construct State
    # Convert dict back to Recipe object logic is handled by Pydantic inside the node usually,
    # but since our AgentState expects a 'Recipe' object (Pydantic model) and we get a Dict,
    # we might need to rely on the node to handle it or convert it here.
    # The chef_node currently does: recipe = state.get("recipe")
    # We should pass the dict, and let's ensure chef_node handles dict access OR convert it here.
    # For simplicity, we pass the dict and if our chef_agent expects a Pydantic object, we convert it there 
    # OR we convert it here.
    # Let's convert it here for type safety if better_agent is available.
    
    from better_agent import Recipe
    recipe_obj = None
    if request.recipe:
        try:
            recipe_obj = Recipe(**request.recipe)
        except Exception as e:
            print(f"Warning: Could not parse recipe object: {e}")
            recipe_obj = None

    initial_state = {
        "messages": [HumanMessage(content=request.message)],
        "recipe": recipe_obj,
        "current_step": request.current_step,
        "image_data": request.image_data
    }
    
    # 2. Invoke Chef Agent
    try:
        final_state = chef_workflow.invoke(initial_state)
        
        # 3. Extract Response
        # The waiter node puts the JSON string in the last message's content
        last_message = final_state["messages"][-1]
        response_json_str = last_message.content
        
        # We assume it's valid JSON because Waiter guarantees it (mostly)
        import json
        response_data = json.loads(response_json_str)
        
        return response_data
        
    except Exception as e:
        print(f"Chat Error: {e}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

# --- Recipe Details Endpoint ---
@app.get("/recipes/{recipe_id}/full")
def get_full_recipe_details(recipe_id: int):
    """
    Fetches full recipe details from Spoonacular and maps to App's RecipeResponse format.
    """
    import os
    import requests
    
    api_key = os.getenv("SPOONACULAR_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="API Key missing")
        
    url = f"https://api.spoonacular.com/recipes/{recipe_id}/information"
    params = {"apiKey": api_key}
    
    try:
        resp = requests.get(url, params=params)
        resp.raise_for_status()
        data = resp.json()
        
        # Map to App Format
        # 1. Ingredients
        ingredients = []
        for ing in data.get("extendedIngredients", []):
            amount = f"{ing.get('amount', '')} {ing.get('unit', '')}".strip()
            ingredients.append({
                "name": ing.get("original", ing.get("name")),
                "amount": amount,
                "imageUrl": f"https://img.spoonacular.com/ingredients_100x100/{ing.get('image', '')}"
            })
            
        # 2. Steps
        steps = []
        if data.get("analyzedInstructions"):
            for step in data["analyzedInstructions"][0].get("steps", []):
                steps.append({
                    "instruction": step.get("step"),
                    "visual_query": None,
                    "imageUrl": None
                })
        else:
            # Fallback to splitting instructions string
            instr = data.get("instructions", "")
            if instr:
                # Remove HTML tags if any
                import re
                clean_instr = re.sub('<[^<]+?>', '', instr)
                steps = [{
                    "instruction": s.strip(),
                    "visual_query": None,
                    "imageUrl": None
                } for s in clean_instr.split('.') if s.strip()]
                
        return {
            "name": data.get("title"),
            "total_time": str(data.get("readyInMinutes", 0)) + " mins",
            "ingredients": ingredients,
            "steps": steps
        }
        
    except Exception as e:
         print(f"Error fetching recipe {recipe_id}: {e}")
         raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)