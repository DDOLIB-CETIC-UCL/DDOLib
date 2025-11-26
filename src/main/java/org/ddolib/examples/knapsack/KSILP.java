package org.ddolib.examples.knapsack;
import com.gurobi.gurobi.*;
import org.ddolib.examples.maximumcoverage.MaxCoverProblem;
import org.ddolib.examples.maximumcoverage.MaxCoverXPs;

import java.io.FileWriter;
import java.io.IOException;

public class KSILP {

    protected static double solveKnapsack(KSProblem instance) {

        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.set("OutputFlag", "0");
            env.start();

            GRBModel model = new GRBModel(env);

            GRBVar[] x = new GRBVar[instance.nbVars()];
            for (int i = 0; i < x.length; i++) {
                x[i] = model.addVar(0, 1, 0.0, GRB.BINARY, "X_" + i);
            }

            GRBLinExpr constraint = new GRBLinExpr();
            for (int i = 0; i < instance.nbVars(); i++) {
                constraint.addTerm(instance.weight[i], x[i]);
            }
            model.addConstr(constraint, GRB.LESS_EQUAL, instance.capa, "capacity");

            GRBLinExpr objective = new GRBLinExpr();
            for (int i = 0; i < instance.nbVars(); i++) {
                objective.addTerm(instance.profit[i], x[i]);
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
        KSProblem[] instances = KSXPs.loadInstances();
        StringBuilder outputs = new StringBuilder();
        outputs.append("Instance;Objective\n");
        for (KSProblem instance : instances) {
            System.out.println(instance);
            double optimal = solveKnapsack(instance);
            outputs.append(instance.name.get() + ";" + optimal + "\n");
        }
            FileWriter writer = new FileWriter("xps/optimalKS.csv");
            writer.write(outputs.toString());
            writer.close();
    }

}
