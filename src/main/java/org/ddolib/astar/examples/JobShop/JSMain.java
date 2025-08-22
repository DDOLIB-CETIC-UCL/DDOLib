package org.ddolib.astar.examples.JobShop;


import org.ddolib.astar.core.solver.LNSSolver;
import org.ddolib.astar.core.solver.LNSSolver2;
import org.ddolib.common.dominance.AstarDominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.apache.commons.cli.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.ddolib.factory.Solvers.*;

public class JSMain {

    public enum SearchType {
        Astar,
        ACS,
        LNS,
        SOA
    }
    private static final Map<String, SearchType> searchMap = new HashMap<>(){
        {
            put("Astar", SearchType.Astar);
            put("ACS", SearchType.ACS);
            put("LNS", SearchType.LNS);
            put("SOA", SearchType.SOA);
        }
    };

    public static void main(String args[]) {
        String quotedValidSearch = searchMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(",\n"));

        Option modelOpt = Option.builder("s").longOpt("solver").argName("SOLVER").required().hasArg()
                .desc("used search.\nValid searches value are : " + quotedValidSearch).build();

        Option inst = Option.builder("i").longOpt("instance").argName("INSTANCE").required().hasArg()
                .desc("path file").build();

        Option time = Option.builder("t").longOpt("time").argName("TIME").required().hasArg()
                .desc("time limit (sec)").build();

        Options options = new Options();

        options.addOption(modelOpt);
        options.addOption(inst);
        options.addOption(time);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        String fileName = null;
        String searchName = null;
        int timeLimit = 0;
        try {
            cmd = parser.parse(options, args);
            fileName = cmd.getOptionValue("i");
            searchName = cmd.getOptionValue("s");
            timeLimit = Integer.parseInt(cmd.getOptionValue("t"));
            if (!searchMap.containsKey(searchName))
                throw new IllegalArgumentException("Unknown solver: " + searchName);

        } catch (ParseException exp) {

            System.err.println(exp.getMessage());
            new HelpFormatter().printHelp("JobShop Problem", options);
            System.exit(1);
        }
        try {
            long t0 = System.currentTimeMillis();
            JSInstance instance = new JSInstance(fileName, false);
            JSProblem problem = new JSProblem(instance);
            final VariableHeuristic<JSState> varh = new DefaultVariableHeuristic<JSState>();
            final JSFastUpperBound fub = new JSFastUpperBound(problem);
            final AstarDominanceChecker<JSState, BitSet> dominance = new AstarDominanceChecker<>(new JSDominance(problem),
                problem.nbVars());
            ArrayList<Precedence> preds = new ArrayList<>();
            for (int i = 0; i < instance.getnJobs(); i++) {
                for (int j = 0; j < instance.getnMachines() - 1; j++) {
                    preds.add(new Precedence(i * instance.getnMachines() + j, i * instance.getnMachines() + j + 1));
                }
            }
            problem.addPrecedencesConstraint(preds);
            if (Objects.equals(searchName, "Astar")) {
                final Solver solverAstar = astarSolver(
                        problem,
                        varh,
                        fub,
                        dominance,
                        timeLimit
                );
                System.out.println("Instance; Search; nbIterations; queueMaxSize; runTimeMS; SearchStatus; Gap; Obj");
                System.out.print(fileName + ";"+ searchName+ ";");
                SearchStatistics stats = solverAstar.maximize(0, false);
                System.out.println(stats);
            }else if (Objects.equals(searchName, "ACS")) {
                final Solver solverACS = acsSolver(
                    problem,
                    varh,
                    fub,
                    dominance,
                    10,
                    timeLimit
                );
                System.out.println("Instance; Search; nbIterations; queueMaxSize; runTimeMS; SearchStatus; Gap; Obj");
                System.out.print(fileName + ";"+ searchName+ ";");
                SearchStatistics stats = solverACS.maximize(1, false);
                System.out.println(stats);
            }else if (Objects.equals(searchName, "LNS")) {
                JSProblem problemLNS = new JSProblem(instance);
                final VariableHeuristic<JSState> varhLNS = new DefaultVariableHeuristic<JSState>();
                final JSFastUpperBound fubLNS = new JSFastUpperBound(problemLNS);
                final AstarDominanceChecker<JSState, BitSet> dominanceLNS = new AstarDominanceChecker<>(new JSDominance(problem), problem.nbVars());
                problemLNS.addPrecedencesConstraint(preds);
                final LNSSolver2<JSState, BitSet> lnsSolver = new LNSSolver2<>(problemLNS,varhLNS,fubLNS,dominanceLNS,10, timeLimit);
                System.out.println("Instance; Search; nbIterations; queueMaxSize; runTimeMS; SearchStatus; Gap; Obj");
                System.out.print(fileName + ";"+ searchName + ";");
                final Solver solverACSwLNS = acswLNS2Solver(
                        problem,
                        varh,
                        fub,
                        dominance,
                        10,
                        lnsSolver,
                        timeLimit
                );
                SearchStatistics stats = solverACSwLNS.maximize(0, false);
                System.out.println(stats);

            }

        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

    }

    public static double main(String arg, String solver) {
        JSInstance instance = new JSInstance(arg, false);
        JSProblem problem = new JSProblem(instance);
        final VariableHeuristic<JSState> varh = new DefaultVariableHeuristic<JSState>();
        final JSFastUpperBound2 fub = new JSFastUpperBound2(problem);
        final AstarDominanceChecker<JSState, BitSet> dominance = new AstarDominanceChecker<>(new JSDominance(problem),
                problem.nbVars());
        ArrayList<Precedence> preds = new ArrayList<>();

        for (int i = 0; i < instance.getnJobs(); i++) {
            for (int j = 0; j < instance.getnMachines() - 1; j++) {
                preds.add(new Precedence(i * instance.getnMachines() + j, i * instance.getnMachines() + j + 1));
            }
        }
        problem.addPrecedencesConstraint(preds);
        final Solver solverAstar = astarSolver(
                problem,
                varh,
                fub,
                dominance,
                300
        );
        final Solver solverACS = acsSolver(
                problem,
                varh,
                fub,
                dominance,
                10,
                300
        );
        if (solver.equals("Astar")){
            solverAstar.maximize(0, false);
            return solverAstar.bestValue().get();
        }else if (solver.equals("ACS")){
            solverACS.maximize(0, false);
            return solverACS.bestValue().get();
        }
        return -1.0;
    }
}
