from chef_agent import graph
from langchain_core.messages import HumanMessage

def run_test():
    print("--- Running Chef Agent Demo ---")
    query = "I have leftover chicken and cheddar cheese. What can I make?"
    print(f"User Query: {query}\n")
    
    initial_state = {"messages": [HumanMessage(content=query)]}
    final_state = graph.invoke(initial_state)
    
    # The last message should be from the Waiter with the JSON structure
    last_message = final_state["messages"][-1]
    print("--- Final Agent Response (JSON) ---")
    print(last_message.content)

if __name__ == "__main__":
    run_test()
