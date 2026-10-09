package org.ddolib.examples.nolayer.tsp;

import org.ddolib.nolayer.modeling.DdoModel;

abstract class TSPDdoModel extends TSPModel implements DdoModel<TSPState> {
    public TSPDdoModel(TSPProblem problem) {
        super(problem);
    }
}
