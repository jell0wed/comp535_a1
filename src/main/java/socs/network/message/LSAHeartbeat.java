package socs.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.node.Link;
import socs.network.node.RouterDescription;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class LSAHeartbeat extends BaseMessage {
    private static HashMap<String, Timer> awaitingResponses = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(LSAHeartbeat.class);

    private String heartbeatId = UUID.randomUUID().toString();
    private boolean isResponse = false;

    public LSAHeartbeat(RouterDescription from, RouterDescription to, TimerTask timeoutAction) {
        super(from, to);
        Timer timeoutTim = new Timer();
        timeoutTim.schedule(timeoutAction, 30 * 1000);

        awaitingResponses.put(this.heartbeatId, timeoutTim);
    }

    private LSAHeartbeat(LSAHeartbeat src) {
        super(src.from, src.to);
        this.isResponse = src.isResponse;
        this.heartbeatId = src.heartbeatId;
    }

    @Override
    public void executeMessage(Link currentLink) {
        if(!this.isResponse) {
            //LOG.info("Received heartbeat from " + currentLink.getRemoteRouterDesc().getSimulatedIPAddress());
            LSAHeartbeat resp = new LSAHeartbeat(this);
            resp.isResponse = true;

            currentLink.send(resp);
        } else {
            //LOG.info("Received heartbeat ack from " + currentLink.getRemoteRouterDesc().getSimulatedIPAddress());
            if(!awaitingResponses.containsKey(this.heartbeatId)) {
                return; // ignore, invalid heartbeat id
            }

            Timer timeoutTim = awaitingResponses.get(this.heartbeatId);
            timeoutTim.cancel();

            awaitingResponses.remove(this.heartbeatId);
        }
    }
}
