package socs.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.message.BaseMessage;

import java.io.*;
import java.net.Socket;

public class Link {
    private static final Logger LOG = LoggerFactory.getLogger(Link.class);

    private boolean listen = true;
    private Socket clientSock;
    private ObjectOutputStream objOut;
    private ObjectInputStream objIn;
    private Router localRouter;

    RouterDescription fromRouter;
    RouterDescription toRouter;

    private Link(RouterDescription from, RouterDescription to) {
        this.fromRouter = from;
        this.toRouter = to;

        try {
            this.clientSock = new Socket(this.toRouter.processIPAddress, this.toRouter.processPortNumber);
            this.initializeSocket();
        } catch (IOException e) {
            throw new RuntimeException("Unable to establish connection", e);
        }
    }

    private Link(RouterDescription to, Socket clientSock) {
        this.toRouter = to;

        try {
            this.clientSock = clientSock;
            this.initializeSocket();
        } catch (IOException e) {
            throw new RuntimeException("Unable to accept incomming connection", e);
        }
    }

    private void initializeSocket() throws IOException {
        this.objOut = new ObjectOutputStream(this.clientSock.getOutputStream());
        this.objIn = new ObjectInputStream(this.clientSock.getInputStream());
        LOG.debug("Initialized client socket to {}", this.toRouter);
    }

    public static Link incomingConnection(Socket clientSock, Router to) {
        Link newLink = new Link(to.routerDesc, clientSock);
        newLink.localRouter = to;

        return newLink;
    }

    public static Link establishConnection(Router from, RouterDescription to) {
        Link newLink = new Link(from.routerDesc, to);
        newLink.localRouter = from;

        return newLink;
    }

    public void listenForIncomingCommands() {
        while(listen) {
            try {
                BaseMessage recvMessage = (BaseMessage) this.objIn.readObject();
                recvMessage.executeMessage(this);
            } catch (IOException | ClassNotFoundException e) {
                LOG.error("", e);
            }
        }
    }

    public Router getLocalRouter() {
        return this.localRouter;
    }
}
