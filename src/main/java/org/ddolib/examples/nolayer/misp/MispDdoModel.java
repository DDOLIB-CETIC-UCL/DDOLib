package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.DdoModel;

abstract class MispDdoModel extends MispModel implements DdoModel<MispState> {
    public MispDdoModel(MispProblem problem) {
        super(problem);
    }
}
