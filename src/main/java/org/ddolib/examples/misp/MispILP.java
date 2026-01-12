package org.ddolib.examples.misp;

import com.gurobi.gurobi.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

public class MispILP {

    protected static double solveMisp(MispProblem instance) {
        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.set("OutputFlag", "0");
            env.start();

            GRBModel model = new GRBModel(env);
            int n = instance.weight.length;

            GRBVar[] vertexVars = new GRBVar[n];
            for (int i = 0; i < n; i++) {
                vertexVars[i] = model.addVar(0, 1, 0, GRB.BINARY, "node_" + i);
            }

            GRBLinExpr constraint;
            for (int node = 0; node < n; node++) {
                BitSet neighbors = instance.neighbors[node];
                for (int neighbor = neighbors.nextSetBit(node + 1); neighbor != -1; neighbor = neighbors.nextSetBit(neighbor + 1)) {
                    constraint = new GRBLinExpr();
                    constraint.addTerm(1.0, vertexVars[node]);
                    constraint.addTerm(1.0, vertexVars[neighbor]);
                    model.addConstr(constraint, GRB.LESS_EQUAL, 1, node + "_" + neighbor);
                }
            }

            GRBLinExpr objective = new GRBLinExpr();
            for (int node = 0; node < n; node++) {
                objective.addTerm(instance.weight[node], vertexVars[node]);
            }
            model.setObjective(objective, GRB.MAXIMIZE);
            model.optimize();

            return model.get(GRB.DoubleAttr.ObjVal);

        } catch (GRBException e) {
            System.err.println(e.getMessage());
        }
        return 0.0;
    }

    public static void main(String[] args) throws IOException {
        MispProblem[] instances = MispXPs.loadInstances();
        StringBuilder outputs = new StringBuilder();
        outputs.append("Instance;Objective\n");
        for (MispProblem instance : instances) {
            System.out.println(instance);
            double optimal = solveMisp(instance);
            outputs.append(instance.name.get()).append(";").append(optimal).append("\n");
        }
        try {
            FileWriter writer = new FileWriter("xps/optimalMisp.csv");
            writer.write(outputs.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
