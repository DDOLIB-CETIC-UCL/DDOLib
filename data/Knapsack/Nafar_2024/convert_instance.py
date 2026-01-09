from os import listdir
from os.path import join

prefix = "old_format/"
for filename in listdir(prefix):
    print(f'Converting {filename}...')
    n_item = -1
    capacity = -1
    weights = []
    profits = []

    with open(join(prefix, filename), 'r') as f:
        lines = f.readlines()
        n_item, capacity, optimal = map(int, lines[2].strip().split(" "))
        weights = [int(x) for x in lines[1].strip().split(" ")]
        profits = [int(x) for x in lines[0].strip().split(" ")]

    with open(filename, 'w') as f:
        f.write(f"{n_item} {capacity} {optimal}\n")
        for i in range(n_item):
            f.write(f"{profits[i]} {weights[i]}\n")
    print(f'Converted {filename} to new format.')
print("All files converted.")
print("Done.")