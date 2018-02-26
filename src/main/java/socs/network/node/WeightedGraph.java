package socs.network.node;

import java.util.ArrayList;

public class WeightedGraph {

    // simulatedIP for each vertex (router)
    private final String value;
    // link cost
    private final int cost;
    private final WeightedGraph parent;
    private ArrayList<WeightedGraph> children = new ArrayList<>();

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

    public void addChild(WeightedGraph node) {
        if (!hasBeenVisited(node.parent, node.value)) {
            children.add(node);
        }
    }

    public WeightedGraph getChild(int index) {
        return children.get(index);
    }

    private boolean hasBeenVisited(WeightedGraph node, String target) {
        return node.value.equals(target) || node.cost != 0 && hasBeenVisited(node.parent, target);
    }
}
