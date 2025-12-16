import os
import re
import pandas as pd
import os
import argparse

def parse_result_files(folder_path):
    """
    Parses result files in the specified folder and collects
    algorithm, instance, time, and gap data.
    """
    
    # List to store the collected data
    data = []
    
    # Regex pattern to extract data from the last line
    # Matches: %%optimality:SAT gap:41.35... time:4612
    line_pattern = re.compile(r"%%optimality:(?P<status>\w+)\s+gap:(?P<gap>[\d\.]+)\s+time:(?P<time>[\d\.]+)")

    # specific allowed algorithms (optional validation)
    allowed_algos = {'acs', 'ddo', 'astar'}

    # Iterate over all files in the folder
    for filename in os.listdir(folder_path):
        if not filename.endswith(".txt"):
            continue

        # 1. Parse Filename
        # format: algorithm_instance_name.txt
        parts = filename.split('_', 1)
        
        if len(parts) < 2:
            print(f"Skipping file with unexpected name format: {filename}")
            continue
            
        algo = parts[0]
        instance_part = parts[1]
        
        # Remove the .txt extension to get the clean instance name
        instance = instance_part.rsplit('.', 1)[0]

        # Optional: Skip if the prefix isn't one of your known algorithms
        if algo not in allowed_algos:
            # You can comment this out if you have other algorithms
            pass 

        # 2. Parse File Content
        file_path = os.path.join(folder_path, filename)
        
        try:
            with open(file_path, 'r') as f:
                lines = f.readlines()
                
                if not lines:
                    print(f"Warning: File is empty - {filename}")
                    continue
                
                # We only care about the last line
                last_line = lines[-1].strip()
                
                match = line_pattern.search(last_line)
                
                if match:
                    # Extract values
                    status = match.group("status")
                    gap = float(match.group("gap"))
                    time_val = float(match.group("time"))
                    
                    # Store in our list
                    data.append({
                        "Instance": instance,
                        "Algorithm": algo,
                        "Status": status,
                        "Gap": gap,
                        "Time": time_val
                    })
                else:
                    print(f"Warning: Last line format not recognized in {filename}")
                    print(f"Line was: {last_line}")

        except Exception as e:
            print(f"Error processing {filename}: {e}")

    # Convert list of dicts to a DataFrame for easy viewing/saving
    df = pd.DataFrame(data)
    
    # Sort for better readability (by Instance then Algorithm)
    if not df.empty:
        df = df.sort_values(by=['Instance', 'Algorithm'])
        
    return df

import os
import argparse

# --- ARGUMENT PARSING ---

parser = argparse.ArgumentParser(description="Parse result files")
parser.add_argument(
    "folder",
    help="Path to the folder containing result files"
)

args = parser.parse_args()
folder_path = args.folder

# --- EXECUTION ---

if os.path.exists(folder_path):
    df_results = parse_result_files(folder_path)

    if not df_results.empty:
        print("--- parsing complete ---")
        print(df_results)

        # Optional: Save to CSV
        # df_results.to_csv("consolidated_results.csv", index=False)
        # print("\nSaved to consolidated_results.csv")
    else:
        print("No valid data found.")
else:
    print(f"The folder '{folder_path}' does not exist.")