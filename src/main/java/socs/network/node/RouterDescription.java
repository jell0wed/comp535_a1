package socs.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class RouterDescription implements Serializable {
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

    public String getProcessIPAddress() {
        return processIPAddress;
    }

    public void setProcessIPAddress(String processIPAddress) {
        this.processIPAddress = processIPAddress;
    }

    public int getProcessPortNumber() {
        return processPortNumber;
    }

    public void setProcessPortNumber(int processPortNumber) {
        this.processPortNumber = processPortNumber;
    }

    public String getSimulatedIPAddress() {
        return simulatedIPAddress;
    }

    public void setSimulatedIPAddress(String simulatedIPAddress) {
        this.simulatedIPAddress = simulatedIPAddress;
    }

    public void setStatus(RouterStatus status) {
        this.status = status;
    }
}
