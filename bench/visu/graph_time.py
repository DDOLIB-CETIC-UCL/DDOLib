import argparse
import os

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

# --- ARGUMENT PARSING ---

parser = argparse.ArgumentParser(description="Parse result files")
parser.add_argument(
    "data_file",
    help="Path to the folder containing result files"
)

args = parser.parse_args()
data_file_path = args.data_file

if os.path.exists(data_file_path):
    df = pd.read_csv(data_file_path)
    plt.figure(figsize=(12, 8))

    sns.barplot(data=df, x='Instance', y='Time', hue='Algorithm')

    plt.xticks(rotation=90)
    plt.title('Time for each instance by algorithm')
    plt.xlabel('Instance')
    plt.ylabel('Time (s)')
    plt.tight_layout()

    plt.savefig('graph_time.png')  # save image
