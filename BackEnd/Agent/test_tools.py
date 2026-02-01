from tools import google_search, google_image_search
from dotenv import load_dotenv

load_dotenv()

def test_google_search():
    print("--- Testing Google Search ---")
    query = "History of Pizza"
    result = google_search.invoke(query)
    print(f"Query: {query}")
    print(f"Result:\n{result[:500]}...") # Print first 500 chars

def test_google_image_search():
    print("\n--- Testing Google Image Search ---")
    query = "Margherita Pizza"
    result = google_image_search.invoke(query)
    print(f"Query: {query}")
    print(f"Result: {result}")

if __name__ == "__main__":
    test_google_search()
    test_google_image_search()
