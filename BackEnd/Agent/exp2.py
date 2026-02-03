import os
from google import genai

# 1. Initialize the client using the correct environment variable
client = genai.Client(api_key=os.getenv("GOOGLE_API_KEY"))

# 2. Upload the video using the correct 'file' keyword argument
# No 'path' argument is needed in the new SDK
video_file = client.files.upload(file="my_video.mp4")

# 3. Request analysis using the simplified 'contents' structure
# Note: 'video_metadata' is not a valid config parameter;
# if you need thinking tokens, use the 'thinking_config' shown in the video
response = client.models.generate_content(
    model="gemini-3-flash-preview",
    contents=[
        video_file,
        "Tell me about this video"
    ],
    config={
        # Use thinking levels (High/Low) for complex reasoning instead of metadata [00:04:47]
        "thinking_config": {"include_thoughts": True}
    }
)

print(response.text)