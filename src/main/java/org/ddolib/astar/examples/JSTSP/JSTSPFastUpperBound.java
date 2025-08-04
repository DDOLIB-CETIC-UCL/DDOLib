package org.ddolib.astar.examples.JSTSP;

import org.ddolib.modeling.FastUpperBound;

import java.util.HashMap;
import java.util.Set;

import static java.lang.Math.max;

public class JSTSPFastUpperBound implements FastUpperBound<JSTSPState> {

    JSTSPProblem problem;

    public JSTSPFastUpperBound(JSTSPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(JSTSPState state, Set<Integer> variables) {
        double min_c = Integer.MAX_VALUE;
        long toolactive = 0L;
        int n = 0;
        for (int i = 0; i < this.problem.instance.n; i++) {
            if ((state.remaining & (1L << i)) == 1L << i) {
                this.problem.ret[i] = true;
                n++;
                if (min_c > this.problem.distanceMatrix[state.currentJobIdx][i]) {
                    min_c = this.problem.distanceMatrix[state.currentJobIdx][i];
                }
            } else {
                this.problem.ret[i] = false;
                toolactive |= this.problem.instance.jobs[i];
            }
        }
        if (min_c == Integer.MAX_VALUE) {
            min_c = 0;

        }

        Integer cost = this.problem.kruskal_value.get(state.remaining);
        if (cost == null) {
            cost = kruskal(this.problem.ret,Long.bitCount(toolactive),n);
            this.problem.kruskal_value.put(state.remaining, cost);
        }
        return - (cost + min_c );
    }
    public Integer kruskal(boolean[] mask, int toolactive,int n) {
        int explored = 0;
        int global_cost = 0;
        HashMap<Integer, PairLi> tools = new HashMap<>();
        for (Edge edge : this.problem.edges) {
            if (mask[edge.v1()] && mask[edge.v2()]) {
                int find_v1 = this.problem.unionFind.find(edge.v1());
                int find_v2 = this.problem.unionFind.find(edge.v2());
                if (find_v1 != find_v2) {
                    explored++;
                    if (this.problem.unionFind.getRank(find_v1) == 0 && this.problem.unionFind.getRank(find_v2) == 0) {
                        this.problem.unionFind.union(edge.v1(), edge.v2());
                        int new_find = this.problem.unionFind.find(edge.v1());
                        long tools_req = this.problem.instance.jobs[edge.v1()] | this.problem.instance.jobs[edge.v2()];
                        int nb_tools_req = Long.bitCount(tools_req);
                        double cost = edge.cost();
                        if (explored==n-1){
                            cost = max(cost,max(nb_tools_req - this.problem.instance.c + max(this.problem.instance.c-toolactive, 0),0));
                        }
                        else if (cost < nb_tools_req - this.problem.instance.c) {
                            cost = nb_tools_req - this.problem.instance.c;
                        }
                        tools.put(new_find, new PairLi(tools_req, cost));
                    } else if (this.problem.unionFind.getRank(find_v1) == 0) {
                        this.problem.unionFind.union(edge.v1(), edge.v2());
                        PairLi p = tools.get(find_v2);
                        p.tools_req = p.tools_req | this.problem.instance.jobs[edge.v1()];
                        int nb_tools_req = Long.bitCount(p.tools_req);
                        p.cost += edge.cost();
                        if (explored==n-1){
                            p.cost = max(p.cost,max(nb_tools_req - this.problem.instance.c + max(this.problem.instance.c-toolactive, 0),0));
                        }
                        else if (p.cost < nb_tools_req - this.problem.instance.c) {
                            p.cost = nb_tools_req - this.problem.instance.c;
                        }
                        tools.replace(find_v2, p);
                    } else if (this.problem.unionFind.getRank(find_v2) == 0) {
                        this.problem.unionFind.union(edge.v1(), edge.v2());
                        PairLi p = tools.get(find_v1);
                        p.tools_req = p.tools_req | this.problem.instance.jobs[edge.v2()];
                        int nb_tools_req = Long.bitCount(p.tools_req);
                        p.cost += edge.cost();
                        if (explored==n-1){
                            p.cost = max(p.cost,max(nb_tools_req - this.problem.instance.c + max(this.problem.instance.c-toolactive, 0),0));
                        }
                        else if (p.cost < nb_tools_req - this.problem.instance.c) {
                            p.cost = nb_tools_req - this.problem.instance.c;
                        }
                        tools.replace(find_v1, p);
                    } else {
                        this.problem.unionFind.union(edge.v1(), edge.v2());
                        int new_find = this.problem.unionFind.find(edge.v1());
                        PairLi p1 = tools.get(find_v1);
                        PairLi p2 = tools.get(find_v2);


                        p1.tools_req = p1.tools_req | p2.tools_req;
                        int nb_tools_req = Long.bitCount(p1.tools_req);
                        p1.cost += edge.cost() + p2.cost;
                        if (explored==n-1){
                            p1.cost = max(p1.cost,max(nb_tools_req - this.problem.instance.c + max(this.problem.instance.c-toolactive, 0),0));
                        }
                        else if (p1.cost < nb_tools_req - this.problem.instance.c) {
                            p1.cost = nb_tools_req - this.problem.instance.c;
                        }
                        tools.replace(new_find, p1);
                    }

                    if (this.problem.unionFind.getRank(edge.v1()) >= this.problem.unionFind.getRank(edge.v2())) {
                        global_cost = (int) tools.get(this.problem.unionFind.find(edge.v1())).cost;
                    } else {
                        global_cost = (int) tools.get(this.problem.unionFind.find(edge.v2())).cost;
                    }
                }
            }
        }
        this.problem.unionFind.reset(this.problem.instance.n);

        return global_cost;
    }
    class PairLi {
        public long tools_req;
        public double cost;

        public PairLi(long tools_req, double cost) {
            this.tools_req = tools_req;
            this.cost = cost;
        }
    }
}
