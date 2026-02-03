# 🚀 PlateIt: Gemini 3 Hackathon Implementation Plan

**Goal:** Build a "Cooking Coach" that listens, speaks, and sees.
**Deadline:** 5 Days.

## 📅 Daily Sprint Schedule

### **Day 1: The Brain & Memory (Database & Agent Core)**
**Goal:** The system can save users, save recipes, and understand video layouts.
- [ ] **1.1 DB Setup:** Finalize Firebase Firestore for Users/Recipes.
- [ ] **1.2 Recipe Schema:** Define the JSON structure for a "Coachable" recipe (separated ingredients, step-by-step arrays).
- [ ] **1.3 Video Extractor Agent:**
    - [ ] Upgrade `extract_recipe_from_url` to handle generic text scrapers.
    - [ ] **Gemini Native Video:** Implement ability to parse recipe from a video file (mp4) using Gemini 1.5 Pro.

### **Day 2: The Coach (State Management & Logic)**
**Goal:** The Agent tracks *where* the user is and *what* they need.
- [ ] **2.1 Coach Logic:** Create a `cooking_session` in DB.
- [ ] **2.2 Navigation Tools:** Add tools for `next_step`, `repeat_step`, `go_to_step(n)`.
- [ ] **2.3 Context Awareness:** Modify Agent prompt to answer questions *relative* to the current step.
- [ ] **2.4 Smart Shopping List:** Implement logic to compare `RecipeIngredients` vs `Pantry`. Return a "Missing Items" list.

### **Day 3: The Voice (Ears & Mouth)**
**Goal:** Hands-free interaction.
- [ ] **3.1 Android STT:** Implement Speech-to-Text in `CookingModeActivity`.
- [ ] **3.2 Agent TTS:** The Agent's text response is converted to Audio (Google Cloud TTS or Android Native).
- [ ] **3.3 Wake Word / Trigger:** Basic "Hey Chef" or button-press-to-talk loop.

### **Day 4: The Eyes (Vision & Tools)**
**Goal:** Multimodal capabilities.
- [ ] **4.1 Photo Input:** Allow user to upload image in chat.
- [ ] **4.2 "Fridge Scan":** Agent tool to identify ingredients from photo.
- [ ] **4.3 Smart Tools:** Add `set_timer(minutes)`, `convert_units`.
- [ ] **4.4 Copyable Shopping List:** Ensure the missing ingredients list is easily copyable by the user (UI feature).

### **Day 5: Polish & Submission**
**Goal:** A crash-free demo.
- [ ] **5.1 UI Polish:** Transitions, Loading states.
- [ ] **5.2 Demo Video:** Record the submission video.
- [ ] **5.3 Submission:** DevPost text and links.

---
## 🛠 Tech Decisions
- **DB:** Firebase Firestore.
- **Video Model:** Gemini 1.5 Flash (Speed) or Pro (Accuracy).
- **Voice:** Android Native (Speed) or Google Cloud (Quality).
