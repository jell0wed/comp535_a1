package socs.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.node.Link;
import socs.network.node.RouterDescription;
import socs.network.node.RouterStatus;

import java.io.*;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class SOSPFPacket extends BaseMessage {
    private static final Logger LOG = LoggerFactory.getLogger(SOSPFPacket.class);
    protected SOSPFPacket(RouterDescription from, RouterDescription to) {
        super(from, to);
    }

    public enum SOSPFPacketType {
        HELLO,
        LINKSTATE_UPDATE
    }

    //for inter-process communication
    public String srcProcessIP;
    public int srcProcessPort;

    //simulated IP address
    public String srcIP;
    public String dstIP;

    //common header
    public SOSPFPacketType sospfType; //0 - HELLO, 1 - LinkState Update
    public String routerID;

    //used by HELLO message to identify the sender of the message
    //e.g. when router A sends HELLO to its neighbor, it has to fill this field with its own
    //simulated IP address
    public String neighborID; //neighbor's simulated IP address


    @Override
    public void executeMessage(Link currentLink) {
        if(this.sospfType != SOSPFPacketType.HELLO) {
            return;
        }

        LOG.info("received HELLO from {}", this.srcIP);

        LSA currentlyDiscoveredLSA = currentLink.getLocalRouter().getLinkStateDatabase().getDiscoveredRouter(currentLink.getLocalRouter().getRouterDesc().getSimulatedIPAddress());
        Optional<LinkDescription> foundDescOpt = currentlyDiscoveredLSA
                .links
                .stream()
                .filter(x -> x.linkID.equalsIgnoreCase(this.srcIP))
                .findAny();

        if(!foundDescOpt.isPresent()) {
            LinkDescription fromLSAToHere = new LinkDescription();
            fromLSAToHere.linkID = this.srcIP;
            fromLSAToHere.portNum = currentLink.getLocalRouter().getPortNumber(currentLink);
            //fromLSAToHere.tosMetrics = 99;
            fromLSAToHere.updateStatus(RouterStatus.INIT);

            currentlyDiscoveredLSA.links.add(fromLSAToHere);
            currentLink.getLocalRouter().getLinkStateDatabase().updateDiscoveredRouter(currentLink.getLocalRouter().getRouterDesc().getSimulatedIPAddress(), currentlyDiscoveredLSA);
            currentLink.send(SOSPFPacket.createHelloPak(this.to, this.from));
        } else {
            LinkDescription newDesc = foundDescOpt.get();
            if(foundDescOpt.get().status == RouterStatus.INIT) { // update to TWO-WAY
                newDesc.updateStatus(RouterStatus.TWO_WAY);
                newDesc.tosMetrics = foundDescOpt.get().tosMetrics;

                currentlyDiscoveredLSA.links.remove(foundDescOpt.get());
                currentlyDiscoveredLSA.links.add(newDesc);

                currentLink.getLocalRouter().getLinkStateDatabase().updateDiscoveredRouter(currentLink.getLocalRouter().getRouterDesc().getSimulatedIPAddress(), currentlyDiscoveredLSA);
                currentLink.send(SOSPFPacket.createHelloPak(this.to, this.from));

                // broadcast LSA update
                currentLink.getLocalRouter().broadcastLSAUpdate();
            }
        }

    }

    public static SOSPFPacket createHelloPak(RouterDescription from, RouterDescription to) {
        SOSPFPacket pak = new SOSPFPacket(from, to);

        pak.srcProcessIP = from.getProcessIPAddress();
        pak.srcProcessPort = from.getProcessPortNumber();
        pak.srcIP = from.getSimulatedIPAddress();
        pak.dstIP = to.getSimulatedIPAddress();
        pak.sospfType = SOSPFPacketType.HELLO;
        pak.routerID = to.getSimulatedIPAddress();
        pak.neighborID = from.getSimulatedIPAddress();

        return pak;
    }
}
