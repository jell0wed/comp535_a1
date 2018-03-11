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
        this.simIpOrigin = lsa.linkStateID;
        this.update = lsa;
    }

    @Override
    public void executeMessage(Link currentLink) {
        LinkStateDatabase localDb = currentLink.getLocalRouter().getLinkStateDatabase();

        if(localDb.updateDiscoveredRouter(this.simIpOrigin, this.update)) {
            currentLink.getLocalRouter().broadcastLSAUpdate(new LSAUpdate(this));
        }

        /*if (!localDb.hasRouterBeenDiscovered(this.simIpOrigin)) {

            LOG.info(" > Discovered new router {}", this.simIpOrigin);

            // forward to neighbours
        } else {
            LOG.info("> Updating already discovered router {}", this.simIpOrigin);
            localDb.updateDiscoveredRouter(this.simIpOrigin, this.update);
        }



        lastReceivedSeqNumber = this.lsaSeqNumber;*/
    }
}
