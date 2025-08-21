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
                SearchStatistics stats = solverACS.maximize(0, false);
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
            solverAstar.maximize(4, false);
            return solverAstar.bestValue().get();
        }else if (solver.equals("ACS")){
            solverACS.maximize(4, false);
            return solverACS.bestValue().get();
        }
        return -1.0;
    }
//    public static void main(String args[]) {

//        System.out.println(main("data/JobShop/Lawrence1984/la07.txt", "ACS"));

//        System.out.println(main("data/JobShop/Lawrence1984/la01.txt","ACS"));

//        JSInstance instance = new JSInstance("data/JobShop/Lawrence1984/la01.txt", false);
//        JSProblem problem = new JSProblem(instance);
//        final VariableHeuristic<JSState> varh = new DefaultVariableHeuristic<JSState>();
//        final JSFastUpperBound fub = new JSFastUpperBound(problem);
//        final AstarDominanceChecker<JSState, BitSet> dominance = new AstarDominanceChecker<>(new JSDominance(problem),
//                problem.nbVars());
//
//
//
//        ArrayList<Precedence> preds = new ArrayList<>();
//
//        for (int i = 0; i < instance.getnJobs(); i++) {
//            for (int j = 0; j < instance.getnMachines() - 1; j++) {
//                preds.add(new Precedence(i * instance.getnMachines() + j, i * instance.getnMachines() + j + 1));
//            }
//        }
//        problem.addPrecedencesConstraint(preds);
//
//
//        final Solver solverACS = acsSolver(
//                problem,
//                varh,
//                fub,
//                dominance,
//                10
//        );
//
//
//        System.out.println("Solving with ACS");
//
//        long start = System.currentTimeMillis();
//        SearchStatistics stats = solverACS.maximize(4, false);
//        double duration = (System.currentTimeMillis() - start) / 1000.0;
//
//        System.out.println("Search statistics using ddo:" + stats);
//
//
//
//        int[] solution = solverACS.bestSolution().map(decisions -> {
//            int[] values = new int[problem.nbVars()];
//            for (Decision d : decisions) {
//                values[d.var()] = d.val();
//            }
//            return values;
//        }).get();
//
//        System.out.printf("Duration : %.3f seconds%n", duration);
//        System.out.printf("Objective: %f%n", solverACS.bestValue().get());
//        System.out.printf("Solution : %s%n", Arrays.toString(solution));

//        JSInstance instance = new JSInstance("data/JobShop/Lawrence1984/la17.txt", false);
//        JSProblem problem = new JSProblem(instance);
//        JSProblem problemLNS = new JSProblem(instance);
//        final VariableHeuristic<JSState> varh = new DefaultVariableHeuristic<JSState>();
//        final VariableHeuristic<JSState> varhLNS = new DefaultVariableHeuristic<JSState>();
//        final JSFastUpperBound fub = new JSFastUpperBound(problem);
//        final JSFastUpperBound fubLNS = new JSFastUpperBound(problemLNS);
//        final AstarDominanceChecker<JSState, BitSet> dominance = new AstarDominanceChecker<>(new JSDominance(problem),
//                problem.nbVars());
//        final AstarDominanceChecker<JSState, BitSet> dominanceLNS = new AstarDominanceChecker<>(new JSDominance(problem),
//                problem.nbVars());
//
//
//        ArrayList<Precedence> preds = new ArrayList<>();
//
//        for (int i = 0; i < instance.getnJobs(); i++) {
//            for (int j = 0; j < instance.getnMachines() - 1; j++) {
//                preds.add(new Precedence(i * instance.getnMachines() + j, i * instance.getnMachines() + j + 1));
//            }
//        }
//        problem.addPrecedencesConstraint(preds);
//        problemLNS.addPrecedencesConstraint(preds);
//
//        final LNSSolver2 lnsSolver = new LNSSolver2(problemLNS,varh,fubLNS,dominanceLNS,10);
//
//        final Solver solverACSwLNS = acswLNS2Solver(
//                problem,
//                varh,
//                fub,
//                dominance,
//                10, lnsSolver
//        );
//
//
//        System.out.println("Solving with ACS");
//
//        long start = System.currentTimeMillis();
//        SearchStatistics stats = solverACSwLNS.maximize(4, false);
//        double duration = (System.currentTimeMillis() - start) / 1000.0;
//
//        System.out.println("Search statistics using ddo:" + stats);
//
//
//
//        int[] solution = solverACSwLNS.bestSolution().map(decisions -> {
//            int[] values = new int[problem.nbVars()];
//            for (Decision d : decisions) {
//                values[d.var()] = d.val();
//            }
//            return values;
//        }).get();
//
//        System.out.printf("Duration : %.3f seconds%n", duration);
//        System.out.printf("Objective: %f%n", solverACSwLNS.bestValue().get());
//        System.out.printf("Solution : %s%n", Arrays.toString(solution));




//        JSInstance instance = new JSInstance("data/JobShop/FisherThompson1963/ft10.txt", false);
//        JSProblem problem = new JSProblem(instance);
//        final VariableHeuristic<JSState> varh = new DefaultVariableHeuristic<JSState>();
//        final JSFastUpperBound fub = new JSFastUpperBound(problem);
//        final AstarDominanceChecker<JSState, BitSet> dominance = new AstarDominanceChecker<>(new JSDominance(problem),
//                problem.nbVars());
//        final AstarDominanceChecker<JSState, BitSet> dominanceLNS = new AstarDominanceChecker<>(new JSDominance(problem),
//                problem.nbVars());
//
//
//        ArrayList<Precedence> preds = new ArrayList<>();
//
//        for (int i = 0; i < instance.getnJobs(); i++) {
//            for (int j = 0; j < instance.getnMachines() - 1; j++) {
//                preds.add(new Precedence(i * instance.getnMachines() + j, i * instance.getnMachines() + j + 1));
//            }
//        }
//        problem.addPrecedencesConstraint(preds);
//
//        final LNSSolver<JSState, BitSet> lnsSolver = new LNSSolver<>(problem,varh,fub,dominanceLNS,10);
//        problem.addPrecedenceConstraint(new Precedence( 10,41));
//        problem.addPrecedenceConstraint(new Precedence( 41,61));
//        problem.addPrecedenceConstraint(new Precedence( 61,0));
//        problem.addPrecedenceConstraint(new Precedence( 0,80));
//        problem.addPrecedenceConstraint(new Precedence( 80,71));
//        problem.addPrecedenceConstraint(new Precedence( 71,32));
//        problem.addPrecedenceConstraint(new Precedence( 32,21));
//        problem.addPrecedenceConstraint(new Precedence( 21,91));
//        problem.addPrecedenceConstraint(new Precedence( 60,42));
//        problem.addPrecedenceConstraint(new Precedence( 42,30));
//        problem.addPrecedenceConstraint(new Precedence( 30,1));
//        problem.addPrecedenceConstraint(new Precedence( 1,81));
//        problem.addPrecedenceConstraint(new Precedence( 81,20));
//        problem.addPrecedenceConstraint(new Precedence( 20,90));
//        problem.addPrecedenceConstraint(new Precedence( 90,51));
//        problem.addPrecedenceConstraint(new Precedence( 51,72));
//        problem.addPrecedenceConstraint(new Precedence( 72,15));
//        problem.addPrecedenceConstraint(new Precedence( 11,70));
//        problem.addPrecedenceConstraint(new Precedence( 70,63));
//        problem.addPrecedenceConstraint(new Precedence( 63,31));
//        problem.addPrecedenceConstraint(new Precedence( 31,2));
//        problem.addPrecedenceConstraint(new Precedence( 2,50));
//        problem.addPrecedenceConstraint(new Precedence( 50,92));
//        problem.addPrecedenceConstraint(new Precedence( 92,23));
//        problem.addPrecedenceConstraint(new Precedence( 23,84));
//        problem.addPrecedenceConstraint(new Precedence( 62,44));
//        problem.addPrecedenceConstraint(new Precedence( 44,14));
//        problem.addPrecedenceConstraint(new Precedence( 14,3));
//        problem.addPrecedenceConstraint(new Precedence( 3,82));
//        problem.addPrecedenceConstraint(new Precedence( 82,22));
//        problem.addPrecedenceConstraint(new Precedence( 22,53));
//        problem.addPrecedenceConstraint(new Precedence( 53,37));;
//        problem.addPrecedenceConstraint(new Precedence( 37,97));
//        problem.addPrecedenceConstraint(new Precedence( 97,79));
//        problem.addPrecedenceConstraint(new Precedence( 45,4));
//        problem.addPrecedenceConstraint(new Precedence( 4,33));
//        problem.addPrecedenceConstraint(new Precedence( 33,69));
//        problem.addPrecedenceConstraint(new Precedence( 69,74));
//        problem.addPrecedenceConstraint(new Precedence( 98,88));;
//        problem.addPrecedenceConstraint(new Precedence( 88,58));
//        problem.addPrecedenceConstraint(new Precedence( 58,29));
//        problem.addPrecedenceConstraint(new Precedence( 43,65));;
//        problem.addPrecedenceConstraint(new Precedence( 65,5));
//        problem.addPrecedenceConstraint(new Precedence( 5,83));
//        problem.addPrecedenceConstraint(new Precedence( 52,73));
//        problem.addPrecedenceConstraint(new Precedence( 73,17));
//        problem.addPrecedenceConstraint(new Precedence( 17,96));;
//        problem.addPrecedenceConstraint(new Precedence( 96,25));
//        problem.addPrecedenceConstraint(new Precedence( 25,39));;
//        problem.addPrecedenceConstraint(new Precedence( 64,6));
//        problem.addPrecedenceConstraint(new Precedence( 34,49));
//        problem.addPrecedenceConstraint(new Precedence( 93,16));
//        problem.addPrecedenceConstraint(new Precedence( 75,86));
//        problem.addPrecedenceConstraint(new Precedence( 27,57));;
//        problem.addPrecedenceConstraint(new Precedence( 7,36));
//        problem.addPrecedenceConstraint(new Precedence( 36,18));;
//        problem.addPrecedenceConstraint(new Precedence( 18,26));
//        problem.addPrecedenceConstraint(new Precedence( 26,87));
//        problem.addPrecedenceConstraint(new Precedence( 87,78));
//        problem.addPrecedenceConstraint(new Precedence( 78,99));
//        problem.addPrecedenceConstraint(new Precedence( 99,59));
//        problem.addPrecedenceConstraint(new Precedence( 46,67));
//        problem.addPrecedenceConstraint(new Precedence( 67,35));
//        problem.addPrecedenceConstraint(new Precedence( 35,94));
//        problem.addPrecedenceConstraint(new Precedence( 24,76));
//        problem.addPrecedenceConstraint(new Precedence( 76,19));
//        problem.addPrecedenceConstraint(new Precedence( 19,8));
//        problem.addPrecedenceConstraint(new Precedence( 8,89));
//        problem.addPrecedenceConstraint(new Precedence( 13,66));
//        problem.addPrecedenceConstraint(new Precedence( 66,48));
//        problem.addPrecedenceConstraint(new Precedence( 48,95));
//        problem.addPrecedenceConstraint(new Precedence( 95,85));
//        problem.addPrecedenceConstraint(new Precedence( 85,38));
//        problem.addPrecedenceConstraint(new Precedence( 38,55));
//        problem.addPrecedenceConstraint(new Precedence( 77,9));
//        problem.addPrecedenceConstraint(new Precedence( 9,28));
//        lnsSolver.setLB(-988.0);
//
//        long start = System.currentTimeMillis();
//        SearchStatistics stats = lnsSolver.maximize(4, false);
//        double duration = (System.currentTimeMillis() - start) / 1000.0;
//        System.out.printf("Duration : %.3f seconds%n", duration);
//
//        System.out.println("Search statistics using ddo:" + stats);
//
//
//        int[] solution = lnsSolver.bestSolution().map(decisions -> {
//            int[] values = new int[problem.nbVars()];
//            for (Decision d : decisions) {
//                values[d.var()] = d.val();
//            }
//            return values;
//        }).get();
//
//        System.out.printf("Duration : %.3f seconds%n", duration);
//        System.out.printf("Objective: %f%n", lnsSolver.bestValue().get());
//        System.out.printf("Solution : %s%n", Arrays.toString(solution));
//    }
}
