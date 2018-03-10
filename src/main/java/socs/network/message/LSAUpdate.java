package socs.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.node.Link;
import socs.network.node.LinkStateDatabase;
import socs.network.node.RouterDescription;

public class LSAUpdate extends BaseMessage {
    private static final Logger LOG = LoggerFactory.getLogger(LSAUpdate.class);
    public final String simIpOrigin;
    public final LSA update;

    LSAUpdate(LSAUpdate lsa) {
        super(lsa.from, lsa.to);
        this.simIpOrigin = lsa.simIpOrigin;
        this.update = lsa.update;
    }

    public LSAUpdate(LSA lsa, RouterDescription from, RouterDescription to) {
        super(from, to);
        this.simIpOrigin = from.getSimulatedIPAddress();
        this.update = lsa;
    }

    @Override
    public void executeMessage(Link currentLink) {
        LinkStateDatabase localDb = currentLink.getLocalRouter().getLinkStateDatabase();

        if (!localDb.hasRouterBeenDiscovered(this.simIpOrigin)) {
            localDb.updateDiscoveredRouter(this.simIpOrigin, this.update); // update the local database
            LOG.info(" > Update local database with {}", this.simIpOrigin);

            // forward to neighbours
            currentLink.getLocalRouter().broadcastLSAUpdate(new LSAUpdate(this));
        } else {
            LOG.info(" > Already know about {}", this.simIpOrigin);
        }
    }
}
