package org.ddolib.common.solver.stat;

/**
 * Class tracking statistics specific to Decision Diagram Optimization (DDO) solver.
 */
public class DdoStats extends SearchStatistics<DdoStats> {
    /**
     * Constructs a new SearchStatistics instance.
     *
     * @param startTime the start time of the search (in milliseconds)
     * @param initValue the initial value for the incumbent
     */
    public DdoStats(long startTime, double initValue) {
        super(startTime, initValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DdoStats createSpecificInstance() {
        return new DdoStats(this._startTime, this._incumbent);
    }
}
