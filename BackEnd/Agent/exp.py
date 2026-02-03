import os
import time

from google import genai
from google.genai import types
# 1. Setup the Client
# Replace 'YOUR_API_KEY' with your actual API key

client = genai.Client(api_key=os.getenv("GOOGLE_API_KEY"))

def analyze_video(video_path, user_prompt):
    print(f"Uploading file: {video_path}...")

    # 2. Upload the video to the Files API
    # This allows Gemini to "see" the video content
    video_file = client.files.upload(file=video_path)

    # 3. Wait for processing
    # Large video files need a moment to be indexed before they can be queried
    while video_file.state.name == "PROCESSING":
        print(".", end="", flush=True)
        time.sleep(2)
        video_file = client.files.get(name=video_file.name)

    if video_file.state.name == "FAILED":
        raise ValueError("Video processing failed.")

    print("\nVideo processing complete.")

    # 4. Generate content based on the video
    # You can specify the model (e.g., 'gemini-2.0-flash' or 'gemini-1.5-pro')
    response = client.models.generate_content(
        model="gemini-3-pro-preview",
        contents=[
            video_file,
            user_prompt
        ],
        config=types.GenerateContentConfig(
            # Optional: Add a system instruction to guide the model's behavior
            system_instruction="You are an expert video analyst."
        )
    )

    return response.text


# --- Main Execution ---
if __name__ == "__main__":
    # Path to your local video file
    VIDEO_PATH = "my_video.mp4"
    PROMPT = "Tell me about the video."

    try:
        result = analyze_video(VIDEO_PATH, PROMPT)
        print("\n--- Gemini's Analysis ---\n")
        print(result)
    except Exception as e:
        print(f"An error occurred: {e}")