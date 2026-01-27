import argparse
import os
import sys
from itertools import cycle

import matplotlib

matplotlib.use("Qt5Agg")

import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import numpy as np
import pandas as pd

# --- Font Size Configuration ---
FS_TITLE = 16
FS_AXIS_LABEL = 14
FS_TICK_LABEL = 12
FS_LEGEND = 12

# --- Argument Parsing ---
parser = argparse.ArgumentParser(
    description="Generate clean performance profiles with an extra tick at the end (no minor ticks).")
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
fig, (ax1, ax2) = plt.subplots(1, 2, sharey=True, figsize=(14, 7))
plt.subplots_adjust(wspace=0.1)

colors = cycle(['orange', 'green', 'red', 'purple', 'blue'])
linestyles = cycle(['-', '--', ':', '-.'])

algo_styles = {}
for algo in algos:
    algo_styles[algo] = {
        'color': next(colors),
        'linestyle': next(linestyles)
    }

max_instances = df.groupby('Algorithm').size().max()


# --- HELPER: Calculate Extended Limits ---
def get_extended_limit(max_value):
    """
    Calculates a new limit that includes one extra major tick step.
    Example: If max is 5 and step is 1, returns 6.
    """
    if max_value == 0:
        return 1.0

    # Simulate matplotlib's default tick choice
    locator = ticker.MaxNLocator(nbins='auto')
    ticks = locator.tick_values(0, max_value)

    if len(ticks) < 2:
        return max_value * 1.1

    step = ticks[1] - ticks[0]
    current_last_tick = ticks[-1]

    # Ensure we cover the max value
    while current_last_tick < max_value:
        current_last_tick += step

    # Add the EXTRA tick requested
    new_limit = current_last_tick + step

    return new_limit


# 1. Determine raw max values
raw_max_time = df['Time'].max()
raw_max_gap = df['Gap'].max()
if raw_max_gap == 0: raw_max_gap = 1.0

# 2. Calculate the EXTENDED limits
extended_max_time = get_extended_limit(raw_max_time)
extended_max_gap = get_extended_limit(raw_max_gap)

solved_percentages = {}


# --- HELPER: Setup Ticks (Clean Version) ---
def setup_axis_ticks(ax):
    """
    Configures standard grid and font sizes without minor ticks.
    """
    # Only major grid
    ax.grid(True, which='major', linestyle='-', alpha=0.6, linewidth=1)

    # Font sizes for ticks
    ax.tick_params(axis='both', which='major', labelsize=FS_TICK_LABEL)


# --- PLOT 1: Cactus Plot (Time) ---
print("--- Plotting Time Profile ---")

for algo in algos:
    subset = df[(df['Algorithm'] == algo) & (df['Gap'] == 0)].copy()
    style = algo_styles[algo]

    if subset.empty:
        solved_percentages[algo] = 0.0
        ax1.hlines(y=0, xmin=0, xmax=extended_max_time,
                   color=style['color'], linestyle=style['linestyle'], linewidth=2, label=algo)
        continue

    subset = subset.sort_values(by='Time')
    x = subset['Time'].values
    x = np.insert(x, 0, 0)
    y = np.arange(0, len(subset) + 1) / max_instances * 100

    # Extend to the new limit
    if x[-1] < extended_max_time:
        x = np.append(x, extended_max_time)
        y = np.append(y, y[-1])

    ax1.step(x, y, where='post', label=algo,
             color=style['color'], linestyle=style['linestyle'], linewidth=2)
    solved_percentages[algo] = y[-1]

ax1.set_xlabel('Completion time (s)', fontsize=FS_AXIS_LABEL)
ax1.set_ylabel('Percentage of Instances (%)', fontsize=FS_AXIS_LABEL)
ax1.set_title('Solved Instances Profile (Gap = 0)', fontsize=FS_TITLE)
ax1.set_xlim(left=0, right=extended_max_time)
ax1.legend(fontsize=FS_LEGEND)
setup_axis_ticks(ax1)

# --- PLOT 2: Gap Profile ---
print("--- Plotting Gap Profile ---")

for algo in algos:
    subset = df[(df['Algorithm'] == algo) & (df['Gap'] > 0)].copy()
    style = algo_styles[algo]
    start_percentage = solved_percentages.get(algo, 0.0)

    if subset.empty:
        ax2.hlines(y=start_percentage, xmin=0, xmax=extended_max_gap,
                   color=style['color'], linestyle=style['linestyle'], linewidth=2, label=algo)
        continue

    subset = subset.sort_values(by='Gap')
    x = subset['Gap'].values
    x = np.insert(x, 0, 0)
    additional_percentage = np.arange(0, len(subset) + 1) / max_instances * 100
    y = start_percentage + additional_percentage

    # Extend to the new limit
    if x[-1] < extended_max_gap:
        x = np.append(x, extended_max_gap)
        y = np.append(y, y[-1])

    ax2.step(x, y, where='post', label=algo,
             color=style['color'], linestyle=style['linestyle'], linewidth=2)

ax2.set_xlabel('Optimality Gap Threshold (%)', fontsize=FS_AXIS_LABEL)
ax2.set_title('Gap Profile (Continuous)', fontsize=FS_TITLE)
ax2.set_xlim(left=0, right=extended_max_gap)
ax2.set_ylim(0, 105)
setup_axis_ticks(ax2)

# --- Save Output ---
filename = os.path.basename(data_file_path)
suffix = "_consolidated_results.csv"
png_filename = f"{filename.replace(suffix, "")}_cactus_plot.png"
plt.savefig(png_filename, transparent=True, dpi=600)
print(f"Graph saved to: {png_filename}")
plt.show()
