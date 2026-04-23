package org.ddolib.common.solver;

public final class SearchObserver {
    private final long _t0;
    private long _lastTimeOfImprovement;
    private long _currentTime;
    private SearchStatus _status = SearchStatus.UNKNOWN;
    private int _nbIteration = 0;
    private int _lastIterationOfImprovement = 0;
    private double _incumbent;
    private double _prevIncumbent = Double.POSITIVE_INFINITY;
    private double _gap = 1.0;
    private int _frontierMaxSize = 0;


    public SearchObserver(long startTime, double initValue) {
        _t0 = startTime;
        _lastTimeOfImprovement = startTime;
        _currentTime = startTime;
        _incumbent = initValue;
    }

    public long lastTimeOfImprovement() {
        return _lastTimeOfImprovement;
    }

    public long runTime() {
        return _currentTime - _t0;
    }

    public int nbIteration() {
        return _nbIteration;
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

    public SearchObserver copy() {
        SearchObserver clone = new SearchObserver(this._t0, this._incumbent);

        clone._currentTime = this._currentTime;
        clone._lastTimeOfImprovement = this._lastTimeOfImprovement;
        clone._status = this._status;
        clone._nbIteration = this._nbIteration;
        clone._lastIterationOfImprovement = this._lastIterationOfImprovement;
        clone._prevIncumbent = this._prevIncumbent;
        clone._gap = this._gap;
        clone._frontierMaxSize = this.frontierMaxSize();

        return clone;
    }

    public SearchObserver updateIncumbent(double incumbent, double gap) {
        SearchObserver toReturn = this.copy();
        toReturn._incumbent = incumbent;
        toReturn._prevIncumbent = this._incumbent;
        toReturn._currentTime = System.currentTimeMillis();
        toReturn._lastTimeOfImprovement = toReturn._currentTime;
        toReturn._lastIterationOfImprovement = this._nbIteration;
        toReturn._gap = gap;

        return toReturn;
    }

    public SearchObserver updateStatus(SearchStatus status) {
        SearchObserver toReturn = this.copy();
        toReturn._status = status;
        return toReturn;
    }

    public SearchObserver incrementNbIter() {
        SearchObserver toReturn = this.copy();
        toReturn._nbIteration++;
        return toReturn;
    }

    public SearchObserver updateFrontierMaxSize(int frontierSize) {
        SearchObserver toReturn = this.copy();
        toReturn._frontierMaxSize = Integer.max(this._frontierMaxSize, frontierSize);
        return toReturn;
    }

    public SearchObserver updateGap(double gap) {
        SearchObserver toReturn = this.copy();
        toReturn._gap = gap;
        return toReturn;
    }

    public SearchObserver updateTime(long time) {
        SearchObserver toReturn = this.copy();
        toReturn._currentTime = time;
        return toReturn;
    }

    public SearchStatistics toStats() {
        return new SearchStatistics(status(), nbIteration(), frontierMaxSize(), runTime(),
                incumbent(), gap());
    }


    @Override
    public String toString() {
        String str = "\n\tstatus: " + status();
        str += "\n\tnbIterations: " + nbIteration();
        str += "\n\tfrontierMaxSize: " + frontierMaxSize();
        str += "\n\truntime: " + runTime();
        str += "\n\tincumbent: " + (Double.isInfinite(incumbent()) ? "+-∞" : incumbent());
        str += "\n\tgap: " + (Double.isInfinite(gap()) ? "∞" : incumbent());
        str += "\n";
        return str;
    }
}
