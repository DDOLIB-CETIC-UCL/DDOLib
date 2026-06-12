package org.ddolib.examples.pdptw;

public class PDPTWReductionStrategy extends KeyBasedReductionStrategy<PDPTWState> {

    public record PDPClusteringKey(int position){}

    @Override
    Object getKey(PDPTWState state) {
        return new PDPClusteringKey(state.current.nextSetBit(0));
    }
}

