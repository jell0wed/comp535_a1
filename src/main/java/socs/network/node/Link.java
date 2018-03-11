package socs.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.message.BaseMessage;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Link {
    private static final Logger LOG = LoggerFactory.getLogger(Link.class);

    private boolean listen = true;
    private Socket clientSock;
    private ObjectOutputStream objOut;
    private ObjectInputStream objIn;
    private Router localRouter;
    private RouterDescription remoteRouterDesc;
    private Map<String, BaseMessage> awaitingMessages = new HashMap<>();
    private Map<String, BaseMessage> awaitingResponses = new HashMap<>();

    private Link(Router local, RouterDescription toRouter) {
        this.localRouter = local;

        try {
            this.clientSock = new Socket(toRouter.processIPAddress, toRouter.processPortNumber);
            this.initializeSocket();
        } catch (IOException e) {
            throw new RuntimeException("Unable to establish connection", e);
        }
    }

    private Link(Router local, Socket clientSock) {
        this.localRouter = local;

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
    }

    /* Establish a new link from the local router to an unknown remote router (incoming connection) */
    public static Link incomingConnection(Socket clientSock, Router to) {
        Link newLink = new Link(to, clientSock);

        return newLink;
    }

    /* Establish a new link from the local router to a known remote router */
    public static Link establishConnection(Router from, RouterDescription to) {
        Link newLink = new Link(from, to);
        newLink.remoteRouterDesc = to;

        return newLink;
    }

    public void send(BaseMessage msg) {
        try {
            msg.from = this.localRouter.routerDesc;
            msg.to = this.remoteRouterDesc;

            this.objOut.reset();
            this.objOut.writeObject(msg);
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    public BaseMessage sendAndWait(BaseMessage msg) {
        try {
            msg.from = this.localRouter.routerDesc;
            msg.to = this.remoteRouterDesc;

            this.objOut.writeObject(msg);
            this.awaitingMessages.put(msg.seq, msg);

            msg.seq.wait();

            BaseMessage response = this.awaitingResponses.get(msg.seq);
            this.awaitingMessages.remove(msg.seq);
            this.awaitingResponses.remove(msg.seq);

            return response;
        } catch (IOException | InterruptedException e) {
            LOG.error("", e);
            throw new RuntimeException(e);
        }
    }

    public void listenForIncomingCommands() {
        while(listen) {
            try {
                BaseMessage recvMessage = (BaseMessage) this.objIn.readObject();

                // if we still do not know the remote router yet...
                if(this.remoteRouterDesc == null) {
                    this.remoteRouterDesc = recvMessage.from;
                }

                if(this.awaitingMessages.containsKey(recvMessage.seq)) {
                    this.awaitingResponses.put(recvMessage.seq, recvMessage);
                    this.awaitingMessages.get(recvMessage.seq).notifyAll();
                }

                recvMessage.executeMessage(this);
            } catch (EOFException e) {
                this.listen = false;
                LOG.error("Remote socket has disconnected ...");
            } catch (IOException | ClassNotFoundException e) {
                LOG.error("", e);
            }
        }
    }

    public Router getLocalRouter() {
        return this.localRouter;
    }

    public RouterDescription getRemoteRouterDesc() {
        return remoteRouterDesc;
    }
}
