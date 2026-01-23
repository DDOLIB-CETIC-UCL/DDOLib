import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

# Create dummy data
data = []
algos = ['acs', 'ddo', 'astar']
n_instances = 50

for i in range(n_instances):
    for algo in algos:
        # Randomly decide result
        # Scenario: ACS is fast but sometimes has gaps. DDO is slow but optimal.
        if algo == 'acs':
            gap = 0 if np.random.rand() > 0.5 else np.random.rand() * 50
            time_val = np.random.rand() * 1000
        else:
            gap = 0 if np.random.rand() > 0.2 else np.random.rand() * 10
            time_val = np.random.rand() * 5000

        data.append({
            "Instance": f"inst_{i}",
            "Algorithm": algo,
            "Gap": gap,
            "Time": time_val
        })

df = pd.DataFrame(data)

# --- Plotting Logic ---
fig, (ax1, ax2) = plt.subplots(1, 2, sharey=True, figsize=(12, 6))
# Remove space between plots
plt.subplots_adjust(wspace=0.05)

# Left Plot: Solved over Time
for algo in algos:
    subset = df[(df['Algorithm'] == algo) & (df['Gap'] == 0.0)].copy()
    if subset.empty: continue
    subset = subset.sort_values(by='Time')
    x = subset['Time'].values
    y = np.arange(1, len(subset) + 1)
    # Append a starting point? Optional, usually standard plots just start at first point
    ax1.plot(x, y, label=algo, marker='.', linestyle='-')

ax1.set_xlabel('Runtime (s)')
ax1.set_ylabel('Cumulative Instances')
ax1.set_title('Solved vs Time')
ax1.grid(True)

# Right Plot: Gap Profile
for algo in algos:
    subset = df[df['Algorithm'] == algo].copy()
    if subset.empty: continue
    subset = subset.sort_values(by='Gap')
    x = subset['Gap'].values
    y = np.arange(1, len(subset) + 1)
    ax2.plot(x, y, label=algo, marker='.', linestyle='-')

ax2.set_xlabel('Gap (%)')
# ax2.set_ylabel('Cumulative Instances') # Shared
ax2.set_title('Gap Profile')
ax2.grid(True)
ax2.legend()

plt.savefig('smic_combined_plot.png')
