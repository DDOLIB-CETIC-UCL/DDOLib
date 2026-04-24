package org.ddolib.common.solver;

public final class SearchStatistics {
    private final long _t0;
    private long _lastTimeOfImprovement;
    private long _currentTime;
    private SearchStatus _status = SearchStatus.UNKNOWN;
    private int _nbIterations = 0;
    private int _lastIterationOfImprovement = 0;
    private double _incumbent;
    private double _prevIncumbent = Double.POSITIVE_INFINITY;
    private double _gap = 100.0;
    private int _frontierMaxSize = 0;


    public SearchStatistics(long startTime, double initValue) {
        _t0 = startTime;
        _lastTimeOfImprovement = startTime;
        _currentTime = startTime;
        _incumbent = initValue;
    }

    public long lastTimeOfImprovement() {
        return _lastTimeOfImprovement;
    }

    public long runtime() {
        return _currentTime - _t0;
    }

    public int nbIterations() {
        return _nbIterations;
    }

    public int lastIterationOfImprovement() {
        return _lastIterationOfImprovement;
    }

    public double gap() {
        return _gap;
    }

    public double incumbent() {
        return _incumbent;
    }

    public double prevIncumbent() {
        return _prevIncumbent;
    }

    public SearchStatus status() {
        return _status;
    }

    public int frontierMaxSize() {
        return _frontierMaxSize;
    }

    public SearchStatistics copy() {
        SearchStatistics clone = new SearchStatistics(this._t0, this._incumbent);

        clone._currentTime = this._currentTime;
        clone._lastTimeOfImprovement = this._lastTimeOfImprovement;
        clone._status = this._status;
        clone._nbIterations = this._nbIterations;
        clone._lastIterationOfImprovement = this._lastIterationOfImprovement;
        clone._prevIncumbent = this._prevIncumbent;
        clone._gap = this._gap;
        clone._frontierMaxSize = this.frontierMaxSize();

        return clone;
    }

    public SearchStatistics updateIncumbent(double incumbent, double gap) {
        SearchStatistics toReturn = this.copy();
        toReturn._incumbent = incumbent;
        toReturn._prevIncumbent = this._incumbent;
        toReturn._currentTime = System.currentTimeMillis();
        toReturn._lastTimeOfImprovement = toReturn._currentTime;
        toReturn._lastIterationOfImprovement = this._nbIterations;
        toReturn._gap = gap;

        return toReturn;
    }

    public SearchStatistics updateStatus(SearchStatus status) {
        SearchStatistics toReturn = this.copy();
        toReturn._status = status;
        return toReturn;
    }

    public SearchStatistics incrementNbIter() {
        SearchStatistics toReturn = this.copy();
        toReturn._nbIterations++;
        return toReturn;
    }

    public SearchStatistics updateFrontierMaxSize(int frontierSize) {
        SearchStatistics toReturn = this.copy();
        toReturn._frontierMaxSize = Integer.max(this._frontierMaxSize, frontierSize);
        return toReturn;
    }

    public SearchStatistics incrementFrontierSize() {
        SearchStatistics toReturn = this.copy();
        toReturn._frontierMaxSize++;
        return toReturn;
    }

    public SearchStatistics updateGap(double gap) {
        SearchStatistics toReturn = this.copy();
        toReturn._gap = gap;
        return toReturn;
    }

    public SearchStatistics updateTime(long time) {
        SearchStatistics toReturn = this.copy();
        toReturn._currentTime = time;
        return toReturn;
    }


    @Override
    public String toString() {
        String str = "\n\tstatus: " + status();
        str += "\n\tnbIterations: " + nbIterations();
        str += "\n\tfrontierMaxSize: " + frontierMaxSize();
        str += "\n\truntime: " + runtime();
        str += "\n\tincumbent: " + (Double.isInfinite(incumbent()) ? "+-∞" : incumbent());
        str += "\n\tgap: " + (Double.isInfinite(gap()) ? "∞" : incumbent());
        str += "\n";
        return str;
    }
}
