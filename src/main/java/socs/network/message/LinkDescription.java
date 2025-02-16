package socs.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.node.RouterStatus;

import java.io.Serializable;

public class LinkDescription implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(LinkDescription.class);

    public String linkID;
    public int portNum;
    public int tosMetrics = Integer.MAX_VALUE;
    public RouterStatus status;

    public void updateStatus(RouterStatus newStatus) {
        this.status = newStatus;
        LOG.info("set {} to {}", this.linkID, this.status);
    }

    public String toString() {
        return linkID + "," + portNum + "," + tosMetrics;
    }

    public void combine(LinkDescription desc) {
        if(!this.linkID.equalsIgnoreCase(desc.linkID)) {
            return;
        }

        if(this.tosMetrics < desc.tosMetrics) {
            return;
        }

        this.tosMetrics = desc.tosMetrics;
    }

}
