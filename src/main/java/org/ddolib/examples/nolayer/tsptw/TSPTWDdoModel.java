package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.nolayer.modeling.DdoModel;

abstract class TSPTWDdoModel extends TSPTWModel implements DdoModel<TSPTWState> {
    public TSPTWDdoModel(TSPTWProblem problem) {
        super(problem);
    }
}
