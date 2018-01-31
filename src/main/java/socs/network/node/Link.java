package socs.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class Link {
    private static final Logger LOG = LoggerFactory.getLogger(Link.class);

    private Socket clientSock;
    RouterDescription fromRouter;
    RouterDescription toRouter;

    public Link(RouterDescription from, RouterDescription to) {
        fromRouter = from;
        toRouter = to;

        this.initializeSocket();
    }

    private void initializeSocket() {
        try {
            this.clientSock = new Socket(this.toRouter.processIPAddress, this.toRouter.processPortNumber);
            LOG.debug("Initialized client socket to {}", this.toRouter);
        } catch (IOException e) {
            throw new RuntimeException("Unable to initializeSocket()", e);
        }
    }
}
