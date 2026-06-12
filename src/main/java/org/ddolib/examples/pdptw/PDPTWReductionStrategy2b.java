package org.ddolib.examples.pdptw;

public class PDPTWReductionStrategy2b extends KeyBasedReductionStrategy<PDPTWState> {

    private PDPTWProblem problem;

    public PDPTWReductionStrategy2b(PDPTWProblem problem){
        this.problem = problem;
    }

    @Override
    Object getKey(PDPTWState state) {
        return new PDPClusteringKey(state.current.nextSetBit(0));
    }
}

