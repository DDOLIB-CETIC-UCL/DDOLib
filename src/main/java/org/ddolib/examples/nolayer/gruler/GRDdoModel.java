package org.ddolib.examples.nolayer.gruler;

import org.ddolib.nolayer.modeling.DdoModel;

abstract class GRDdoModel extends GRModel implements DdoModel<GRState> {
    public GRDdoModel(GRProblem problem) {
        super(problem);
    }
}
