# PlateIt AI Agent 🧠

The Core Intelligence of the application. This module orchestrates complex culinary workflows using **LangGraph** and is powered by **Google Gemini 1.5 Flash**.

## 🚀 The "Better Agent" Workflow

The agent is designed as a state machine that can handle multiple types of inputs (Text, YouTube Links, Images, Videos) and convert them into a structured `Recipe` format.

```mermaid
graph TD
    Start[User Input] --> Router{Router}
    
    Router -->|YouTube URL| YouTube[Extract Transcript & Details]
    Router -->|Video File| Video[Vision Analysis]
    Router -->|Food Image| Image[Vision Analysis]
    Router -->|Website URL| Scrape[Web Scraper]
    
    YouTube --> TextExtract[LLM Text Extraction]
    Video --> TextExtract
    Scrape --> TextExtract
    
    Image -->|Ingredients| IngLogic[Find Recipe by Ingredients]
    Image -->|Finished Dish| DishLogic[Identify Dish & Generate Recipe]
    
    TextExtract --> Format[Format to JSON Schema]
    IngLogic --> Format
    DishLogic --> Format
    
    Format --> Enrich[Image Enrichment Parallel]
    Enrich --> Merge[Combine Data]
    Merge --> End[Final Recipe JSON]
```

---

## 🛠️ The Chef's Toolkit

The agent has access to a specialized suite of tools, allowing it to interact with the real world:

### 1. Vision Capability
- **`analyze_image`**: Determines if an image is raw ingredients or a cooked meal.
- **`pantry_scan`**: Identifies multiple items in a single photo for inventory.

### 2. Information Retrieval
- **`search_recipes`**: Queries Spoonacular for verified recipes.
- **`find_by_ingredients`**: Reverse search—finds what you can cook with what you have.
- **`search_youtube`**: Finds relevant video tutorials with thumbnails.
- **`google_search`**: General culinary knowledge retrieval.

### 3. Data Processing
- **`extract_video_id`**: Parses YouTube URLs.
- **`download_video_file`**: Handles social media video downloads for analysis.

---

## 🤖 Why Gemini?

We utilize **Gemini 3** as our primary orchestrator for several reasons:
1.  **Multimodal Native**: It can "see" the food images and "watch" the cooking videos directly without needing separate OCR or frame-extraction pipelines.
2.  **Long Context Window**: Essential for processing full video transcripts or lengthy blog posts to extract accurate recipe steps.
3.  **Speed**: The Flash model provides near-instant responses, crucial for a chat application.

---

## 📂 Key Files

- **`better_agent.py`**: The main LangGraph workflow definition.
- **`chef_agent.py`**: The conversational persona (Chatbot) logic.
- **`tools.py`**: The complete library of external tool functions.
- **`schemas.py`**: Pydantic models for structured data validation.
