import os
from typing import Annotated, Literal, TypedDict
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import SystemMessage, HumanMessage, BaseMessage
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode, tools_condition

from better_agent import Recipe
from tools import (
    search_recipes, search_by_nutrients, find_by_ingredients,
    get_recipe_information, find_similar_recipes, get_random_recipes,
    extract_recipe_from_url, search_ingredients, get_ingredient_information,
    create_recipe_card, google_search, google_image_search, search_youtube
)
from schemas import AgentResponse
from dotenv import load_dotenv

load_dotenv()

# Keys should be provided via environment variables.

# --- 1. State Definition ---
class AgentState(TypedDict):
    messages: Annotated[list[BaseMessage], add_messages]
    # Context injected from the App
    recipe: Recipe | None
    current_step: int
    image_data: str | None # Base64 encoded image
    
# --- 2. Setup Tools & Model ---
tools = [
     search_recipes, search_by_nutrients, find_by_ingredients,
     get_recipe_information, find_similar_recipes, get_random_recipes,
     extract_recipe_from_url, search_ingredients, get_ingredient_information,
     create_recipe_card, google_search, google_image_search, search_youtube
]

# The "Chef" model
llm = ChatGoogleGenerativeAI(model="gemini-3-flash-preview", temperature=0)
llm_with_tools = llm.bind_tools(tools)

# The "Waiter" model (Structural output)
response_generator = llm.with_structured_output(AgentResponse)

# --- 3. Nodes ---

from better_agent import Recipe # Import Recipe model

def chef_node(state: AgentState):
    """
    The 'Reasoning' node.
    Injects context about the current cooking step using the Recipe object.
    Handles Multimodal Input (Text + Image).
    """
    
    # 1. Try to get context from the structured Recipe object
    recipe = state.get("recipe")
    idx = state.get("current_step", 0)
    step_context = "General Cooking Support"
    
    if recipe and recipe.steps:
        # Check bounds
        if isinstance(idx, int) and 0 <= idx < len(recipe.steps):
            current_text = recipe.steps[idx]
            step_context = f"Step {idx + 1} of {len(recipe.steps)}: {current_text}"
        else:
             step_context = "Unknown step index."
    
    # 2. Fallback to string if provided (legacy/direct injection)
    elif isinstance(state.get("current_step"), str):
        step_context = state.get("current_step")

    # System Prompt for Context
    system_msg = SystemMessage(content=f"""
    You are an expert Chef Assistant guiding a user through a recipe.
    
    CURRENT SITUATION:
    The user is working on: "{step_context}"
    
    YOUR JOB:
    1. Answer the user's question based on this step context.
    2. If the user sends an IMAGE, analyze it carefully (doneness, texture, mistakes).
    3. KEEP ANSWERS SHORT (1-2 sentences) and conversational. The user is cooking and listening to you via voice.
    """)
    
    # --- Multimodal Message Construction ---
    input_messages = state["messages"]
    last_user_msg = input_messages[-1]
    
    # Check if we have image data in the state (injected by server)
    image_b64 = state.get("image_data")
    
    if image_b64:
        print("--- Chef Node: Attaching Image to Prompt ---")
        # Structure the message content as a list for LangChain Google integration
        # text + image_url (data scheme)
        
        # We replace the last text-only message with a multimodal one
        content_parts = [
            {"type": "text", "text": last_user_msg.content},
            {
                "type": "image_url", 
                "image_url": {"url": f"data:image/jpeg;base64,{image_b64}"}
            }
        ]
        
        multimodal_msg = HumanMessage(content=content_parts)
        
        # Replace the last message in history with our new multimodal one
        # Note: In LangGraph, we can't easily 'replace' inside the list passed to invoke(messages) 
        # unless we reconstruct it.
        history = [system_msg] + input_messages[:-1] + [multimodal_msg]
        
    else:
        # Text only
        history = [system_msg] + input_messages
    
    return {"messages": [llm_with_tools.invoke(history)]}

def waiter_node(state: AgentState):
    """
    The 'Formatting' node.
    Ensures the output is clean JSON for the Android App.
    """
    system_prompt = SystemMessage(content="""
    You are the 'Waiter' for the PlateIt App.
    Format the Chef's response for the mobile app.
    
    1. 'chat_bubble': The text to behave displayed and SPOKEN by the TTS engine. Keep it natural.
    2. 'ui_component': If the user asked for a timer, unit conversion, or image, specify it here (optional).
    """)
    
    messages = [system_prompt] + state["messages"]
    response = response_generator.invoke(messages)
    
    # Return the raw JSON string as the final message content for the server to parse
    return {"messages": [HumanMessage(content=response.model_dump_json())]} 


# --- 4. The Graph ---

builder = StateGraph(AgentState)

builder.add_node("chef", chef_node)
builder.add_node("tools", ToolNode(tools))
# We invoke the waiter manually at the end of the chef's run if no tools are called.

builder.add_edge(START, "chef")

def router(state: AgentState):
    """
    Check if the chef wants to call a tool.
    If YES -> Go to 'tools'
    If NO -> Go to 'waiter' (to format the answer)
    """
    # This is standard LangGraph logic to check for tool_calls
    last_message = state["messages"][-1]
    if last_message.tool_calls:
        return "tools"
    return "waiter"

builder.add_conditional_edges("chef", router)
builder.add_edge("tools", "chef") # Loop back to chef after tool use

# The Waiter is the temporary "End" of the turn
# In a real API, we would stream this result out.
builder.add_node("waiter", waiter_node)
builder.add_edge("waiter", END) 

graph = builder.compile()

# --- 5. Console Test Loop ---

if __name__ == "__main__":
    print("--- PlateIt Chef Agent (Type 'q' to quit) ---")
    while True:
        user_input = input("User: ")
        if user_input.lower() in ["q", "quit"]:
            break
            
        initial_state = {"messages": [HumanMessage(content=user_input)]}
        
        # We want to catch the FINAL output from the Waiter
        final_state = None
        for event in graph.stream(initial_state):
            for key, value in event.items():
                print(f"[Node: {key}]")
                # if key == "waiter": ... handle display
        
        # Just to show the final structured/raw output from the waiter logic:
        # (Pass)
