package socs.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.node.Link;
import socs.network.node.LinkStateDatabase;
import socs.network.node.RouterDescription;

import java.util.UUID;

public class LSARemove extends BaseMessage {
    private static final Logger LOG = LoggerFactory.getLogger(LSAUpdate.class);

    public String uid = UUID.randomUUID().toString();
    public String removeSimulatedIp;

    public LSARemove(RouterDescription remove, RouterDescription from, RouterDescription to) {
        super(from, to);
        this.removeSimulatedIp = remove.getSimulatedIPAddress();
    }

    public LSARemove(LSARemove src) {
        super(src.from, src.to);
        this.uid = src.uid;
        this.removeSimulatedIp = src.removeSimulatedIp;
    }

    @Override
    public void executeMessage(Link currentLink) {
        LinkStateDatabase localDb = currentLink.getLocalRouter().getLinkStateDatabase();

        if(!LinkStateDatabase.receivedLSARemove.contains(this.uid)) {
            localDb.removeDiscoveredRouter(this.removeSimulatedIp);
            currentLink.getLocalRouter().broadcastLSARemove(new LSARemove(this));

            LinkStateDatabase.receivedLSARemove.add(this.uid);
        }
    }
}
