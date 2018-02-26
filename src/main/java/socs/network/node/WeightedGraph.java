package socs.network.node;

import java.util.*;

// used and modified dijkstra code from:  http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
public class WeightedGraph {

    // simulatedIP for each vertex (router)
    private final String value;
    // link cost
    private final int cost;
    private final WeightedGraph parent;
    public ArrayList<WeightedGraph> children = new ArrayList<>();
    private static HashSet<WeightedGraph> settledNodes, unsettledNodes;
    private static HashMap<WeightedGraph, Integer> distances;
    private static HashMap<WeightedGraph, WeightedGraph> predecessors;

    WeightedGraph(String value, int cost, WeightedGraph parent) {
        this.value = value;
        this.cost = cost;
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }

    public WeightedGraph getParent() {
        return parent;
    }

    public static boolean hasBeenVisited(WeightedGraph node, String target) {
        return node.value.equals(target) || node.cost != 0 && hasBeenVisited(node.parent, target);
    }

    // returns path from root to target, returns null if no path found
    public static LinkedList<WeightedGraph> getPath(WeightedGraph root, WeightedGraph target) {
        dijkstraSetup(root);
        LinkedList<WeightedGraph> path = new LinkedList<>();
        WeightedGraph step = target;
        // check if path exists
        if (predecessors.get(step) == null)
            return null;
        path.add(step);
        while(predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // put it in correct order
        Collections.reverse(path);
        return path;
    }

    // sets up the HashMaps and HashSet used for Dijkstra's algorithm
    private static void dijkstraSetup(WeightedGraph root) {
        settledNodes = new HashSet<>();
        unsettledNodes = new HashSet<>();
        distances = new HashMap<>();
        predecessors = new HashMap<>();
        distances.put(root, 0);
        unsettledNodes.add(root);

        while(!unsettledNodes.isEmpty()) {
            WeightedGraph node = getMinimum(unsettledNodes);
            unsettledNodes.remove(node);
            settledNodes.add(node);
            findMinimalDistances(node);
        }
    }

    private static void findMinimalDistances(WeightedGraph node) {
        for (WeightedGraph target : node.children) {
            if (!settledNodes.contains(target) && getShortestDistance(target) > getShortestDistance(node)+target.cost) {
                distances.put(target, getShortestDistance(node) + target.cost);
                predecessors.put(target, node);
                unsettledNodes.add(target);
            }
        }
    }

    private static int getShortestDistance(WeightedGraph destination) {
        Integer d = distances.get(destination);
        if (d == null)
            return Integer.MAX_VALUE;
        else
            return d;
    }

    private static WeightedGraph getMinimum(HashSet<WeightedGraph> nodes) {
        WeightedGraph minimum = null;
        for (WeightedGraph node : nodes) {
            if (minimum == null)
                minimum = node;
            else if (getShortestDistance(node) < getShortestDistance(minimum))
                minimum = node;
        }
        return minimum;
    }
}
