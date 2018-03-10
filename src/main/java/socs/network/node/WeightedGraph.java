package socs.network.node;

import com.google.common.collect.Lists;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.*;

// used and modified dijkstra code from:  http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
public class WeightedGraph {
    private static HashMap<String, WeightedGraph> visitedNodes = new HashMap<>();

    // simulatedIP for each vertex (router)
    private final String value;
    // link cost
    private final int cost;

    private Set<WeightedGraph> connectedNodes = new HashSet<>();

    public WeightedGraph(String val, int cost) {
        this.value = val;
        this.cost = cost;
    }

    public static WeightedGraph createFromLSD(RouterDescription localRouter, LinkStateDatabase lsd) {
        visitedNodes = new HashMap<>();
        Queue<WeightedGraph> queue = new LinkedList<>();
        WeightedGraph root = new WeightedGraph(localRouter.getSimulatedIPAddress(), 0);

        queue.add(root);

        while(!queue.isEmpty()) {
            WeightedGraph node = queue.poll();
            LSA nodeLSA = lsd.getDiscoveredRouter(node.value);
            for (LinkDescription link : nodeLSA.links) {
                if(!visitedNodes.containsKey(link.linkID)) {
                    WeightedGraph newNode = new WeightedGraph(link.linkID, link.tosMetrics);
                    queue.add(newNode);
                    node.connectedNodes.add(newNode);

                    visitedNodes.put(link.linkID, newNode);
                }
            }
        }

        return root;
    }

    public static WeightedGraph getGraphNode(String simulatedIp) {
        return visitedNodes.get(simulatedIp);
    }

    public List<WeightedGraph> getShortestPath(WeightedGraph root, WeightedGraph target) {
        HashMap<WeightedGraph, Integer> distances = new HashMap<>();
        HashMap<WeightedGraph, WeightedGraph> previous = new HashMap<>();
        Set<WeightedGraph> vertices = new HashSet<>();
        vertices.add(root);

        for(WeightedGraph node: visitedNodes.values()) {
            distances.put(node, Integer.MAX_VALUE);
            previous.put(node, null);
            vertices.add(node);
        }

        distances.put(root, 0);

        while(!vertices.isEmpty()) {
            WeightedGraph u = vertices.stream().sorted((o1, o2) -> Integer.compare(o1.cost, o2.cost)).findFirst().get();
            // it must be the case that u is present
            vertices.remove(u);

            for(WeightedGraph v : u.connectedNodes) {
                int alt = distances.get(u) + v.cost;
                if(alt < distances.get(v)) {
                    distances.put(v, alt);
                    previous.put(v, u);
                }
            }
        }

        // get the path by traversing the previous map backwards
        LinkedList<WeightedGraph> shortestPath = new LinkedList<>();
        WeightedGraph currNode = target;
        while(target != root) {
            if(currNode == null) {
                return Lists.reverse(shortestPath);
            }

            shortestPath.add(currNode);
            currNode = previous.get(currNode);
        }

        return Lists.reverse(shortestPath);
    }
}
