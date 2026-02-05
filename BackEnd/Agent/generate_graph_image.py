from better_agent import workflow
import os

def main():
    try:
        print("Generating graph image...")
        # Get the graph drawable
        graph_drawable = workflow.get_graph()
        # Generate PNG
        img_bytes = graph_drawable.draw_mermaid_png()
        
        output_path = "agent_workflow.png"
        with open(output_path, "wb") as f:
            f.write(img_bytes)
        
        print(f"Successfully saved graph to {os.path.abspath(output_path)}")
    except Exception as e:
        print(f"Error generating graph: {e}")
        # Fallback to mermaid code if image generation fails (e.g. no internet/server)
        try:
            print("Attempting to print mermaid code directly...")
            print(graph_drawable.draw_mermaid())
        except Exception as e2:
            print(f"Could not even print mermaid code: {e2}")

if __name__ == "__main__":
    main()
