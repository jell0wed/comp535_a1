package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.*;
import java.util.stream.Collectors;

public class LinkStateDatabase {
    public static Set<String> receivedLSAUpdate = new TreeSet<>();
    //linkID => LSAInstance
    HashMap<String, LSA> _store = new HashMap<String, LSA>();

    private RouterDescription rd = null;

    public LinkStateDatabase(RouterDescription routerDescription) {
        rd = routerDescription;
        LSA l = initLinkStateDatabase();
        _store.put(l.linkStateID, l);
    }

    /**
     * output the shortest path from this router to the destination with the given IP address
     */
    String getShortestPath(String destinationIP) {
        //TODO: fill the implementation here
        return null;
    }

    //initialize the linkstate database by adding an entry about the router itself
    private LSA initLinkStateDatabase() {
        LSA lsa = new LSA();
        lsa.linkStateID = rd.simulatedIPAddress;
        lsa.lsaSeqNumber = Integer.MIN_VALUE;
        LinkDescription ld = new LinkDescription();
        ld.linkID = rd.simulatedIPAddress;
        ld.portNum = -1;
        ld.tosMetrics = Integer.MAX_VALUE;
        lsa.links.add(ld);
        return lsa;
    }

    public boolean hasRouterBeenDiscovered(String simulatedIp) {
        return this._store.containsKey(simulatedIp);
    }

    public LSA getDiscoveredRouter(String simulatedIp) {
        return this._store.get(simulatedIp);
    }

    public boolean updateDiscoveredRouter(String simulatedIp, LSA updatedLSA) {
        boolean updated = false;
        LSA existingLSA = this._store.get(simulatedIp);
        if(existingLSA != null) {
            updated = existingLSA.links.addAll(updatedLSA.links);
            for (LinkDescription link : existingLSA.links) {
                for(LinkDescription updatedLink : updatedLSA.links) {
                    link.combine(updatedLink);
                }
            }
        } else {
            existingLSA = updatedLSA;
            updated = true;
        }

        this._store.put(simulatedIp, existingLSA);
        return updated;
    }

    public int getBestDistanceForRouter(String ipFrom, String ipTo) {
        int bestDistance = Integer.MAX_VALUE;
        LSA fromLSA = this._store.get(ipFrom);
        LSA toLSA = this._store.get(ipTo);

        Optional<Integer> minDistanceFrom = fromLSA.links.stream().filter(x -> x.linkID.equalsIgnoreCase(ipTo)).map(x -> x.tosMetrics).min(Integer::compareTo);
        Optional<Integer> minDistanceTo = toLSA.links.stream().filter(x -> x.linkID.equalsIgnoreCase(ipFrom)).map(x -> x.tosMetrics).min(Integer::compareTo);

        if(minDistanceFrom.isPresent()) {
            bestDistance = Integer.min(bestDistance, minDistanceFrom.get());
        }

        if(minDistanceTo.isPresent()) {
            bestDistance = Integer.min(bestDistance, minDistanceTo.get());
        }

        return bestDistance;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LSA lsa : _store.values()) {
            sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
            for (LinkDescription ld : lsa.links) {
                sb.append(ld.linkID).append(",").append(ld.portNum).append(",").
                        append(ld.tosMetrics).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public Collection<String> getDiscoveredRouters() {
        return this._store.keySet();
    }
}
