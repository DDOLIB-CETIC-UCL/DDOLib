package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.nolayer.modeling.DdoModel;

abstract class KSDdoModel extends KSModel implements DdoModel<KSState> {
    public KSDdoModel(KSProblem problem) {
        super(problem);
    }
}
