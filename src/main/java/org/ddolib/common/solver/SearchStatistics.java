package org.ddolib.common.solver;

public record SearchStatistics(
        SearchStatus status,
        int nbIterations,
        int queueMaxSize,
        long runTimeMs,
        double incumbent,
        double gap) {

    @Override
    public String toString() {
        return "\n\tstatus = " + status +
                "\n\tnbIterations = " + nbIterations +
                "\n\tqueueMaxSize = " + queueMaxSize +
                "\n\trunTimeMs (ms) = " + runTimeMs +
                "\n\tincumbent = " + (incumbent == Double.POSITIVE_INFINITY || incumbent == Double.NEGATIVE_INFINITY ? "+-∞" : incumbent) +
                "\n\tgap = " + (gap == Double.POSITIVE_INFINITY ? "∞" : gap) +
                "\n";
    }

    public String toCsv() {
        return this.incumbent() + ";" + // objective
                this.runTimeMs() + ";" + // runtime
                this.gap() + ";" + // Gap
                this.nbIterations() + ";" + // nbIterations
                this.status() + ";" +
                this.queueMaxSize();
    }

    public String toCSv() {
        return null;
    }
}

