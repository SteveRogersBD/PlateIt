import os
from typing import Annotated, Literal, TypedDict
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import SystemMessage, HumanMessage, BaseMessage
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode, tools_condition

from tools import (
    search_recipes, search_by_nutrients, find_by_ingredients,
    get_recipe_information, find_similar_recipes, get_random_recipes,
    extract_recipe_from_url, search_ingredients, get_ingredient_information,
    create_recipe_card, google_search, google_image_search
)
from schemas import AgentResponse
from dotenv import load_dotenv

load_dotenv()

# --- 1. State Definition ---
class AgentState(TypedDict):
    messages: Annotated[list[BaseMessage], add_messages]

# --- 2. Setup Tools & Model ---
tools = [
    search_recipes, search_by_nutrients, find_by_ingredients,
    get_recipe_information, find_similar_recipes, get_random_recipes,
    extract_recipe_from_url, search_ingredients, get_ingredient_information,
    create_recipe_card, google_search, google_image_search
]

# The "Chef" uses this model (needs tool calling capability)
llm = ChatGoogleGenerativeAI(model="gemini-3-flash-preview", temperature=0)
llm_with_tools = llm.bind_tools(tools)

# The "Waiter" uses this model (needs structured output capability)
response_generator = llm.with_structured_output(AgentResponse)

# --- 3. Nodes ---

def chef_node(state: AgentState):
    """
    The 'Reasoning' node.
    Decides whether to call a tool or pass to the waiter.
    """
    return {"messages": [llm_with_tools.invoke(state["messages"])]}

def waiter_node(state: AgentState):
    """
    The 'Formatting' node.
    Takes the conversation history and formats the final response for the App.
    """
    # Simply ask the model to summarize the latest state into our Schema
    
    # We provide a system prompt to guide the formatting
    system_prompt = SystemMessage(content="""
    You are the 'Waiter' for the PlateIt App.
    Your job is to take the Chef's analysis and format it for the user's screen.
    
    1. Look at the last message from the 'Chef' (AI).
    2. Create a friendly 'chat_bubble' message.
    3. If the Chef found recipes, ingredients, or videos, populate the corresponding UI payload.
    4. If the Chef just answered a question (text only), set ui_type to 'none'.
    
    Be concise and friendly!
    """)
    
    # We combine system prompt + history
    messages = [system_prompt] + state["messages"]
    
    response = response_generator.invoke(messages)
    
    # We append this final structured response as a special message or just return it?
    # For LangGraph state, we usually append an AIMessage.
    # But since we want the JSON object, we might want to print it or store it specially.
    # For this flow, let's just return it as a final print in the main loop, 
    # but strictly speaking, the node returns state updates.
    # Let's wrap the JSON in a strict AIMessage so the graph is happy, 
    # OR we can just return the raw object if this is the last node.
    
    # Let's return the structured object under a specific key if we extended state, 
    # but to keep it simple, we will act as a "pass through" that effectively ends the turn.
    # We will attach the "final_response" to the state if we wanted to persistence.
    
    return {"messages": [HumanMessage(content=str(response.model_dump()))]} 


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
