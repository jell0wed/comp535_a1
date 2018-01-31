package socs.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterDescription {
    private static final Logger LOG = LoggerFactory.getLogger(RouterDescription.class);

    //used to socket communication
    String processIPAddress;
    int processPortNumber;
    //used to identify the router in the simulated network space
    String simulatedIPAddress;
    //status of the router
    RouterStatus status = RouterStatus.UNKNOWN;

    public RouterStatus getStatus() {
        return status;
    }

    public void updateStatus(RouterStatus status) {
        this.status = status;
        LOG.info("Updated {} state to {}", this.simulatedIPAddress, status);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Router<")
                .append(this.processIPAddress)
                .append(":")
                .append(this.processPortNumber)
                .append(">")
                .append("(")
                .append(this.simulatedIPAddress)
                .append(")")
                .toString();
    }
}
