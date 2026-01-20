package org.ddolib.examples.boundedknapsack;

import com.gurobi.gurobi.*;

import java.io.FileWriter;
import java.io.IOException;

public class BKSILP {

    protected static double solveMultiKnapsack(BKSProblem instance) {

        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.set("OutputFlag", "0");
            env.start();

            GRBModel model = new GRBModel(env);

            GRBVar[] x = new GRBVar[instance.nbVars()];
            for (int i = 0; i < x.length; i++) {
                x[i] = model.addVar(0, instance.quantities[i], 0.0, GRB.INTEGER, "X_" + i);
            }

            GRBLinExpr constraint = new GRBLinExpr();
            for (int i = 0; i < instance.nbVars(); i++) {
                constraint.addTerm(instance.weights[i], x[i]);
            }
            model.addConstr(constraint, GRB.LESS_EQUAL, instance.capacity, "capacity");

            GRBLinExpr objective = new GRBLinExpr();
            for (int i = 0; i < instance.nbVars(); i++) {
                objective.addTerm(instance.values[i], x[i]);
            }
            model.setObjective(objective, GRB.MAXIMIZE);
            model.optimize();

            return model.get(GRB.DoubleAttr.ObjVal);

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }

        return 0.0;
    }

    public static void main(String[] args) throws IOException {
        BKSProblem[] instances = BKSXPs.loadInstances();
        StringBuilder outputs = new StringBuilder();
        outputs.append("Instance;Objective\n");
        for (BKSProblem instance : instances) {
            System.out.println(instance.name);
            double optimal = solveMultiKnapsack(instance);
            outputs.append(instance.name.get() + ";" + optimal + "\n");
        }
        FileWriter writer = new FileWriter("xps/optimalBKS.csv");
        writer.write(outputs.toString());
        writer.close();
    }


}
