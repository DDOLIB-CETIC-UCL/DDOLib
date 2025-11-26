package org.ddolib.examples.maximumcoverage;
import com.gurobi.gurobi.*;

import java.io.FileWriter;
import java.io.IOException;

public class MaxCoverILP {
    
    protected static double solveMaxCover(MaxCoverProblem instance) {

        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.set("OutputFlag", "0");
            env.start();

            GRBModel model = new GRBModel(env);

            GRBVar[] setsVar = new GRBVar[instance.nbSubSets];
            for (int i = 0; i < setsVar.length; i++) {
                setsVar[i] = model.addVar(0, 1, 0.0, GRB.BINARY, "SET_" + i);
            }

            GRBVar[] coveredItemsVar = new GRBVar[instance.nbItems];
            for (int i = 0; i < coveredItemsVar.length; i++) {
                coveredItemsVar[i] = model.addVar(0, 1, 0.0, GRB.BINARY, "ITEM_" + i);
            }

            GRBLinExpr coverConstraint;
            for (int i = 0; i < coveredItemsVar.length; i++) {
                coverConstraint = new GRBLinExpr();
                coverConstraint.addTerm(-1, coveredItemsVar[i]);
                for (int j = 0; j < setsVar.length; j++) {
                    if (instance.subSets[j].get(i)) {
                        coverConstraint.addTerm(1, setsVar[j]);
                    }
                }
                model.addConstr(coverConstraint, GRB.GREATER_EQUAL, 0, "cover_i");
            }

            GRBLinExpr budgetConstraint = new GRBLinExpr();
            for (int i = 0; i < setsVar.length; i++) {
                budgetConstraint.addTerm(1, setsVar[i]);
            }
            model.addConstr(budgetConstraint, GRB.LESS_EQUAL, instance.nbSubSetsToChoose, "budget");

            GRBLinExpr objective = new GRBLinExpr();
            for (int i = 0; i < coveredItemsVar.length; i++) {
                objective.addTerm(1, coveredItemsVar[i]);
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

    public static void main(String[] args) {
        MaxCoverProblem[] instances = MaxCoverXPs.generateInstancesRestricted();
        StringBuilder outputs = new StringBuilder();
        outputs.append("Instance;Objective\n");
        for (MaxCoverProblem instance : instances) {
            System.out.println(instance);
            double optimal = solveMaxCover(instance);
            outputs.append(instance.name.get() + ";" + optimal + "\n");
        }
        try {
            FileWriter writer = new FileWriter("xps/optimalMaxCover.csv");
            writer.write(outputs.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
    
}
