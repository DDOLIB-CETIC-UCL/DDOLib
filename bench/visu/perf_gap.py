import argparse
import os
import sys

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

parser = argparse.ArgumentParser(description="Generate performance graphs from a CSV")
parser.add_argument(
    "data_file",
    help="Path to the CSV file containing results"
)

args = parser.parse_args()
data_file_path = args.data_file

if not os.path.exists(data_file_path):
    print(f"Error: The file '{data_file_path}' does not exist.")
    sys.exit(1)

df = pd.read_csv(data_file_path)

required_columns = ['Algorithm', 'Gap', 'Time']
if not all(col in df.columns for col in required_columns):
    print(f"Error: The CSV must contain columns: {required_columns}")
    sys.exit(1)

algos = df['Algorithm'].unique()

print(f"Data loaded successfully. Algorithms found: {algos}")

fig, (ax1, ax2) = plt.subplots(1, 2, sharey=True, figsize=(12, 6))
plt.subplots_adjust(wspace=0.05)

for algo in algos:
    subset = df[df['Algorithm'] == algo].copy()

    if subset.empty:
        continue

    subset = subset.sort_values(by='Time')
    x = subset['Time'].values
    y = np.arange(1, len(subset) + 1)

    ax1.plot(x, y, label=algo, marker='.', linestyle='-')

ax1.set_xlabel('Runtime (s)')
ax1.set_ylabel('Cumulative Instances')
ax1.set_title('Runtime Profile')
ax1.grid(True)
ax1.legend()

for algo in algos:
    subset = df[df['Algorithm'] == algo].copy()

    if subset.empty:
        continue

    subset = subset.sort_values(by='Gap')
    x = subset['Gap'].values
    y = np.arange(1, len(subset) + 1)
    ax2.plot(x, y, label=algo, marker='.', linestyle='-')

ax2.set_xlabel('Gap (%)')
ax2.set_title('Gap Profile')
ax2.grid(True)
ax2.legend()

output_filename = 'ks_combined_plot.png'
plt.savefig(output_filename, transparent=True)
print(f"Graph saved to: {output_filename}")
