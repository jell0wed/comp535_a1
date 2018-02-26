package socs.network.node;

import java.util.ArrayList;
import java.util.HashSet;

public class WeightedGraph {

    // simulatedIP for each vertex (router)
    private final String value;
    // link cost
    private final int cost;
    private final WeightedGraph parent;
    public ArrayList<WeightedGraph> children = new ArrayList<>();

    WeightedGraph(String value, int cost, WeightedGraph parent) {
        this.value = value;
        this.cost = cost;
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }

    public int getCost() {
        return cost;
    }

    public WeightedGraph getParent() {
        return parent;
    }

    public static boolean hasBeenVisited(WeightedGraph node, String target) {
        return node.value.equals(target) || node.cost != 0 && hasBeenVisited(node.parent, target);
    }
}
