import argparse
import os
import sys
from itertools import cycle

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

# --- Argument Parsing ---
parser = argparse.ArgumentParser(description="Generate split performance profiles with auto-scaled axes.")
parser.add_argument(
    "data_file",
    help="Path to the CSV file containing results"
)
args = parser.parse_args()

# --- File and Data Validation ---
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
print(f"Algorithms found: {algos}")

# --- Plot Configuration ---
fig, (ax1, ax2) = plt.subplots(1, 2, sharey=True, figsize=(12, 6))
plt.subplots_adjust(wspace=0.1)

# Style configuration
colors = cycle(['orange', 'green', 'red', 'blue', 'purple'])
linestyles = cycle(['-', '--', ':', '-.'])

algo_styles = {}
for algo in algos:
    algo_styles[algo] = {
        'color': next(colors),
        'linestyle': next(linestyles)
    }

max_instances = df.groupby('Algorithm').size().max()

# --- GLOBAL LIMITS ---
# 1. Max Time (for Left Graph extension)
global_max_time = df['Time'].max()

# 2. Max Gap (for Right Graph extension and limit)
# We find the max gap in the data. If it's 0, we default to a small value to avoid errors.
global_max_gap = df['Gap'].max()
if global_max_gap == 0:
    global_max_gap = 1.0  # Fallback if perfectly solved

print(f"Global Limits -> Max Time: {global_max_time}s, Max Gap: {global_max_gap}%")

solved_percentages = {}

# --- PLOT 1: Cactus Plot (Time) ---
# Filter: Gap == 0
print("--- Plotting Time Profile (Gap == 0) ---")

for algo in algos:
    subset = df[(df['Algorithm'] == algo) & (df['Gap'] == 0)].copy()
    style = algo_styles[algo]

    if subset.empty:
        solved_percentages[algo] = 0.0
        # Flat line at 0%
        ax1.hlines(y=0, xmin=0, xmax=global_max_time,
                   color=style['color'], linestyle=style['linestyle'], linewidth=2, label=algo)
        continue

    subset = subset.sort_values(by='Time')

    x = subset['Time'].values
    x = np.insert(x, 0, 0)
    y = np.arange(0, len(subset) + 1) / max_instances * 100

    # Extension to global_max_time
    if x[-1] < global_max_time:
        x = np.append(x, global_max_time)
        y = np.append(y, y[-1])

    ax1.step(x, y, where='post', label=algo,
             color=style['color'], linestyle=style['linestyle'], linewidth=2)

    solved_percentages[algo] = y[-1]

ax1.set_xlabel('Completion time (s)')
ax1.set_ylabel('Percentage of Instances (%)')
ax1.set_title('Solved Instances Profile (Gap = 0)')
ax1.grid(True, which='both', linestyle='-', alpha=0.6)
ax1.set_xlim(left=0, right=global_max_time)
ax1.legend()

# --- PLOT 2: Gap Profile ---
# Filter: Gap > 0
print("\n--- Plotting Gap Profile (Gap > 0) ---")

for algo in algos:
    subset = df[(df['Algorithm'] == algo) & (df['Gap'] > 0)].copy()
    style = algo_styles[algo]
    start_percentage = solved_percentages.get(algo, 0.0)

    if subset.empty:
        # Extend flat line only up to global_max_gap
        ax2.hlines(y=start_percentage, xmin=0, xmax=global_max_gap,
                   color=style['color'], linestyle=style['linestyle'], linewidth=2, label=algo)
        continue

    subset = subset.sort_values(by='Gap')

    x = subset['Gap'].values
    x = np.insert(x, 0, 0)

    additional_percentage = np.arange(0, len(subset) + 1) / max_instances * 100
    y = start_percentage + additional_percentage

    # Extension to global_max_gap (instead of 100%)
    if x[-1] < global_max_gap:
        x = np.append(x, global_max_gap)
        y = np.append(y, y[-1])

    ax2.step(x, y, where='post', label=algo,
             color=style['color'], linestyle=style['linestyle'], linewidth=2)

ax2.set_xlabel('Optimality Gap Threshold (%)')
ax2.set_title('Gap Profile (Continuous)')
ax2.grid(True, which='both', linestyle='-', alpha=0.6)
# Force the X-axis to stop at the max gap found
ax2.set_xlim(left=0, right=global_max_gap)
ax2.set_ylim(0, 105)

# --- Save Output ---
output_filename = 'ks_cactus_plot.png'
plt.savefig(output_filename, transparent=True, dpi=600)
print(f"\nGraph saved to: {output_filename}")
