package org.ddolib.common.solver;

import org.ddolib.util.PrettyPrint;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Class representing the statistics of a search process in a solver.
 * <p>
 * This class tracks various metrics such as runtime, number of iterations,
 * incumbent value, and optimality gap. It is designed to be immutable-like,
 * where update methods return a new instance with the updated value.
 * </p>
 */
public final class SearchStatistics {
    /**
     * Start time of the search (in milliseconds)
     */
    private final long _startTime;
    /**
     * Time at which the last improvement was found (in milliseconds)
     */
    private long _lastTimeOfImprovement;
    /**
     * Current time of the search (in milliseconds)
     */
    private long _currentTime;
    /**
     * Current status of the search
     */
    private SearchStatus _status = SearchStatus.UNKNOWN;
    /**
     * Total number of iterations performed
     */
    private int _nbIterations = 0;
    /**
     * Iteration during which the last improvement was found
     */
    private int _lastIterationOfImprovement = 0;
    /**
     * Value of the best solution found so far (incumbent)
     */
    private double _incumbent;
    /**
     * Value of the previous incumbent found
     */
    private double _prevIncumbent = Double.POSITIVE_INFINITY;
    /**
     * Current optimality gap
     */
    private double _gap = 100.0;
    /**
     * Maximum size reached by the search frontier
     */
    private int _frontierMaxSize = 0;


    /**
     * Constructs a new SearchStatistics instance.
     *
     * @param startTime the start time of the search (in milliseconds)
     * @param initValue the initial value for the incumbent
     */
    public SearchStatistics(long startTime, double initValue) {
        _startTime = startTime;
        _lastTimeOfImprovement = startTime;
        _currentTime = startTime;
        _incumbent = initValue;
    }

    /**
     * Returns the time at which the last improvement was found.
     *
     * @return the last time of improvement (in milliseconds)
     */
    public long lastTimeOfImprovement() {
        return _lastTimeOfImprovement;
    }

    /**
     * Returns the total runtime of the search.
     *
     * @return the runtime (in milliseconds)
     */
    public long runtime() {
        return _currentTime - _startTime;
    }

    /**
     * Returns the current time recorded in these statistics.
     *
     * @return the current time (in milliseconds)
     */
    public long currentTime() {
        return _currentTime;
    }

    /**
     * Returns the total number of iterations performed by the search.
     *
     * @return the number of iterations
     */
    public int nbIterations() {
        return _nbIterations;
    }

    /**
     * Returns the iteration number during which the last improvement was found.
     *
     * @return the last iteration of improvement
     */
    public int lastIterationOfImprovement() {
        return _lastIterationOfImprovement;
    }

    /**
     * Returns the current optimality gap.
     *
     * @return the gap (percentage)
     */
    public double gap() {
        return _gap;
    }

    /**
     * Returns the value of the best solution found so far (incumbent).
     *
     * @return the incumbent value
     */
    public double incumbent() {
        return _incumbent;
    }

    /**
     * Returns the value of the previous incumbent found before the current one.
     *
     * @return the previous incumbent value
     */
    public double prevIncumbent() {
        return _prevIncumbent;
    }

    /**
     * Returns the current status of the search.
     *
     * @return the {@link SearchStatus}
     */
    public SearchStatus status() {
        return _status;
    }

    /**
     * Returns the maximum size reached by the search frontier during the process.
     *
     * @return the maximum frontier size
     */
    public int frontierMaxSize() {
        return _frontierMaxSize;
    }

    /**
     * Creates and returns a copy of this statistics instance.
     *
     * @return a copy of the current statistics
     */
    public SearchStatistics copy() {
        SearchStatistics clone = new SearchStatistics(this._startTime, this._incumbent);

        clone._currentTime = this._currentTime;
        clone._lastTimeOfImprovement = this._lastTimeOfImprovement;
        clone._status = this._status;
        clone._nbIterations = this._nbIterations;
        clone._lastIterationOfImprovement = this._lastIterationOfImprovement;
        clone._prevIncumbent = this._prevIncumbent;
        clone._gap = this._gap;
        clone._frontierMaxSize = this._frontierMaxSize;

        return clone;
    }

    /**
     * Returns a new SearchStatistics instance with an updated incumbent value and gap.
     *
     * @param incumbent the new incumbent value
     * @param gap       the new optimality gap
     * @return a new instance with updated incumbent and gap
     */
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

    /**
     * Returns a new SearchStatistics instance with an updated search status.
     *
     * @param status the new {@link SearchStatus}
     * @return a new instance with the updated status
     */
    public SearchStatistics updateStatus(SearchStatus status) {
        SearchStatistics toReturn = this.copy();
        toReturn._status = status;
        return toReturn;
    }

    /**
     * Returns a new SearchStatistics instance with the iteration count incremented by one.
     *
     * @return a new instance with incremented iterations
     */
    public SearchStatistics incrementNbIter() {
        SearchStatistics toReturn = this.copy();
        toReturn._nbIterations++;
        return toReturn;
    }

    /**
     * Returns a new SearchStatistics instance with an updated maximum frontier size.
     *
     * @param frontierSize the current frontier size to potentially update the maximum
     * @return a new instance with the updated maximum frontier size
     */
    public SearchStatistics updateFrontierMaxSize(int frontierSize) {
        SearchStatistics toReturn = this.copy();
        toReturn._frontierMaxSize = Integer.max(this._frontierMaxSize, frontierSize);
        return toReturn;
    }

    /**
     * Returns a new SearchStatistics instance with the maximum frontier size incremented by one.
     *
     * @return a new instance with incremented maximum frontier size
     */
    public SearchStatistics incrementFrontierSize() {
        SearchStatistics toReturn = this.copy();
        toReturn._frontierMaxSize++;
        return toReturn;
    }

    /**
     * Returns a new SearchStatistics instance with an updated optimality gap.
     *
     * @param gap the new gap value
     * @return a new instance with the updated gap
     */
    public SearchStatistics updateGap(double gap) {
        SearchStatistics toReturn = this.copy();
        toReturn._gap = gap;
        return toReturn;
    }

    /**
     * Returns a new SearchStatistics instance with an updated current time.
     *
     * @param time the current time (in milliseconds)
     * @return a new instance with the updated time
     */
    public SearchStatistics updateTime(long time) {
        SearchStatistics toReturn = this.copy();
        toReturn._currentTime = time;
        return toReturn;
    }


    @Override
    public String toString() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#,##0.##########", symbols);
        DecimalFormat gapFormat = new DecimalFormat("#,##0.####", symbols);

        List<String> labels = List.of(
                "Status", "Iterations", "Frontier Max Size", "Runtime", "Incumbent", "Gap"
        );

        List<String> values = List.of(
                _status.toString(),
                df.format(_nbIterations),
                df.format(_frontierMaxSize),
                PrettyPrint.formatMs(runtime()),
                Double.isInfinite(_incumbent) ? "∞" : df.format(_incumbent),
                Double.isInfinite(_gap) ? "∞" : gapFormat.format(_gap) + " %"
        );

        return PrettyPrint.buildTable(labels, Collections.singletonList(values));
    }
}
