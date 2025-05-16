

![Doc](https://github.com/DDOLIB-CETIC-UCL/DDOLib/actions/workflows/publish.yml/badge.svg)
![Coverage](https://github.com/DDOLIB-CETIC-UCL/DDOLib/actions/workflows/test.yml/badge.svg)
<!-- ![Test coverage](https://raw.githubusercontent.com/<username>/<repository>/badges/badges/<branch>/badge.svg) -->


# DDOLib

DDOLib is a java solver for solving dynamic programming (DP) problems developped 
by the [CETIC](https://www.cetic.be/) (team of [Renaud De Landtsheer](https://www.cetic.be/Renaud-DE-LANDTSHEER)) and the [UCLouvain](https://uclouvain.be/en/index.html) (team of [Pierre Schaus](https://pschaus.github.io/)).
It includes a modeling API for users to define their DP problem and solve them efficiently.


# Theoretical Foundations

The technique of using decision diagrams for solving combinatorial problems is based on the work of several researchers.

The first paper that introduced the concept of using decision diagrams for combinatorial optimization is:

*Bergman, D., Cire, A. A., Van Hoeve, W. J., & Hooker, J. N. (2016). Discrete optimization with decision diagrams. INFORMS Journal on Computing.*

Then the technique was improved in several papers that are also implemented in this project or will eventually be:

* *Coppé, V., Gillard, X., & Schaus, P. (2024). Decision diagram-based branch-and-bound with caching for dominance and suboptimality detection. INFORMS Journal on Computing.*
* *Coppé, V., Gillard, X., & Schaus, P. (2024). Modeling and Exploiting Dominance Rules for Discrete Optimization with Decision Diagrams. CPAIOR.*
* *Gillard, X., & Schaus, P. (2022). Large Neighborhood Search with Decision Diagrams. In IJCAI.*
* *Gillard, X., Coppé, V., Schaus, P., & Cire, A. A. (2021). Improving the filtering of branch-and-bound MDD solver. CPAIOR*
* *Gillard, X., Schaus, P., & Coppé, V. (2014). Ddo, a generic and efficient framework for mdd-based optimization. IJCAI.*
# At the origin: The DDO Project

DDOLib is a Java version of DDO, which was originally implemented in Rust and is available [here](https://github.com/xgillard/ddo).
If you use this Java version, please cite the DDO paper:

```bibtex
@inproceedings{gillard2014ddo,
  title={Ddo, a generic and efficient framework for mdd-based optimization},
  author={Gillard, X. and Schaus, P. and Copp{\'e}, V.},
  booktitle={IJCAI},
  year={2014}
}
```

## Examples

The project contains two sets of example models located in the example package.

The Knapsack problem is a classic optimization problem where the goal is to maximize the total value of items while staying within a given weight limit. The dynamic programming model used to solve this problem uses the recurrence relation:

`KS(i, c) = max(KS(i-1, c), KS(i-1, c - w[i]) + p[i])`

where `KS(i, c)` is the maximum value of the first `i` items with a knapsack capacity of ``c, `p[i]` is the profit of item `i`, and `w[i]` is the weight of item `i`.

For modeling this problem in DDOLib, we define the `KSProblem` class that implements the `Problem` interface. 
This interface requires to define a state. This state is the remaining capacity of the knapsack (an Integer).
We also define the `initialState` method to return the initial capacity of the knapsack, and the `initialValue` method to return 0 as the initial value of the knapsack.
The `nbVars` method returns the number of items, which is the same as the number of variables in the problem.
The `domain` method defines the possible decisions for each item (take or not take), and the `transition` method updates the state of the knapsack based on the decision made.
The `transitionCost` method returns the profit of the item if it is taken, and 0 otherwise.
This implicitely define a decision diagram where the optimal solution is the longest path from the root to a leaf node.

```java
/**
 * The state is the remaining capacity of the knapsack (an Integer thus).
 * The decision is the item to take or not (1 or 0).
 */
public class KSProblem implements Problem<Integer> {

   final int capa;
   final int[] profit;
   final int[] weight;
   public final Integer optimal;

   public KSProblem(final int capa, final int[] profit, final int[] weight, final Integer optimal) {
      this.capa = capa;
      this.profit = profit;
      this.weight = weight;
      this.optimal = optimal;
   }

   @Override
   public int nbVars() {
      return profit.length;
   }

   @Override
   public Integer initialState() {
      return capa;
   }

   @Override
   public int initialValue() {
      return 0;
   }

   @Override
   public Iterator<Integer> domain(Integer state, int var) {
      if (state >= weight[var]) { // The item can be taken or not
         return Arrays.asList(1, 0).iterator();
      } else { // The item cannot be taken
         return List.of(0).iterator();
      }
   }

   @Override
   public Integer transition(Integer state, Decision decision) {
      // If the item is taken (1), we decrease the capacity of the knapsack, otherwise leave it unchanged
      return state - weight[decision.var()] * decision.val();
   }

   @Override
   public int transitionCost(Integer state, Decision decision) {
      // If the item is taken (1) the cost is the profit of the item, 0 otherwise
      return profit[decision.var()] * decision.val();
   }
}
```


### Recommended IDE: IntelliJ IDEA

We recommend using **IntelliJ IDEA** to develop and run the DDOLib project.

#### Steps to Import DDOLib into IntelliJ:

1. **Clone the Repository**:
   Open a terminal and run the following command to clone the repository:
   ```bash
   git clone https://github.com/DDOLIB-CETIC-UCL/DDOLib
    ```

2. **Clone the Repository**:
   Launch IntelliJ IDEA.
   Select File > Open and navigate to the maxicp folder you cloned. 
   Open the `pom.xml` file.

3. **Running the tests**:

    From the IntelliJ IDEA editor, navigate to the `src/test/java` directory.
    Right-click then select `Run 'All Tests'` to run all the tests.

    From the terminal, navigate to the root directory of the project and run the following command:
    ```bash
    mvn test
    ```


## Acknowledgments

This project is funded by the Walloon Region (Belgium) as part of the Win4Collective project Convention 2410118. 
We are grateful for their support and contribution to the development of this project.



