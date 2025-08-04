package org.ddolib.astar.examples.JobShop;

import org.ddolib.modeling.FastUpperBound;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class JSFastUpperBound implements FastUpperBound<JSState> {
    
    JSProblem problem;
    
    public JSFastUpperBound(JSProblem problem) {
        this.problem = problem;
    }
    
    @Override
    public double fastUpperBound(JSState state, Set<Integer> variables) {
        BitSet[][] profil = new BitSet[this.problem.data.getnMachines()][ this.problem.data.getHorizon()];
        int[] profilTime = new int[ this.problem.data.getnMachines()];
        int[][] starts = new int[ this.problem.data.getnJobs()][ this.problem.data.getnMachines()];
        for (int i = 0; i <  this.problem.data.getnMachines(); i++) {
            for (int j = 0; j <  this.problem.data.getHorizon(); j++) {
                profil[i][j] = new BitSet( this.problem.data.getnMachines() *  this.problem.data.getnJobs());
            }
        }
        int makespan = this.problem.getMakespan(state);
        for (int i = 0; i <  this.problem.data.getnJobs(); i++) {
            for (int j = 0; j <  this.problem.data.getnMachines(); j++) {
                int startIdx = state.est[i][j];
                ArrayList<Integer> preds = this.problem.pred.get(i *  this.problem.data.getnMachines() + j);
                for (int pred: preds) {
                    startIdx = max(startIdx, starts[pred/ this.problem.data.getnMachines()][pred% this.problem.data.getnMachines()] +  this.problem.data.getDuration()[pred/ this.problem.data.getnMachines()][pred% this.problem.data.getnMachines()]);
                }
                if (!state.done.get(i *  this.problem.data.getnMachines() + j) && startIdx +  this.problem.data.getDuration()[i][j] < makespan) {
                    int s = Integer.MAX_VALUE;
                    for (int k :  this.problem.data.getTasks()[ this.problem.data.getMachine()[i][j]]) {
                        if (!state.done.get(k) && k != i *  this.problem.data.getnMachines() + j) {
                            int job = k /  this.problem.data.getnMachines();
                            int op = k %  this.problem.data.getnMachines();
                            if (state.est[job][op] +  this.problem.data.getDuration()[job][op] >= makespan) {
                                s = min(s, state.est[job][op] +  this.problem.data.getDuration()[job][op]);
                            }
                        }
                    }
                    startIdx = s;
                }
                if (startIdx == Integer.MAX_VALUE) {
                    return - Integer.MAX_VALUE;
                }
                starts[i][j] = startIdx;

            }
        }
        for (int i = 0; i <  this.problem.data.getnJobs(); i++) {
            for (int j = 0; j <  this.problem.data.getnMachines(); j++) {
                int machine =  this.problem.data.getMachine()[i][j];
                int startIdx = starts[i][j];
                for (int k = startIdx; k < startIdx +  this.problem.data.getDuration()[i][j]; k++) {
                    profil[machine][k].set(i *  this.problem.data.getnMachines() + j);
                }
                profilTime[machine] = max(profilTime[machine], startIdx +  this.problem.data.getDuration()[i][j]);
            }
        }

        int bound = 0;
        for (int i = 0; i <  this.problem.data.getnMachines(); i++) {
            int count = 0;
            for (int j = 0; j < profilTime[i]; j++) {
                int s = profil[i][j].cardinality();
                if (s > 1) {
                    count += (s - 1);
                } else if (s == 0) {
                    count--;
                    count = max(count, 0);
                }
            }
            profilTime[i] += count;

            bound = max(bound, profilTime[i]);
        }

        return - (bound - this.problem.getMakespan(state));
        //return 0;
    }
}
