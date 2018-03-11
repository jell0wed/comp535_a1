package socs.network.node;

import com.google.common.collect.Lists;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.*;

// used and modified dijkstra code from:  http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
public class WeightedGraph {
    public static class Vertex {
        public String value;
        public Vertex(String val) {
            this.value = val;
        }
    }

    public static class Edge {
        public Vertex source;
        public Vertex destination;
        public int cost;

        public Edge(Vertex src, Vertex dst, int cost) {
            this.source = src;
            this.destination = dst;
            this.cost = cost;
        }
    }

    private final Set<Vertex> vertices = new TreeSet<>(Comparator.comparing(o -> o.value));
    private final Set<Edge> edges = new HashSet<>();
    private final HashMap<String, Vertex> visitedNodes = new HashMap<>();

    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Integer> distance;

    public WeightedGraph() {

    }

    public static WeightedGraph createFromLSD(RouterDescription localRouter, LinkStateDatabase lsd) {
        WeightedGraph graph = new WeightedGraph();

        Queue<Vertex> queue = new LinkedList<>();
        Vertex root = new Vertex(localRouter.getSimulatedIPAddress());

        graph.vertices.add(root);
        queue.add(root);

        while(!queue.isEmpty()) {
            Vertex node = queue.poll();
            LSA nodeLSA = lsd.getDiscoveredRouter(node.value);
            for (LinkDescription link : nodeLSA.links) {
                if(!graph.visitedNodes.containsKey(link.linkID) && !link.linkID.equalsIgnoreCase(node.value)) {
                    // create a vertex
                    Vertex newNode = new Vertex(link.linkID);
                    graph.vertices.add(newNode);

                    // create an edge
                    int bestDistance = Integer.min(lsd.getBestDistanceForRouter(node.value, newNode.value), lsd.getBestDistanceForRouter(newNode.value, node.value));
                    Edge newEdge = new Edge(node, newNode, bestDistance);
                    graph.edges.add(newEdge);

                    Edge newEdge2 = new Edge(newNode, node, bestDistance);
                    graph.edges.add(newEdge2);

                    graph.visitedNodes.put(link.linkID, newNode);
                    queue.add(newNode);
                }
            }
        }

        return graph;
    }

    public void execute(Vertex source) {
        settledNodes = new HashSet<Vertex>();
        unSettledNodes = new HashSet<Vertex>();
        distance = new HashMap<Vertex, Integer>();
        predecessors = new HashMap<Vertex, Vertex>();
        distance.put(source, 0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            Vertex node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(Vertex node) {
        List<Vertex> adjacentNodes = getNeighbors(node);
        for (Vertex target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private int getDistance(Vertex node, Vertex target) {
        for (Edge edge : edges) {
            if (edge.source.equals(node)
                    && edge.destination.equals(target)) {
                return edge.cost;
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> neighbors = new ArrayList<Vertex>();
        for (Edge edge : edges) {
            if (edge.source.equals(node)
                    && !isSettled(edge.destination)) {
                neighbors.add(edge.destination);
            }
        }
        return neighbors;
    }

    private Vertex getMinimum(Set<Vertex> vertexes) {
        Vertex minimum = null;
        for (Vertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(Vertex vertex) {
        return settledNodes.contains(vertex);
    }

    private int getShortestDistance(Vertex destination) {
        Integer d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    public LinkedList<Vertex> getPath(Vertex target) {
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

    public Vertex getVertex(String ip) {
        return this.visitedNodes.get(ip);
    }
}
