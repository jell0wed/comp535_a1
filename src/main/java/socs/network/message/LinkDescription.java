package socs.network.message;

import socs.network.node.Link;
import socs.network.node.RouterStatus;

import java.io.Serializable;

public class LinkDescription implements Serializable {
    public String linkID;
    public int portNum;
    public int tosMetrics;
    public RouterStatus status;

    public void updateStatus(RouterStatus newStatus) {
        this.status = newStatus;
        System.out.println("Link to " + this.linkID + " status updated to " + this.status.toString());
    }

    public String toString() {
        return linkID + "," + portNum + "," + tosMetrics;
    }

}
