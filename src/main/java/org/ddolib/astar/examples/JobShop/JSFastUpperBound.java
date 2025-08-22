package org.ddolib.astar.examples.JobShop;

import org.ddolib.modeling.FastUpperBound;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class JSFastUpperBound implements FastUpperBound<JSState> {

    JSProblem problem;

    public JSFastUpperBound(JSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(JSState state, Set<Integer> variables) {
        int bound = 0;
        int makespan = this.problem.getMakespan(state);
        BitSet[][] profil = new BitSet[this.problem.data.getnMachines()][ this.problem.data.getHorizon()];
        int[] profilTime = new int[ this.problem.data.getnMachines()];
        int[][] starts = new int[ this.problem.data.getnJobs()][ this.problem.data.getnMachines()];
        for (int i = 0; i <  this.problem.data.getnMachines(); i++) {
            for (int j = 0; j <  this.problem.data.getHorizon(); j++) {
                profil[i][j] = new BitSet( this.problem.data.getnMachines() *  this.problem.data.getnJobs());
            }
        }
        for (int i = 0; i <  this.problem.data.getnJobs(); i++) {
            for (int j = 0; j <  this.problem.data.getnMachines(); j++) {
                starts[i][j] = state.est[i][j];
            }
        }

        List<Integer> order = topologicalSort(this.problem.data.getnJobs()* this.problem.data.getnMachines(), state);
        if (order==null){
            return -Integer.MAX_VALUE;
        }
        for (int o : order) {
            int i = o/this.problem.data.getnMachines();
            int j = o%this.problem.data.getnMachines();
            int startIdx = state.est[i][j];
            ArrayList<Integer> preds = this.problem.pred.get(o);
            for (int pred: preds) {
                startIdx = max(startIdx, starts[pred/ this.problem.data.getnMachines()][pred% this.problem.data.getnMachines()] +  this.problem.data.getDuration()[pred/ this.problem.data.getnMachines()][pred% this.problem.data.getnMachines()]);
            }
            if (!state.done.get(i *  this.problem.data.getnMachines() + j) && startIdx +  this.problem.data.getDuration()[i][j] < makespan) {
                int s = Integer.MAX_VALUE;
                for (int k :  this.problem.data.getTasks()[ this.problem.data.getMachine()[i][j]]) {
                    if (!state.done.get(k) && k != i *  this.problem.data.getnMachines() + j) {
                        int job = k /  this.problem.data.getnMachines();
                        int op = k %  this.problem.data.getnMachines();
                        if (starts[job][op] +  this.problem.data.getDuration()[job][op] >= makespan) {
                            s = min(s, starts[job][op] +  this.problem.data.getDuration()[job][op]);
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


    private List<Integer> topologicalSort(int V, JSState state) {
        List<List<Edge>> graph = new ArrayList<>();
        int makespan = this.problem.getMakespan(state);
        for (int i = 0; i < this.problem.data.getnJobs()* this.problem.data.getnMachines(); i++) {
            graph.add(new ArrayList<>());
        }

        for(int i = 0; i < this.problem.data.getnJobs(); i++){
            for (int j = 0; j < this.problem.data.getnMachines()-1; j++) {
                graph.get(i* this.problem.data.getnMachines()+j).add(new Edge(i* this.problem.data.getnMachines()+j+1, this.problem.data.getDuration()[i][j]));
            }
        }
        for(int i = 0; i < this.problem.data.getnMachines()*this.problem.data.getnJobs(); i++){
            if (state.done.get(i)) {
                int job = i / this.problem.data.getnMachines();
                int op = i % this.problem.data.getnMachines();
                int m = this.problem.data.getMachine()[job][op];
                for (int j : this.problem.data.getTasks()[m]) {
                    if (j>i && state.done.get(j)) {
                        if (state.est[job][op]< state.est[j/this.problem.data.getnMachines()][j%this.problem.data.getnMachines()]){
                            graph.get(i).add(new Edge(j, this.problem.data.getDuration()[job][op]));
                        }else{
                            graph.get(j).add(new Edge(i, this.problem.data.getDuration()[j/this.problem.data.getnMachines()][j%this.problem.data.getnMachines()]));
                        }
                    }
                    else if (!state.done.get(j)){
                        graph.get(i).add(new Edge(j, this.problem.data.getDuration()[job][op]));
                    }
                }
            }else{
                int job = i / this.problem.data.getnMachines();
                int op = i % this.problem.data.getnMachines();
                int m = this.problem.data.getMachine()[job][op];
                if(state.est[job][op]+this.problem.data.getDuration()[job][op] < makespan ){
                    int minNode = -1;
                    for (int j : this.problem.data.getTasks()[m]) {
                        if (!state.done.get(j)) {
                            if (state.est[j / this.problem.data.getnMachines()][j % this.problem.data.getnMachines()] + this.problem.data.getDuration()[j / this.problem.data.getnMachines()][j % this.problem.data.getnMachines()] >= makespan) {
                                if (minNode==-1 || state.est[j / this.problem.data.getnMachines()][j % this.problem.data.getnMachines()] < state.est[minNode / this.problem.data.getnMachines()][minNode % this.problem.data.getnMachines()]){
                                    minNode = j;
                                }
                            }
                        }
                    }
                    if (minNode != -1) {
                        graph.get(minNode).add(new Edge(i, this.problem.data.getDuration()[minNode / this.problem.data.getnMachines()][minNode % this.problem.data.getnMachines()]));
                    }
                }

            }

        }

        int[] inDegree = new int[V];
        for (int u = 0; u < V; u++) {
            for (Edge edge : graph.get(u)) {
                inDegree[edge.to]++;
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < V; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            topoOrder.add(u);

            for (Edge edge : graph.get(u)) {
                int v = edge.to;
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.add(v);
                }
            }
        }

        if (topoOrder.size() != V) {
            return null;
        }

        return topoOrder;
    }

}
