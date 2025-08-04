package org.ddolib.astar.examples.JobShop;

import org.ddolib.modeling.Dominance;

import java.util.BitSet;

import static java.lang.Math.min;

public class JSDominance implements Dominance<JSState, BitSet> {
    
    final JSProblem problem;
    
    public JSDominance(JSProblem problem) {
        this.problem = problem;
    }
    @Override
    public BitSet getKey(JSState state) {
        return state.done;
    }

    @Override
    public boolean isDominatedOrEqual(JSState state1, JSState state2) {
        BitSet set = state1.done;
        int dominatedSate1 = 0;
        int dominatedSate2 = 0;
        int Cmax1 = this.problem.getMakespan(state1);
        int Cmax2 = this.problem.getMakespan(state2);
        if (set.cardinality() == this.problem.data.getnJobs() * this.problem.data.getnMachines()) {
            if (Cmax1 > Cmax2) {
                return true;
            } else if (Cmax1 < Cmax2) {
                return false;
            }
        }
        for (int i = 0; i < this.problem.data.getnJobs(); i++) {
            for (int j = 0; j < this.problem.data.getnMachines(); j++) {
                if (!set.get(i * this.problem.data.getnMachines() + j)) {
                    int e1 = Integer.MAX_VALUE;
                    int e2 = Integer.MAX_VALUE;
                    for (int k : this.problem.data.getTasks()[this.problem.data.getMachine()[i][j]]) {
                        if (!set.get(k) && k != i * this.problem.data.getnMachines() + j) {
                            int job = k / this.problem.data.getnMachines();
                            int op = k % this.problem.data.getnMachines();
                            if (state1.est[job][op] + this.problem.data.getDuration()[job][op] >= Cmax1) {
                                e1 = min(e1,state1.est[job][op] + this.problem.data.getDuration()[job][op] + this.problem.data.getDuration()[i][j]);
                            }
                            if (state2.est[job][op] + this.problem.data.getDuration()[job][op] >= Cmax2) {
                                e2 = min(e2,state2.est[job][op] + this.problem.data.getDuration()[job][op] + this.problem.data.getDuration()[i][j]);
                            }
                        }
                    }
                    if (state1.est[i][j] + this.problem.data.getDuration()[i][j] >= Cmax1) {
                        e1 = state1.est[i][j] + this.problem.data.getDuration()[i][j];
                    }
                    if (state2.est[i][j] + this.problem.data.getDuration()[i][j] >= Cmax2) {
                        e2 = state2.est[i][j] + this.problem.data.getDuration()[i][j];
                    }
                    if (e1 < e2) {
                        dominatedSate2 = 1;
                    }
                    if (e2 < e1) {
                        dominatedSate1 = 1;
                    }

                }
            }
        }
        if (dominatedSate1 == 1 && dominatedSate2 == 1) {
            return false;
        } else if (dominatedSate1 == 1) {
            return true;
        } else if (dominatedSate2 == 1) {
            return false;
        } else if (dominatedSate1 == 0 && dominatedSate2 == 0) {
            return true;
        }
        return false;
    }
}
