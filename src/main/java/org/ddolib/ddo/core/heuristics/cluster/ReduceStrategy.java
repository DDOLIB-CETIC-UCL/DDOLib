package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram.NodeType;
import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram.NodeSubProblem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public interface ReduceStrategy<T> {

    public List<NodeSubProblem<T>>[] defineClusters(final List<NodeSubProblem<T>> layer, final int maxWidth);

}
