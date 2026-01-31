
from database import create_db_and_tables

if __name__ == "__main__":
    print("Creating tables in Supabase...")
    try:
        create_db_and_tables()
        print("Tables created successfully!")
    except Exception as e:
        print(f"Error creating tables: {e}")
