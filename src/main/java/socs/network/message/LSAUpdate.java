package socs.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.node.Link;
import socs.network.node.LinkStateDatabase;
import socs.network.node.RouterDescription;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class LSAUpdate extends BaseMessage {
    private static final Logger LOG = LoggerFactory.getLogger(LSAUpdate.class);


    public String uid = UUID.randomUUID().toString();
    public String simIpOrigin;
    public LSA update;

    LSAUpdate(LSAUpdate lsa) {
        super(lsa.from, lsa.to);
        this.uid = lsa.uid;
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

        if(!LinkStateDatabase.receivedLSAUpdate.contains(this.uid)) {
            localDb.updateDiscoveredRouter(this.update.linkStateID, this.update);
            currentLink.getLocalRouter().broadcastLSAUpdate(new LSAUpdate(this));

            LinkStateDatabase.receivedLSAUpdate.add(this.uid);
        }
    }
}
