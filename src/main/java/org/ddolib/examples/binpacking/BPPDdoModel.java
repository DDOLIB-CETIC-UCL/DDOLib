package org.ddolib.examples.binpacking;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.verbosity.VerbosityLevel;

public class BPPDdoModel implements DdoModel<BPPState> {

    private final BPPProblem problem;
    private final int WIDTH;
    private final BPPRanking ranking = new BPPRanking();

    public BPPDdoModel(BPPProblem problem, int width) {
        this.problem = problem;
        this.WIDTH = width;
    }

    @Override
    public Relaxation<BPPState> relaxation() {
        return new BPPRelax(problem) {};
    }

    @Override
    public Problem<BPPState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<BPPState> lowerBound() {
        return new BPPFastLowerBound(problem);
    }

    @Override
    public StateRanking<BPPState> ranking() {
        return ranking;
    }

    @Override
    public Frontier<BPPState> frontier() {
        return new SimpleFrontier<>(ranking, CutSetType.Frontier);
    }

    @Override
    public WidthHeuristic<BPPState> widthHeuristic() {
        return new FixedWidth<>(WIDTH);
    }

    @Override
    public VerbosityLevel verbosityLevel() {
        return VerbosityLevel.NORMAL;
    }

    @Override
    public DominanceChecker<BPPState> dominance() {
        return new SimpleDominanceChecker<>(new BPPDominance(), problem.nbItems);
    }
}
