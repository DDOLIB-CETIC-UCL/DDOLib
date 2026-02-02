package org.ddolib.examples.setcover;
import com.gurobi.gurobi.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class SetCoverILP {

    protected static double solveSetCover(SetCoverProblem instance) {
        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.set("OutputFlag", "1");
            env.start();

            GRBModel model = new GRBModel(env);

            GRBVar[] setsVar = new GRBVar[instance.nSet];
            for (int i = 0; i < setsVar.length; i++) {
                setsVar[i] = model.addVar(0, 1, 0.0, GRB.BINARY, "SET_" + i);
            }

            GRBLinExpr coverConstraint;
            for (int i = 0; i < instance.nItems; i++) {
                coverConstraint = new GRBLinExpr();
                for (int set: instance.constraints.get(i)) {
                    coverConstraint.addTerm(1, setsVar[set]);
                }
                model.addConstr(coverConstraint, GRB.GREATER_EQUAL, 1, "cover_" + i);
            }
            System.out.println();

            GRBLinExpr objective = new GRBLinExpr();
            for (int i = 0; i < instance.nSet; i++) {
                objective.addTerm(instance.weights[i], setsVar[i]);
            }
            model.setObjective(objective, GRB.MINIMIZE);
            model.optimize();

            return model.get(GRB.DoubleAttr.ObjVal);


        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }

        return 0.0;
    }

    private static void unweighted() throws IOException {
        SetCoverProblem[] instances = SetCoverXPs.loadInstances();
        StringBuilder outputs = new StringBuilder();
        outputs.append("Instance;Objective\n");
        for (SetCoverProblem instance : instances) {
            System.out.println(instance.name);
            double optimal = solveSetCover(instance);
            outputs.append(instance.name.get() + ";" + optimal + "\n");
        }
        try {
            FileWriter writer = new FileWriter("xps/optimalSetCover.csv");
            writer.write(outputs.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void weighted() throws IOException {
        SetCoverProblem[] instances = SetCoverXPs.loadWeightedInstances();
        StringBuilder outputs = new StringBuilder();
        outputs.append("Instance;Objective\n");
        for (SetCoverProblem instance : instances) {
            System.out.println(instance.name);
            double optimal = solveSetCover(instance);
            outputs.append(instance.name.get() + ";" + optimal + "\n");
        }
        try {
            FileWriter writer = new FileWriter("xps/optimalWeightedSetCover.csv");
            writer.write(outputs.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        weighted();
        unweighted();
    }

}
