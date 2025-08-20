package org.ddolib.astar.examples.JobShop;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.*;

import static java.lang.Math.*;

public class JSProblem implements Problem<JSState> {

    final JSInstance data;
    public  HashMap<Integer, ArrayList<Integer>> pred;
    public HashMap<Integer, ArrayList<Integer>> succ;
    private double percentage;
    public HashMap<String, Integer> fullExplored;
    private final Random rdPath;
    private final Random rdRemove;
    private HashMap<Integer, Integer> starts;

    public JSProblem(JSInstance data) {
        this.data = data;
        pred = new HashMap<>();
        succ = new HashMap<>();
        for (int i = 0; i < data.getnJobs() * data.getnMachines(); i++) {
            pred.put(i, new ArrayList<>());
            succ.put(i, new ArrayList<>());
        }
        this.percentage = 0.15;
        this.fullExplored = new HashMap<String, Integer>();
        this.rdPath =  new Random(42);
        this.rdRemove = new Random(42);
        this.starts = new HashMap<>();
    }

    @Override
    public int nbVars() {
        return data.getnJobs()*data.getnMachines();
    }

    @Override
    public JSState initialState() {
        int[][] est = new int[data.getnJobs()][data.getnMachines()];
        for (int i = 0; i < data.getnJobs(); i++) {
            for (int j = 0; j < data.getnMachines(); j++) {
                est[i][j] = starts.getOrDefault(i*data.getnMachines()+j, 0);
            }
        }
        for (int k: pred.keySet()) {
            ArrayList<Integer> succs = succ.get(k);
            for (int succ: succs) {
                int jobId = succ / data.getnMachines();
                int opId = succ % data.getnMachines();
                int job = k / data.getnMachines();
                int op = k % data.getnMachines();
                est[jobId][opId] = max(est[jobId][opId],est[job][op]+data.getDuration()[job][op]);
            }
        }
        return new JSState(est);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(JSState state, int var) {
        List<Integer> ret = new ArrayList<>();
        int makespan = getMakespan(state);
        for (int i = 0; i < data.getnJobs(); i++) {
            boolean add = true;
            for (int j = 0; j < data.getnMachines(); j++) {
                if (!state.done.get(i * data.getnMachines() + j)) {
                    if (state.est[i][j] + data.getDuration()[i][j] >= makespan) {
                        for (int pred: this.pred.get(i * data.getnMachines() + j)) {
                            if (!state.done.get(pred)) {
                                add = false;
                            }
                        }
                        if (add) {
                            ret.add(i * data.getnMachines() + j);
                        }else{
                            break;
                        }
                    }

                }
            }
        }
        return ret.iterator();
    }

    @Override
    public JSState transition(JSState state, Decision decision) {
        int jobId = decision.val() / data.getnMachines();
        int opId = decision.val() % data.getnMachines();
        int machine = data.getMachine()[jobId][opId];
        JSState newState = new JSState(state.est, state.done,state.order);
        newState.done.set(decision.val());
        for (int j : data.getTasks()[machine]) {
            if (!newState.done.get(j)) {
                int job = j / data.getnMachines();
                int op = j % data.getnMachines();
                newState.est[job][op] = max(newState.est[job][op], newState.est[jobId][opId] + data.getDuration()[jobId][opId]);
                for (int succ: succ.get(j)){
                    if (!newState.done.get(succ)) {
                        updateTime(succ, j, newState);
                    }
                }
            }
        }
        for (int j= 0; j < data.getnMachines(); j++) {
            if (!newState.done.get(jobId*data.getnMachines()+j)) {
                newState.est[jobId][j] = max(newState.est[jobId][j], newState.est[jobId][opId] + data.getDuration()[jobId][opId]);
            }
        }
        newState.order[newState.done.cardinality()-1]=decision.val();
        return newState;
    }

    public void updateTime(int succ, int pred, JSState newState){
        newState.est[succ / data.getnMachines()][succ % data.getnMachines()] = max(newState.est[succ / data.getnMachines()][succ % data.getnMachines()], newState.est[pred/ data.getnMachines()][pred % data.getnMachines()] + data.getDuration()[pred / data.getnMachines()][pred % data.getnMachines()]);
        for (int s: this.succ.get(succ)){
            if (!newState.done.get(succ)) {
                updateTime(s, succ, newState);
            }
        }

    }

    @Override
    public double transitionCost(JSState state, Decision decision) {
        int makespan = getMakespan(state);
        int jobId = decision.val() / data.getnMachines();
        int opId = decision.val() % data.getnMachines();
        return -(max(makespan, state.est[jobId][opId]+data.getDuration()[jobId][opId]) - makespan);
    }

    public void fixStartTime(int start1, int task1){
       this.starts.put(task1, start1);
    }

    public void addInFullExplored(JSState state, int nChildren){
        this.fullExplored.put(Arrays.toString(state.order),nChildren);
    }
    public void childrenFullExplored(JSState state){
        int c = this.fullExplored.getOrDefault(Arrays.toString(state.order),1);
        c = c-1;
        this.fullExplored.put(Arrays.toString(state.order),c);
        int[] s = state.order.clone();
        int l = state.done.cardinality()-1;
        while(c==0&&l>0){
            s[l]=0;
            c = this.fullExplored.get(Arrays.toString(s));
            c = c-1;
            this.fullExplored.put(Arrays.toString(s),c);
            l-=1;
        }
    }
    public boolean fullExplored(JSState state){
        return this.fullExplored.getOrDefault(Arrays.toString(state.order),1)==0;
    }

    public void increasePercentage(){
        this.percentage+=0.05;
        this.percentage = min(this.percentage,0.35);
    }
    public void resetPercentage(){
        this.percentage=0.25;
    }

    public void addPrecedencesConstraint(ArrayList<Precedence> precedences) {
        for (int i = 0; i < precedences.size(); i++) {
            Precedence p = precedences.get(i);
            this.pred.get(p.j).add(p.i);
            this.succ.get(p.i).add(p.j);
        }
    }
    public void addPrecedencesConstraint2(ArrayList<Precedence> precedences) {
        for (int i = 0; i < precedences.size(); i++) {
            Precedence p = precedences.get(i);
            this.pred.get(p.j).add(p.i);
            this.succ.get(p.i).add(p.j);
//            System.out.println("problem.addPrecedenceConstraint(new Precedence( "+p.i+","+ p.j+"))");
        }
    }
    public void addPrecedenceConstraint(Precedence precedence) {
        Precedence p = precedence;
        this.pred.get(p.j).add(p.i);
        this.succ.get(p.i).add(p.j);

    }
    public Integer getMakespan(JSState s) {
        int makespan = 0;
        for (int i = s.done.nextSetBit(0); i >= 0; i = s.done.nextSetBit(i + 1)) {
            int job = i / data.getnMachines();
            int op = i % data.getnMachines();
            makespan = max(makespan, s.est[job][op] + data.getDuration()[job][op]);
        }
        return makespan;
    }

    private int[] findLongestPaths(List<List<Edge>> graph, int V, int source, int[] parent) {
        List<Integer> topo = topologicalSort(V, graph);

        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MIN_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        for (int u : topo) {
            if (dist[u] != Integer.MIN_VALUE) {
                for (Edge edge : graph.get(u)) {
                    if (dist[edge.to] < dist[u] + edge.weight) {
                        dist[edge.to] = dist[u] + edge.weight;
                        parent[edge.to] = u;  // Track the predecessor
                    }
                }
            }
        }

        return dist;
    }

    private List<Integer> reconstructPath(int dest, int[] parent) {
        List<Integer> path = new ArrayList<>();
        for (int at = dest; at != -1; at = parent[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }


    private List<Integer> topologicalSort(int V, List<List<Edge>> graph) {
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
            throw new IllegalStateException("Graph has a cycle — topological sort not possible.");
        }

        return topoOrder;
    }

    public boolean getRelaxation(JSState sol){
        List<List<Edge>> graph = new ArrayList<>();
        for (int i = 0; i < data.getnJobs()* data.getnMachines()+2; i++) {
            graph.add(new ArrayList<>());
        }
        ArrayList<Precedence> precs = new ArrayList<>();
        for(int i = 0; i < data.getnJobs(); i++){
            graph.get(data.getnJobs()* data.getnMachines()).add(new Edge(i* data.getnMachines(),0));
            graph.get(i* data.getnMachines()+ data.getnMachines() - 1).add(new Edge(data.getnJobs()* data.getnMachines()+1,data.getDuration()[i][data.getnMachines()-1]));
            for (int j = 0; j < data.getnMachines()-1; j++) {
                graph.get(i* data.getnMachines()+j).add(new Edge(i* data.getnMachines()+j+1, data.getDuration()[i][j]));
            }
        }
        for (int m=0; m<data.getnMachines(); m++) {
            Integer[] machines = Arrays.stream(data.getTasks()[m]).boxed().toArray(Integer[]::new);
            Arrays.sort(machines, Comparator.comparingInt(i ->
                    sol.est[i / data.getnMachines()][i % data.getnMachines()]
            ));
            for (int j = 0; j < data.getnJobs()-1; j++) {
                graph.get(machines[j]).add(new Edge(machines[j+1],data.getDuration()[machines[j]/ data.getnMachines()][machines[j]%data.getnMachines()]));
                precs.add(new Precedence(machines[j],machines[j+1]));
            }

        }
        int[] parent = new int[data.getnJobs()* data.getnMachines()+2];
        int[] distances = findLongestPaths(graph, data.getnJobs()* data.getnMachines()+2, data.getnJobs()* data.getnMachines(), parent);
        int distMax = distances[data.getnJobs()* data.getnMachines()+1];
        List<Integer> path = reconstructPath(data.getnJobs()* data.getnMachines()+1, parent);
        int dist = distMax;
        int toRemove = (int) ceil(precs.size()*percentage);
        System.out.println("Remove : "+ toRemove);
        int removed = 0;

        while(distMax-dist<10) {
            if (precs.isEmpty()){
                return false;
            }
            int rand = rdPath.nextInt(path.size() - 3) +1;
            int elem = path.get(rand);
            if (path.get(rand+1)/ data.getnMachines() == elem / data.getnMachines()){
                continue;
            }
            graph.get(elem).remove(new Edge(path.get(rand+1), data.getDuration()[elem/data.getnMachines()][elem%data.getnMachines()]));
            precs.remove(new Precedence(elem, path.get(rand+1)));
            System.out.println("Remove : Precedence "+  elem + " - > " + path.get(rand+1));
            List<Edge> p = graph.get(path.get(rand+1));
//            for (Edge edge : p) {
//                if (edge.to/ data.getnMachines() != path.get(rand+1)/ data.getnMachines()) {
//                    precs.remove(new Precedence(path.get(rand+1), edge.to));
//                    p.remove(edge);
//                    System.out.println("Remove : Precedence "+ path.get(rand+1) + " - > " + edge.to);
//                    break;
//                }
//            }
            distances = findLongestPaths(graph, data.getnJobs()* data.getnMachines()+2, data.getnJobs()* data.getnMachines(), parent);
            dist = distances[data.getnJobs()* data.getnMachines()+1];
            path = reconstructPath(data.getnJobs()* data.getnMachines()+1, parent);
            removed+=1;
        }

        toRemove-= removed;
        System.out.println("dist : "+ dist+ " Remove2 : "+ toRemove);
        for(int i=0; i<toRemove; i++){
            int rand = rdRemove.nextInt(precs.size()) ;
            Precedence p = precs.get(rand);
            List<Edge> pr = graph.get(p.j);
//            for (Edge edge : pr) {
//                if (edge.to/ data.getnMachines() != p.j/ data.getnMachines()) {
//                    precs.remove(new Precedence(p.j, edge.to));
//                    System.out.println("Remove : Precedence "+ p.j + " - > " + edge.to);
//                    pr.remove(edge);
//                    break;
//                }
//            }
            precs.remove(p);
            graph.get(p.i).remove(new Edge(p.j, data.getDuration()[p.i/ data.getnMachines()][p.i%data.getnMachines()]));
            System.out.println("Remove : Precedence "+ p.i + " - > " + p.j);

        }
        this.addPrecedencesConstraint2(precs);
        return true;

    }


    public void removePrecedenceConstraint(){
        this.pred.clear();
        this.succ.clear();
        for (int i = 0; i < data.getnJobs() * data.getnMachines(); i++) {
            pred.put(i, new ArrayList<>());
            succ.put(i, new ArrayList<>());
        }
        ArrayList<Precedence> preds =  new ArrayList<>();
        for(int i =0; i<data.getnJobs(); i++) {
            for(int j=0; j<data.getnMachines()-1; j++) {
                preds.add(new Precedence(i*data.getnMachines()+j,i*data.getnMachines()+j+1));
            }
        }
        this.addPrecedencesConstraint(preds);

    }
}
