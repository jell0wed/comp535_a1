package socs.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.node.Link;
import socs.network.node.RouterDescription;
import socs.network.node.RouterStatus;

public class HELLOMessage extends BaseMessage {
    private static final Logger LOG = LoggerFactory.getLogger(HELLOMessage.class);

    @Override
    public void executeMessage(Link currentLink) {
        // we are receiving this HELLO message
        // update the FROM status
        LOG.info("receiving HELLO from {}", this.from);

        if(!currentLink.getLocalRouter().getDiscoveredRouters().hasRouterBeenDiscovered(this.from)) {
            currentLink.getLocalRouter().getDiscoveredRouters().insertNewlyDiscoveredRouter(this.from);
            currentLink.send(new HELLOMessage());
        } else {
            RouterDescription discoveredRouter = currentLink.getLocalRouter().getDiscoveredRouters().getDiscoveredRouter(this.from);
            if(discoveredRouter.getStatus() == RouterStatus.INIT) {
                discoveredRouter.updateStatus(RouterStatus.TWO_WAY);
                currentLink.getLocalRouter().getDiscoveredRouters().updateDiscoveredRouter(discoveredRouter);
                currentLink.send(new HELLOMessage());
            }
        }
    }
}
