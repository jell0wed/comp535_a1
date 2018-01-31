package socs.network.node;

public class RouterDescription {
    //used to socket communication
    String processIPAddress;
    int processPortNumber;
    //used to identify the router in the simulated network space
    String simulatedIPAddress;
    //status of the router
    RouterStatus status;


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
