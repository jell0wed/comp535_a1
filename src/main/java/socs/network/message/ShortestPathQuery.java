package socs.network.message;

import socs.network.node.Link;
import socs.network.node.LinkStateDatabase;
import socs.network.node.RouterDescription;

import java.util.Collection;

public class ShortestPathQuery extends BaseMessage {
    private String destIp;

    public ShortestPathQuery(String destIp, RouterDescription from, RouterDescription to) {
        super(from, to);
        this.destIp = destIp;
    }

    @Override
    public void executeMessage(Link currentLink) {
        LinkStateDatabase linkDatabase = currentLink.getLocalRouter().getLinkStateDatabase();

        Path nextPath = null;
        // base case: target router is connected directly
        if(linkDatabase.hasRouterBeenDiscovered(this.destIp)) {
            LSA lsa = linkDatabase.getDiscoveredRouter(this.destIp);
            nextPath = new Path(currentLink.getLocalRouter().getRouterDesc(), null, lsa.lsaSeqNumber);

        } else {
            // check if there's a shortest path among connected routers
            Collection<Link> connectedRouters = currentLink.getLocalRouter().getConnectedPorts();
            for(Link connRouterLink: connectedRouters) {
                ShortestPathResponse resp = (ShortestPathResponse) connRouterLink.sendAndWait()
            }

            // if not, carry on the empty path
        }
    }
}
