import sys

def write_instance(prefix, instance_index, n, m, optimal, prices, weights, constraints):
    with open(f"{prefix}_{instance_index}.txt", 'w') as f:
        f.write(f"{n} {m} {optimal}\n")
        for capa in constraints:
            f.write(f"{capa} ")
        f.write("\n")
        for i in range(n):
            f.write(f"{int(prices[i])} ")
            for weight in weights:
                f.write(f"{weight[i]} ")
            f.write("\n")

with open(sys.argv[1], 'r') as file:
    lines = file.readlines()

prefix_name = sys.argv[1].split('.')[0].split('/')[-1]

nbr_instances = int(lines[0].strip())
instance_num = 1
context = "init"
for line in lines[1:]:
    if context == "init":
        split_line = line.strip().split()
        if len(split_line) != 3:
            exit("Error: first line of instance must contain 3 integers")
        n, m, optimal = map(int, split_line)
        context = "prices"
        prices = []
        weights = [[]]
        constraints = []
    elif context == "prices":
        prices += [int(i) for i in line.strip().split()]
        if len(prices) == n:
            context = "weights"
    elif context == "weights":
        weights[-1] += [int(i) for i in line.strip().split()]
        if len(weights[-1]) == n and len(weights) < m:
            weights.append([])
        elif len(weights[-1]) == n and len(weights) == m:
            context = "constraints"
    elif context == "constraints":
        constraints += [int(i) for i in line.strip().split()]
        if len(constraints) == m:
            context = "init"
            write_instance(prefix_name,instance_num, n, m, optimal, prices, weights, constraints)
            instance_num += 1
            
