
from database import create_db_and_tables
from models import * # Import all models to ensure they are registered

print("Creating database tables...")
create_db_and_tables()
print("Tables created successfully!")
