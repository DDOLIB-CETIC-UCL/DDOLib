package org.ddolib.examples.qks;

import com.gurobi.gurobi.*;

import java.io.FileWriter;
import java.io.IOException;

public class QKSILP {

    protected static double solveQuadraticKnapsack(QKSProblem instance) {

        try {
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.set("OutputFlag", "0");
            env.start();

            GRBModel model = new GRBModel(env);
            int n = instance.nbVars();

            GRBVar[] x = new GRBVar[instance.nbVars()];
            for (int i = 0; i < x.length; i++) {
                x[i] = model.addVar(0, 1, 0.0, GRB.BINARY, "X_" + i);
            }

            GRBVar[] y = new GRBVar[instance.nbVars()*instance.nbVars()];
            for (int i = 0; i < y.length; i++) {
                y[i] = model.addVar(0, 1, 0.0, GRB.BINARY, "Y_" + i);
            }

            GRBLinExpr constraint = new GRBLinExpr();
            for (int i = 0; i < instance.nbVars(); i++) {
                constraint.addTerm(instance.weights[i], x[i]);
            }
            model.addConstr(constraint, GRB.LESS_EQUAL, instance.capacity, "capacity");

            for (int i = 0; i < instance.nbVars(); i++) {
                for (int j = 0; j < instance.nbVars(); j++) {
                    int yIndex = i*n + j;

                    constraint = new GRBLinExpr();
                    constraint.addTerm(-1, y[yIndex]);
                    constraint.addTerm(1, x[i]);
                    model.addConstr(constraint, GRB.GREATER_EQUAL, 0, "y_"+i+"_"+j+"_0");

                    constraint = new GRBLinExpr();
                    constraint.addTerm(-1, y[yIndex]);
                    constraint.addTerm(1, x[j]);
                    model.addConstr(constraint, GRB.GREATER_EQUAL, 0, "y_"+i+"_"+j+"_1");

                    constraint = new GRBLinExpr();
                    constraint.addTerm(-1, y[yIndex]);
                    constraint.addTerm(1, x[j]);
                    constraint.addTerm(1, x[i]);
                    model.addConstr(constraint, GRB.LESS_EQUAL, 1, "y_"+i+"_"+j+"_2");
                }
            }

            GRBLinExpr objective = new GRBLinExpr();
            for (int i = 0; i < instance.nbVars(); i++) {
                for (int j = 0; j < instance.nbVars(); j++) {
                    objective.addTerm(instance.profitMatrix[i][j], y[i*n+j]);
                }

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
        QKSProblem[] instances = QKSXPs.loadInstances();
        StringBuilder outputs = new StringBuilder();
        outputs.append("Instance;Objective\n");
        for (QKSProblem instance : instances) {
            System.out.println(instance.name);
            double optimal = solveQuadraticKnapsack(instance);
            outputs.append(instance.name.get() + ";" + optimal + "\n");
        }
        FileWriter writer = new FileWriter("xps/optimalQKS.csv");
        writer.write(outputs.toString());
        writer.close();
    }
    
}
