import numpy as np

def generate_number_of_actors(n_value, avg):
    # sample x values from a normal distribution with mean y and std sqrt(y)
    sizes = np.random.normal(loc=avg, scale=np.sqrt(3), size=n_value)
    # round to nearest integer and ensure non-negative integer sizes
    sizes = np.rint(sizes).astype(int)
    sizes = np.maximum(sizes, 1)
    return sizes

def generate_instance(n: int, m: int, p: float, k: int):
    avg_actors_per_scene = p * m
    groups = generate_number_of_actors(k, avg_actors_per_scene)
    costs = [np.random.randint(1, 100) for _ in range(m)]
    groupped_requirements = [np.random.randint(m, size=groups[i]) for i in groups]
    requirements = []

    for i in range(n):
        requirements.append(groupped_requirements[i % k])

    return costs, requirements

def instance_string(n: int, m:int, requirements: list, costs: list, name:str) -> str:
    output = f"{name}\n"
    output += f"{n}\n"
    output += f"{m}\n\n"
    for actor in range(m):
        for scene in requirements:
            if actor in scene:
                output += "1 "
            else:
                output += "0 "
        output += f"     {costs[actor]}\n"

    output += "\n"
    for i in range(n):
        output += f"{len(requirements[i])} "
    output += "\n"
    return output


if __name__ == "__main__":

    ns = [22, 24, 26, 28]
    ms = [10, 15]
    ps = [0.3, 0.4]
    ks = [15, 20, 25] 
    for n in ns:
        for m in ms:
            for p in ps:
                for k in ks:
                    filename = f"instance_{n}_{m}_{int(p*100)}_{k}.txt"
                    costs, requirements= generate_instance(n, m, p, k)
                    instance_str = instance_string(n, m, requirements, costs, filename)
                    with open(filename, "w") as f:
                        f.write(instance_str)
                        f.close()


