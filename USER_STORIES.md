# PlateIt User Stories & Epics

## Persona
**Busy Home Cook**
*   Saves Recipe Videos
*   Has Ingredients but Not Everything
*   Wants to Cook Now, Not Plan Forever
*   Hates Being Forced to Shop

---

## Epic 1: Turn Inspiration Into a Cookable Recipe

### User Story 1.1 — Video to Recipe
**As a** User,
**When** I Paste a Recipe Video or Link,
**I Want** the App to Extract Ingredients, Amounts, and Steps,
**So That** I Can Cook Without Rewatching the Video.

**Acceptance Criteria**
*   Supports YouTube Videos and Shorts
*   Structured Output (Ingredients, Steps, Time)
*   Marks Estimates Clearly
*   Fully Editable by the User

---

## Epic 2: Understand What I Already Have

### User Story 2.1 — Import Pantry
**As a** User,
**I Want** to Import What I Have Using a Photo (Fridge, Groceries, Handwritten List) or Text,
**So That** I Don’t Have to Manually Type Everything.

**Acceptance Criteria**
*   Accepts Image or Text Input
*   AI Extracts Items
*   Nothing Is Auto-Saved
*   Always Shows a Review Screen

### User Story 2.2 — Confirm Pantry
**As a** User,
**I Want** to Quickly Edit the Detected Pantry List,
**So That** Mistakes Don’t Block Me From Cooking.

**Acceptance Criteria**
*   Item Names Are Editable
*   Quantities Are Optional
*   Add or Remove Items in One Tap
*   Confirmation Is Required

---

## Epic 3: Detect Friction Before Cooking

### User Story 3.1 — Ingredient Availability Check
**As a** User,
**After** Selecting a Recipe,
**I Want** to See Which Ingredients I Have and Which Are Missing,
**So That** I Don’t Get Stuck Mid-Cooking.

**Acceptance Criteria**
*   Clear “Have” vs “Missing” Split
*   Defaults to Missing
*   Fast Toggle Interaction

---

## Epic 4: Resolve Missing Ingredients Intelligently

### User Story 4.1 — Choose How to Proceed
**As a** User,
**When** Ingredients Are Missing,
**I Want** Clear Options on What to Do Next,
**So That** I Stay in Control.

**Options**
*   Generate Grocery List
*   Find Alternative Recipes Using What I Have
*   Try Ingredient Substitutions

### User Story 4.2 — Alternative Recipe Suggestion
**As a** User,
**If** I Don’t Want to Shop,
**I Want** the App to Suggest Similar Recipes That Use My Available Ingredients,
**So That** I Can Still Cook Today.

**Acceptance Criteria**
*   Suggests 3–5 Alternatives
*   Ranked by Missing Ingredient Count
*   Shows Why Each Alternative Fits
*   User Explicitly Selects One

### User Story 4.3 — Safe Ingredient Substitution
**As a** User,
**When** Only Small Items Are Missing,
**I Want** Safe Substitutions Suggested,
**So That** I Don’t Abandon the Recipe.

**Acceptance Criteria**
*   Only Common, Low-Risk Swaps
*   Substitutions Clearly Labeled
*   User Must Approve Changes

---

## Epic 5: Prepare and Shop (Optional Path)

### User Story 5.1 — Grocery List Generation
**As a** User,
**If** I Choose to Shop,
**I Want** a Clean Grocery List Generated From Missing Ingredients,
**So That** I Can Buy Only What I Need.

**Acceptance Criteria**
*   Grouped by Category
*   Editable
*   Export or Deep-Link Supported
*   No Forced In-App Checkout

---

## Epic 6: Guide Me Through Cooking

### User Story 6.1 — Cook Mode
**As a** User,
**Once** I Start Cooking,
**I Want** a Distraction-Free Step-by-Step Mode,
**So That** I Can Focus on Cooking.

**Acceptance Criteria**
*   One Step at a Time
*   Large, Readable Text
*   Timers Supported
*   Pause and Resume Cooking

### User Story 6.2 — AI Cooking Coach
**As a** User,
**While** Cooking,
**I Want** Short, Contextual Tips When Needed,
**So That** I Don’t Second-Guess Myself.

**Acceptance Criteria**
*   Step-Specific Tips
*   Actionable Guidance Only
*   No Chat Required
*   Optional and Unobtrusive
