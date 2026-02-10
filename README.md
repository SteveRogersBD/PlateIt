# PlateIt 🍽️

**PlateIt** is an intelligent, AI-powered culinary companion designed to revolutionize your cooking experience. By combining computer vision, generative AI, and a robust recipe database, PlateIt helps you reduce food waste and discover delicious meals based on what you already have.

---

## ✨ Key Features

- **📸 AI Pantry Scanner**: Snap a photo of your ingredients, and our Gemini-powered vision agent will identify them and populate your virtual pantry.
- **🤖 Personal Chef Agent**: Chat with a context-aware AI chef to get recipe ideas, cooking tips, or ingredient substitutions.
- **🔍 Smart Recipe Discovery**: Find recipes that match your available ingredients using Spoonacular's extensive database.
- **🎥 Multimedia Cooking**: Search for specific cooking techniques and get instant YouTube video tutorials with thumbnails.
- **📱 Native Android Experience**: A smooth, Material Design interface built for speed and usability.

---

## 🛠️ Tech Stack

### Mobile App (Android)
- **Language**: Java
- **Networking**: Retrofit2, OkHttp3
- **UI**: Material Design Components
- **Image Loading**: Picasso / Glide

### Backend API
- **Framework**: FastAPI (Python)
- **AI Orchestration**: LangGraph, LangChain
- **LLM & Vision**: Google Gemini 1.5/2.0 Flash & Pro
- **Database**: Supabase (PostgreSQL)
- **Deployment**: Google Cloud Run (Dockerized)

### External APIs
- **Spoonacular**: Recipe data and nutritional info.
- **SerpApi**: Google Search & YouTube Data.

---

## 📂 File Structure

```
PlateIt/
├── app/                  # Android Application Source
│   ├── src/main/java/    # Java Code (Activities, Fragments, Adapters)
│   ├── src/main/res/     # Resources (Layouts, Drawables, Values)
│   └── build.gradle      # App-level Gradle config
│
├── BackEnd/              # Python Backend
│   ├── Agent/            # AI Agent Logic
│   │   ├── agent_server.py # FastAPI Entry Point
│   │   ├── better_agent.py # LangGraph Workflow
│   │   ├── tools.py        # External Tool Definitions
│   │   └── models.py       # Pydantic & SQLModel schemas
│   └── Dockerfile        # Cloud Run Deployment Config
│
└── README.md             # Project Documentation
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Koala or later)
- **Python 3.10+**
- **Docker** (optional, for containerization)

### 1. Clone the Repository
```bash
git clone https://github.com/SteveRogersBD/PlateIt.git
cd PlateIt
```

### 2. Backend Setup
Navigate to the backend directory and install dependencies:
```bash
cd BackEnd/Agent
pip install -r requirements.txt
```

Create a `.env` file in `BackEnd/Agent/` with your API keys:
```ini
GOOGLE_API_KEY=your_gemini_key
GEMINI_API_KEY=your_gemini_key
SPOONACULAR_API_KEY=your_spoonacular_key
SERP_API_KEY=your_serpapi_key
DATABASE_URL=your_supabase_url
```

Run the server locally:
```bash
uvicorn agent_server:app --reload
```

### 3. Android Setup
1. Open the project in **Android Studio**.
2. Sync Gradle files.
3. Update `RetrofitClient.java` if testing locally (set `BASE_URL` to your local IP).
4. Connect a device or emulator and press **Run**.

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/SteveRogersBD">SteveRogersBD</a> using Google Gemini
</p>
