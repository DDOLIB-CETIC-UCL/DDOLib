package org.ddolib.examples.pdptw;

public class PDPTWReductionStrategy2 extends KeyBasedReductionStrategy<PDPTWState> {

    public record PDPClusteringKey(int position, int Content){}

    @Override
    Object getKey(PDPTWState state) {
        return new PDPClusteringKey(state.current.nextSetBit(0), state.minContent);
    }
}

