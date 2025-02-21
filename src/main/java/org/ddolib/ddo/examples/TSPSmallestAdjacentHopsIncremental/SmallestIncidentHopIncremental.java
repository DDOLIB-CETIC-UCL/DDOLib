package org.ddolib.ddo.examples.TSPSmallestAdjacentHopsIncremental;

import java.util.BitSet;

public class SmallestIncidentHopIncremental {
    int baseNode;
    int closestNeighbour;

    SmallestIncidentHopIncremental next;

    public SmallestIncidentHopIncremental(int baseNode, int closestNeighbour, SmallestIncidentHopIncremental next){
        this.baseNode = baseNode;
        this.closestNeighbour = closestNeighbour;
        this.next = next;
    }

    SmallestIncidentHopIncremental updateToRestrictedNodeSet(BitSet nodes, int [][] distance){

        if(!nodes.get(baseNode)) {
            //drop me
            return next.updateToRestrictedNodeSet(nodes, distance);
        }else if(nodes.get(closestNeighbour)){
            //keep me as is
            SmallestIncidentHopIncremental newNext = next.updateToRestrictedNodeSet(nodes,distance);
            if(next == newNext) return this;
            else return new SmallestIncidentHopIncremental(baseNode, closestNeighbour, newNext);
        }else{
            //update me
            int newClosestNeighbour = nodes
                    .stream()
                    .filter(i -> i != baseNode)
                    .boxed()
                    .min((a, b) -> (distance[baseNode][a] - distance[baseNode][b]))
                    .get();

            SmallestIncidentHopIncremental newNext = next.updateToRestrictedNodeSet(nodes,distance);
            return new SmallestIncidentHopIncremental(baseNode, newClosestNeighbour, newNext);
        }
    }


    int sumOfAllHops(int [][] distance){
        if(next == null){
            return distance[baseNode][closestNeighbour];
        }else{
            return distance[baseNode][closestNeighbour] + next.sumOfAllHops(distance);
        }
    }

    int biggestHop(int [][] distance){
        if(next == null){
            return distance[baseNode][closestNeighbour];
        }else{
            return Math.max(distance[baseNode][closestNeighbour],next.sumOfAllHops(distance));
        }
    }

    int computeHeuristics(int [][] distance){
        return sumOfAllHops(distance) - biggestHop(distance);
    }

    String myString(int[][] matrix){
        return "SmallestIncidentHopIncremental(base:" + baseNode + " nearestNeighbour:" + closestNeighbour + " dist:" + matrix[baseNode][closestNeighbour] + ")";
    }

}


