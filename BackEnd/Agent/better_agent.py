import json
import os
from langgraph.constants import START
from langgraph.graph import StateGraph, END
from typing import TypedDict, Annotated, final
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import HumanMessage, AIMessage, BaseMessage, SystemMessage
from langgraph.graph.message import add_messages
from pydantic import BaseModel, Field
import requests

class Recipe(BaseModel):
    name: str = Field("Name of the recipe")
    steps: list[str] = Field("List of steps that must be taken from the recipe")
    ingredients: list[str] = Field("List of ingredients extracted from the recipe")

class Video(TypedDict):
    title: str
    video_id: str
    video_url: str
    description: str
    transcript: list[str]
    recipe: Recipe


llm = ChatGoogleGenerativeAI(model="gemini-3-flash-preview")



from urllib.parse import urlparse, parse_qs

def get_video_id(state: Video):
    url = state["video_url"]
    # Case 1: Standard URL (e.g., youtube.com/watch?v=VIDEO_ID)
    if "watch" in url:
        parsed_url = urlparse(url)
        # Grab the 'v' parameter from the query string
        video_id = parse_qs(parsed_url.query)['v'][0]
        return {"video_id": video_id}

    # Case 2: Shorts (e.g., youtube.com/shorts/VIDEO_ID)
    elif "shorts" in url:
        # Split the text by "shorts/" and take the part after it
        video_id = url.split("shorts/")[1].split("?")[0] # .split("?")[0] removes any extra params
        return {"video_id": video_id}

    # Case 3: Share links (e.g., youtu.be/VIDEO_ID)
    elif "youtu.be" in url:
        # Split by "/" and take the very last piece
        video_id = url.split("/")[-1].split("?")[0]
        return {"video_id": video_id}

    return {"video_id": ""}

# first node: get the transcript
def get_transcript(state: Video):
    # The base endpoint without parameters
    url = "https://serpapi.com/search"

    # We pass the parameters as a dictionary.
    # This automatically handles adding the '?' and '&' characters for you.
    params = {
        "engine": "youtube_video_transcript",
        "v": state["video_id"],
        "api_key": "5430f234ebc964dd1ca39b4c98572751c4ddadf23fb5157fce32e354e7aa9811",
    }

    try:
        response = requests.get(url, params=params)

        # Check for errors
        response.raise_for_status()

        # Parse the JSON
        data = response.json()

        transcript_list = data["transcript"]
        transcripts = []
        for transcript in transcript_list:
            transcripts.append(transcript["snippet"])

        return {"transcript": transcripts}

    except requests.exceptions.HTTPError as e:
        print(f"HTTP error: {e}")
    except Exception as e:
        print(f"Error: {e}")

def get_description(state: Video):
    # The base endpoint without parameters
    url = "https://serpapi.com/search"

    # We pass the parameters as a dictionary.
    # This automatically handles adding the '?' and '&' characters for you.
    params = {
        "engine": "youtube_video",
        "v": state["video_id"],
        "api_key": "5430f234ebc964dd1ca39b4c98572751c4ddadf23fb5157fce32e354e7aa9811",
    }

    try:
        response = requests.get(url, params=params)

        # Check for errors
        response.raise_for_status()

        # Parse the JSON
        data = response.json()

        desc = data["description"]["content"]

        return {"description": desc}

    except requests.exceptions.HTTPError as e:
        print(f"HTTP error: {e}")
    except Exception as e:
        print(f"Error: {e}")


recipe_llm = llm.with_structured_output(Recipe)
def extract_recipe(state: Video):
    prompt = [
        SystemMessage(content="You are a cooking expert."),
        HumanMessage(content=f"""
                Based on the video transcript: \n{state['transcript']}
                \n and the description: \n{state['description']}

                Create a detailed recipe for a classic dish. Include:
                - Full recipe description
                - Clear cooking instructions
                - List of ingredients
                - Step-by-step instructions
                - Output format: Provide the recipe content in Markdown format.

                Please provide a comprehensive recipe.
                """)
    ]
    response = recipe_llm.invoke(prompt)
    if not response:
        raise ValueError(f"Failed to extract recipe from URL: "
                         f"{state['video_url']}. Ending execution.")
    return {
        'recipe': response
    }


def pass_node(state: Video):
    return {}

graph = StateGraph(Video)

graph.add_node("get_video_id",get_video_id)
graph.add_node("get_transcript",get_transcript)
graph.add_node("get_description",get_description)
graph.add_node("merge_results", pass_node)
graph.add_node("extract_recipe",extract_recipe)

graph.add_edge(START,"get_video_id")
graph.add_edge("get_video_id","get_transcript")
graph.add_edge("get_video_id","get_description")
graph.add_edge("get_transcript","merge_results")
graph.add_edge("get_description","merge_results")
graph.add_edge("merge_results","extract_recipe")
graph.add_edge("extract_recipe",END)

workflow = graph.compile()

if __name__ == "__main__":


    attempts = 0
    max_retries = 3
    final_state = None

    while attempts < max_retries:
        url = input("Enter the URL: ")
        initial_state = {"video_url": url}
        print(f"--- Attempt {attempts + 1} ---")
        try:
            # Invoke the graph
            final_state = workflow.invoke(initial_state)

            # Check if we got valid results
            recipe = final_state.get('recipe')

            # Condition: Check if 'recipe' exists and has non-empty steps/ingredients
            if recipe and recipe.steps and recipe.ingredients:
                print("Success! Recipe extracted.")
                break  # Exit the loop on success
            else:
                print("Partial result: Missing steps or ingredients. Retrying...")

        except Exception as e:
            print(f"Error occurred: {e}")

        attempts += 1

    if final_state and final_state.get('recipe'):
        print("\n--- Final State ---")
        print(json.dumps(final_state.get('recipe', {}).model_dump(), indent=2))
    else:
        print("Failed to extract complete recipe after all attempts.")
