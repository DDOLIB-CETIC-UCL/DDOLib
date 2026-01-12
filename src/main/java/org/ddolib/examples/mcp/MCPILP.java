package org.ddolib.examples.mcp;

import com.gurobi.gurobi.*;
import org.ddolib.examples.maximumcoverage.MaxCoverILP;
import org.ddolib.examples.maximumcoverage.MaxCoverProblem;
import org.ddolib.examples.maximumcoverage.MaxCoverXPs;

import java.io.FileWriter;
import java.io.IOException;

public class MCPILP {

    protected static double solveMCP(MCPProblem instance) {
        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.set("OutputFlag", "0");
            env.start();

            GRBModel model = new GRBModel(env);
            int n = instance.graph.numNodes;

            GRBVar[] vertexVar = new GRBVar[instance.graph.numNodes];
            for (int i = 0; i < vertexVar.length; i++) {
                // vertexVar[i] = 0 -> i is on left side, vertexVar[i] = 1 -> i is on right side,
                vertexVar[i] = model.addVar(0, 1, 0.0, GRB.BINARY, "VERTEX_" + i);
            }

            GRBVar[] edgeVar = new GRBVar[instance.graph.numNodes*instance.graph.numNodes];
            for (int i = 0; i < edgeVar.length; i++) {
                // edgeVar[i] = 1 <=> edge i is on the cut
                if (instance.graph.weightOf(i/n,i%n) != 0) {
                    // System.out.println(i/n + ", " + i%n);
                    edgeVar[i] = model.addVar(0, 1, 0.0, GRB.BINARY, "EDGE_" + i);
                }
            }

            GRBLinExpr constraint;
            for (int i = 0; i < edgeVar.length; i++) {
                if (instance.graph.weightOf(i/n,i%n) != 0) {
                    /*System.out.println("************");
                    System.out.println(i/n);
                    System.out.println(i%n);
                    System.out.println(instance.graph.weightOf(i/n, i%n));*/
                    // y_ij <= x_i + x_j
                    // if i and j are on 0 => y_ij = 0
                    constraint = new GRBLinExpr();
                    constraint.addTerm(1.0, edgeVar[i]);
                    constraint.addTerm(-1.0, vertexVar[i/n]);
                    constraint.addTerm(-1.0, vertexVar[i%n]);
                    model.addConstr(constraint, GRB.LESS_EQUAL, 0, "0_" + i);

                    // y_ij <= 2 - (x_i + x_j)
                    // if i and j are on 1 => y_ij = 0
                    constraint = new GRBLinExpr();
                    constraint.addTerm(1.0, edgeVar[i]);
                    constraint.addTerm(1.0, vertexVar[i/n]);
                    constraint.addTerm(1.0, vertexVar[i%n]);
                    model.addConstr(constraint, GRB.LESS_EQUAL, 2, "1_" + i);

                    constraint = new GRBLinExpr();
                    constraint.addTerm(1.0, edgeVar[i]);
                    constraint.addTerm(-1.0, vertexVar[i/n]);
                    constraint.addTerm(1.0, vertexVar[i%n]);
                    model.addConstr(constraint, GRB.GREATER_EQUAL, 0, "2_" + i);

                    constraint = new GRBLinExpr();
                    constraint.addTerm(1.0, edgeVar[i]);
                    constraint.addTerm(1.0, vertexVar[i/n]);
                    constraint.addTerm(-1.0, vertexVar[i%n]);
                    model.addConstr(constraint, GRB.GREATER_EQUAL, 0, "3_" + i);
                }
            }

            GRBLinExpr objective = new GRBLinExpr();
            for (int i = 0; i < edgeVar.length; i++) {
                if (instance.graph.weightOf(i/n,i%n) != 0) {
                    objective.addTerm(instance.graph.weightOf(i/n, i%n), edgeVar[i]);
                }
            }
            model.setObjective(objective, GRB.MAXIMIZE);
            model.optimize();

            return model.get(GRB.DoubleAttr.ObjVal) / 2; // edges weights are counted twice in the objective

        } catch (GRBException e) {
            System.err.println(e.getMessage());
        }

        return 0;
    }

    public static void main(String[] args) throws IOException {

        // MCPProblem[] instances = MCPXPs.loadInstances();
        MCPProblem[] instances = new MCPProblem[2];
        instances[0] = new MCPProblem("data/MCP/mcp_100.txt");
        // StringBuilder outputs = new StringBuilder();
        // outputs.append("Instance;Objective\n");
        try {
            FileWriter writer = new FileWriter("xps/optimalMCP.csv");
            writer.write("Instance;Objective\n");
            for (MCPProblem instance : instances) {
                System.out.println(instance);
                double optimal = solveMCP(instance);
                writer.write(instance.name.get() + ";" + optimal + "\n");
                writer.flush();
            }
            // writer.write(outputs.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

}
