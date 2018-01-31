package socs.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.node.Link;
import socs.network.node.RouterStatus;

public class HELLOMessage extends BaseMessage {
    private static final Logger LOG = LoggerFactory.getLogger(HELLOMessage.class);

    @Override
    public void executeMessage(Link currentLink) {
        // we are receiving this HELLO message
        // update the FROM status
        LOG.info("receiving HELLO from {}", currentLink.getFromRouter());

        if(currentLink.getFromRouter().getStatus() == RouterStatus.UNKNOWN) {
            currentLink.getFromRouter().updateStatus(RouterStatus.INIT);

            // send ack
            currentLink.send(new HELLOMessage());
        } else if(currentLink.getFromRouter().getStatus() == RouterStatus.INIT) {
            currentLink.getFromRouter().updateStatus(RouterStatus.TWO_WAY);

            // send ack
            currentLink.send(new HELLOMessage());
        }
    }
}
