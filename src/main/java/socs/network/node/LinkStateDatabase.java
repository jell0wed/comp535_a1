package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.*;

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
        ld.tosMetrics = 0;
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
        } else {
            existingLSA = updatedLSA;
            updated = true;
        }

        this._store.put(simulatedIp, existingLSA);
        return updated;
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
