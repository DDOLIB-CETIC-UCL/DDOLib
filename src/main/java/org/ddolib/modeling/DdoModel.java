package org.ddolib.modeling;

import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;

public interface DdoModel<T> extends Model<T> {

    Relaxation<T> relaxation();

    default StateRanking<T> ranking() {
        return (o1, o2) -> 0;
    }

    default WidthHeuristic<T> widthHeuristic() {
        return new FixedWidth<>(10);
    }

    default Frontier<T> frontier() {
        return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
    }

    default boolean useCache() {
        return false;
    }

    default boolean exportDot() {
        return false;
    }

}
