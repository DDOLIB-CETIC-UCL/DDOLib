package org.ddolib.ddo.core.mdd;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.cache.Threshold;
import org.ddolib.ddo.core.compilation.CompilationInput;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import static org.ddolib.util.MathUtil.saturatedAdd;
import static org.ddolib.util.MathUtil.saturatedDiff;

/**
 * This class implements the decision diagram as a linked structure.
 *
 * @param <T> the type of state
 * @param <K> the type of key
 */
public final class LinkedDecisionDiagramWithCache<T, K> extends LinkedDecisionDiagram<T, K> {

    /**
     * Depth of the last exact layer
     */
    private int depthLEL = -1;


    @Override
    public void compile(CompilationInput<T, K> input) {
        // make sure we don't have any stale data left
        this.clear();

        // initialize the compilation
        final int maxWidth = input.maxWidth();
        final SubProblem<T> residual = input.residual();
        final Node root = new Node(residual.getValue());
        this.pathToRoot = residual.getPath();
        this.nextLayer.put(residual.getState(), root);
        this.debugLevel = input.debugLevel();

        dotStr.append("digraph ").append(input.compilationType().toString().toLowerCase()).append("{\n");

        // proceed to compilation
        final Problem<T> problem = input.problem();
        final Relaxation<T> relax = input.relaxation();
        final VariableHeuristic<T> var = input.variableHeuristic();
        final NodeSubProblemComparator<T> ranking = new NodeSubProblemComparator<>(input.stateRanking());
        final DominanceChecker<T, K> dominance = input.dominance();
        final SimpleCache<T> cache = input.cache().get();
        double bestLb = input.bestLB();

        final Set<Integer> variables = varSet(input);

        int depthGlobalDD = residual.getPath().size();
        int depthCurrentDD = 0;
        int initialDepth = residual.getPath().size();

        Set<NodeSubProblem<T>> currentCutSet = new HashSet<>();
        // list of depth for the current relax compilation of the DD
        ArrayList<Integer> listDepths = new ArrayList<>();
        // the list of NodeSubProblem of the corresponding depth
        ArrayList<ArrayList<NodeSubProblem<T>>> nodeSubProblemPerLayer = new ArrayList<>();
        // the list of Threshold of the corresponding depth
        ArrayList<ArrayList<Threshold>> layersThresholds = new ArrayList<>();
        // list of nodes pruned
        ArrayList<NodeSubProblem<T>> pruned = new ArrayList<>();

        while (!variables.isEmpty()) {
            Integer nextVar = var.nextVariable(variables, nextLayer.keySet().iterator());
            // change the layer focus: what was previously the next layer is now
            // becoming the current layer
            this.prevLayer.clear();
            for (NodeSubProblem<T> n : this.currentLayer) {
                this.prevLayer.put(n.node, n);
            }
            this.currentLayer.clear();

            for (Entry<T, Node> e : this.nextLayer.entrySet()) {
                T state = e.getKey();
                Node node = e.getValue();
                if (node.getNodeType() != NodeType.EXACT || !dominance.updateDominance(state, depthGlobalDD, node.value)) {
                    double fub = input.fub().fastUpperBound(state, variables);
                    double rub = saturatedAdd(node.value, fub);
                    node.setFub(fub);
                    this.currentLayer.add(new NodeSubProblem<>(state, rub, node));
                }
            }

            // prunes the current layer with the current values of the cache
            pruned.clear();
            if (depthGlobalDD > initialDepth) {
                for (NodeSubProblem<T> n : this.currentLayer) {
                    if (cache.getLayer(depthGlobalDD).containsKey(n.state) && cache.getThreshold(n.state, depthGlobalDD).isPresent() &&
                            n.node.value <= cache.getThreshold(n.state, depthGlobalDD).get().getValue()) {
                        pruned.add(n);
                    }
                }
            }
            this.currentLayer.removeAll(pruned);
            this.nextLayer.clear();

            if (this.currentLayer.isEmpty()) {
                // there is no feasible solution to this subproblem, we can stop the compilation here
                return;
            }

            if (nextVar == null) {
                // Some variables simply can't be assigned
                clear();
                return;
            } else {
                variables.remove(nextVar);
            }


            // If the current layer is too large, we need to shrink it down. 
            // Whether this shrinking down means that we want to perform a restriction
            // or a relaxation depends on the type of compilation which has been 
            // requested from this decision diagram  
            //
            // IMPORTANT NOTE:
            // The check is on depth 2 because the parent of the current layer is saved
            // if a LEL is to be remembered. In order to be sure
            // to make progress, we must be certain to develop AT LEAST one layer per 
            // mdd compiled otherwise the LEL is going to be the root of this MDD (and
            // we would be stuck in an infinite loop)

            if (depthCurrentDD >= 2 && this.currentLayer.size() > maxWidth) {
                switch (input.compilationType()) {
                    case Restricted:
                        exact = false;
                        restrict(maxWidth, ranking);
                        break;
                    case Relaxed:
                        if (exact) {
                            exact = false;
                            if (input.cutSetType() == CutSetType.LastExactLayer) {
                                cutset.addAll(prevLayer.values());
                                depthLEL = depthCurrentDD - 1;
                            }
                        }
                        relax(maxWidth, ranking, relax);
                        break;
                    case Exact:
                        /* nothing to do */
                        break;
                }
            }
            for (NodeSubProblem<T> n : this.currentLayer) {
                if (input.exportAsDot() || input.debugLevel() >= 2) {
                    dotStr.append(generateDotStr(n, false));
                }
                if (n.ub <= input.bestLB()) {
                    continue;
                } else {
                    final Iterator<Integer> domain = problem.domain(n.state, nextVar);
                    while (domain.hasNext()) {
                        final int val = domain.next();
                        final Decision decision = new Decision(nextVar, val);

                        branchOn(n, decision, problem);
                    }
                }
                // Compute cutset: exact parent nodes of relaxed nodes of the current nodes are put in the cutset
                if (input.compilationType() == CompilationType.Relaxed && !exact && depthCurrentDD >= 2 && input.cutSetType() == CutSetType.Frontier) {
                    if (variables.isEmpty() && n.node.getNodeType() == NodeType.EXACT) {
                        currentCutSet.add(n);
                    }
                    if (n.node.getNodeType() == NodeType.RELAXED) {
                        for (Edge e : n.node.edges) {
                            Node origin = e.origin;
                            if (origin.getNodeType() == NodeType.EXACT) {
                                currentCutSet.add(prevLayer.get(origin));
                            }
                        }
                    }
                }
            }

            // Compute the list of sub-problems per layer with the current layer
            // Initialize the list of thresholds per layer to their default values

            if (input.compilationType() == CompilationType.Relaxed) {
                listDepths.add(depthGlobalDD);
                nodeSubProblemPerLayer.add(new ArrayList<>());
                layersThresholds.add(new ArrayList<>());
                for (NodeSubProblem<T> n : this.currentLayer) {
                    nodeSubProblemPerLayer.get(depthCurrentDD).add(n);
                    layersThresholds.get(depthCurrentDD).add(new Threshold(Integer.MAX_VALUE, false));
                }
            }

            depthGlobalDD += 1;
            depthCurrentDD += 1;
        }
        if (input.compilationType() == CompilationType.Relaxed && input.cutSetType() == CutSetType.Frontier) {
            cutset.addAll(currentCutSet);
        }

        // finalize: find best
        for (Node n : nextLayer.values()) {
            if (best == null || n.value > best.value) {
                best = n;
            }
        }

        if (input.exportAsDot() || input.debugLevel() > 0) {
            for (Entry<T, Node> entry : nextLayer.entrySet()) {
                T state = entry.getKey();
                Node node = entry.getValue();
                NodeSubProblem<T> subProblem = new NodeSubProblem<>(state, best.value, node);
                dotStr.append(generateDotStr(subProblem, true));
            }
        }


        // Compute the local bounds of the nodes in the mdd *iff* this is a relaxed mdd
        if (input.compilationType() == CompilationType.Relaxed) {

            if (!cutset.isEmpty()) {
                computeLocalBounds();

                for (NodeSubProblem<T> n : cutset) {
                    if (n.node.isMarked) {
                        n.node.isInExactCutSet = true;
                    }
                }

                markNodesAboveExactCutSet(nodeSubProblemPerLayer, input.cutSetType());
                // update the cache to improve the next computation of the BB
                computeAndUpdateThreshold(cache, listDepths, nodeSubProblemPerLayer, layersThresholds, bestLb, input.cutSetType());
            }
        }

        if (debugLevel >= 1 && input.compilationType() != CompilationType.Relaxed) {
            checkFub(input.problem());
        }
    }


    // --- UTILITY METHODS -----------------------------------------------

    private void markNodesAboveExactCutSet(ArrayList<ArrayList<NodeSubProblem<T>>> nodePerLayer, CutSetType cutSetType) {
        HashSet<Node> current = new HashSet<>();
        HashSet<Node> parent = new HashSet<>();

        if (cutSetType == CutSetType.LastExactLayer) {
            for (NodeSubProblem<T> n : nodePerLayer.get(depthLEL)) {
                parent.add(n.node);
            }
        } else {
            parent.addAll(nextLayer.values());
        }

        while (!parent.isEmpty()) {
            HashSet<Node> tmp = current;
            current = parent;
            parent = tmp;
            parent.clear();

            for (Node n : current) {
                for (Edge e : n.edges) {
                    // Note: we might want to do something and stop as soon as the lel has been reached
                    Node origin = e.origin;
                    parent.add(origin);
                    if ((n.isInExactCutSet || n.isAboveExactCutSet) && origin.getNodeType() == NodeType.EXACT && !origin.isInExactCutSet) {
                        origin.isAboveExactCutSet = true;
                    }
                }
            }
        }
    }

    /**
     * perform the bottom up traversal of the mdd to compute and update the cache
     */
    private void computeAndUpdateThreshold(SimpleCache<T> simpleCache, ArrayList<Integer> listDepth, ArrayList<ArrayList<NodeSubProblem<T>>> nodePerLayer, ArrayList<ArrayList<Threshold>> currentCache, double lb, CutSetType cutSetType) {
        for (int j = listDepth.size() - 1; j >= 0; j--) {
            int depth = listDepth.get(j);
            for (int i = 0; i < nodePerLayer.get(j).size(); i++) {
                NodeSubProblem<T> sub = nodePerLayer.get(j).get(i);
                if (simpleCache.getLayer(depth).containsKey(sub.state) && simpleCache.getLayer(depth).get(sub.state).isPresent() &&
                        sub.node.value <= simpleCache.getLayer(depth).get(sub.state).get().getValue()) {
                    double value = simpleCache.getLayer(depth).get(sub.state).get().getValue();
                    currentCache.get(j).get(i).setValue(value);
                } else {
                    if (sub.ub <= lb) {
                        double rub = saturatedDiff(sub.ub, sub.node.value);
                        double value = saturatedDiff(lb, rub);
                        currentCache.get(j).get(i).setValue(value);
                    } else if (sub.node.isInExactCutSet) {
                        if (sub.node.suffix != null && saturatedAdd(sub.node.value, sub.node.suffix) <= lb) {
                            double value = Math.min(currentCache.get(j).get(i).getValue(), saturatedDiff(lb, sub.node.suffix));
                            currentCache.get(j).get(i).setValue(value);
                        } else {
                            currentCache.get(j).get(i).setValue(sub.node.value);
                        }
                    }
                    if (sub.node.getNodeType() == NodeType.EXACT) {
                        if (sub.node.isAboveExactCutSet && !sub.node.isInExactCutSet) {
                            currentCache.get(j).get(i).setExplored(true);
                        }
                        if (cutSetType == CutSetType.LastExactLayer && sub.node.value < currentCache.get(j).get(i).getValue() && sub.node.isInExactCutSet)
                            currentCache.get(j).get(i).setExplored(true);
                        if (currentCache.get(j).get(i).isExplored()) {
                            simpleCache.getLayer(depth).update(sub.state, currentCache.get(j).get(i));
                        }
                    }
                }
                for (Edge e : sub.node.edges) {
                    Node origin = e.origin;
                    int index = -1;
                    for (int k = 0; k < nodePerLayer.get(j - 1).size(); k++) {
                        if (nodePerLayer.get(j - 1).get(k).node.equals(origin)) {
                            index = k;
                            break;
                        }
                    }
                    double value = Math.min(currentCache.get(j - 1).get(index).getValue(), saturatedDiff(currentCache.get(j).get(i).getValue(), e.weight));
                    currentCache.get(j - 1).get(index).setValue(value);
                }
            }
        }
    }
}