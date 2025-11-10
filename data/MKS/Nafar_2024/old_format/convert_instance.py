from os import listdir
from os.path import join

prefix = "old_format/"
for filename in listdir(prefix):
    if filename == "convert_instance.py":
        continue
    print(f'Converting {filename}...')
    n_item = -1
    capacity = -1
    weights = []
    profits = []

    with open(join(prefix, filename), 'r') as f:
        lines = f.readlines()

    n_item, dimensions, optimal = map(int, lines[-1].strip().split("\t"))
    capacities = [int(x) for x in lines[-2].strip().split(" ")]
    profits = [float(x) for x in lines[-3].strip().split(" ")]

    weights = [[] for _ in range(n_item)]
    for i in range(dimensions):
        for index, weight in enumerate(lines[i].strip().split(" ")):
            weights[index].append(int(weight))

    with open(filename, 'w') as f:
        f.write(f"{n_item} {dimensions} {optimal}\n")
        for capa in capacities:
            f.write(f"{capa} ")
        f.write("\n")
        for i in range(n_item):
            f.write(f"{int(profits[i])} ")
            for weight in weights[i]:
                f.write(f"{weight} ")
            f.write("\n")

    print(f'Converted {filename} to new format.')
print("All files converted.")
print("Done.")