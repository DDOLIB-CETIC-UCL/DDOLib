package org.ddolib.astar.examples.JSTSP;

import java.util.Objects;

public class JSTSPState {
    public int currentJobIdx;
    FreeTools f;
    public long remaining;
    int hash;

    public JSTSPState(int currentJobIdx, FreeTools f, long remaining) {
        this.currentJobIdx = currentJobIdx;
        this.f = f;
        this.remaining = remaining;
        this.hash = Objects.hash(this.currentJobIdx, this.f, this.remaining);
    }

    public String toString() {
        return "current_job: " + currentJobIdx + " \n f: " + f + " \n remaining: " + remaining;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object state) {
        JSTSPState ts = (JSTSPState) state;
        return currentJobIdx == ts.currentJobIdx && remaining == ts.remaining && f.equals(ts.f);
    }
}
