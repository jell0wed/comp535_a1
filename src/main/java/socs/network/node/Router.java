package socs.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.message.HELLOMessage;
import socs.network.message.SOSPFPacket;
import socs.network.util.Configuration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Router {
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    private ExecutorService connectedPortsThreadPool = Executors.newFixedThreadPool(10);
    private Thread incomingConnectionThread;
    private ServerSocket routerSock;
    private ObjectOutputStream objOut;
    private ObjectInputStream objIn;

    protected LinkStateDatabase lsd;
    private int nextAvailPort = 0;
    private boolean listen = true;
    RouterDescription routerDesc = new RouterDescription();
    Link[] ports = new Link[4]; //assuming that all routers are with 4 ports


    public Router(Configuration config) {
        routerDesc.processPortNumber = 50000;
        routerDesc.processIPAddress = "0.0.0.0";
        routerDesc.simulatedIPAddress = UUID.randomUUID().toString();

        lsd = new LinkStateDatabase(routerDesc);

        this.initializeSocket();
        this.listenForIncomingConnection();

        LOG.info("Started router " + routerDesc.simulatedIPAddress);
    }

    private void initializeSocket() {
        try {
            this.routerSock = new ServerSocket(routerDesc.processPortNumber);
            LOG.debug("Initialized server socket for {}", this.routerDesc);
        } catch (IOException e) {
            throw new RuntimeException("Unable to initializeSocket()", e);
        }
    }

    private void listenForIncomingConnection() {
        this.incomingConnectionThread = new Thread(() -> {
            while(listen) {
                try {
                    Socket clientSock = (Router.this).routerSock.accept();

                    Link newLink = Link.incomingConnection(clientSock, (Router.this));
                    (Router.this).ports[(Router.this).nextAvailPort] = newLink;

                    (Router.this).connectedPortsThreadPool.submit(newLink::listenForIncomingCommands);

                    (Router.this).nextAvailPort++;
                } catch (IOException e) {
                    LOG.error("Exception while accepting client socket", e);
                }
            }
        });
    }

    /**
     * output the shortest path to the given destination ip
     * <p/>
     * format: source ip address  -> ip address -> ... -> destination ip
     *
     * @param destinationIP the ip adderss of the destination simulated router
     */
    private void processDetect(String destinationIP) {

    }

    /**
     * disconnect with the router identified by the given destination ip address
     * Notice: this command should trigger the synchronization of database
     *
     * @param portNumber the port number which the link attaches at
     */
    private void processDisconnect(short portNumber) {

    }

    /**
     * attach the link to the remote router, which is identified by the given simulated ip;
     * to establish the connection via socket, you need to indentify the process IP and process Port;
     * additionally, weight is the cost to transmitting data through the link
     * <p/>
     * NOTE: this command should not trigger link database synchronization
     */
    private void processAttach(String processIP, int processPort,
                               String simulatedIP, short weight) {
        RouterDescription targetRouter = new RouterDescription();
        targetRouter.processIPAddress = processIP;
        targetRouter.processPortNumber = processPort;
        targetRouter.simulatedIPAddress = simulatedIP;

        // create a new link and connect to target router
        Link newLink = Link.establishConnection(this, targetRouter);
        this.ports[this.nextAvailPort] = newLink;
        this.connectedPortsThreadPool.submit(newLink::listenForIncomingCommands);

        this.nextAvailPort++;
    }

    /**
     * broadcast Hello to neighbors
     */
    private void processStart() {
        // broadcast HELLO to every neighbors
        for(int i = 0; i < this.nextAvailPort; i++) {
            HELLOMessage helloPak = new HELLOMessage();

            this.ports[i].send(helloPak);
        }
    }

    /**
     * attach the link to the remote router, which is identified by the given simulated ip;
     * to establish the connection via socket, you need to indentify the process IP and process Port;
     * additionally, weight is the cost to transmitting data through the link
     * <p/>
     * This command does trigger the link database synchronization
     */
    private void processConnect(String processIP, short processPort,
                                String simulatedIP, short weight) {

    }

    /**
     * output the neighbors of the routers
     */
    private void processNeighbors() {

    }

    /**
     * disconnect with all neighbors and quit the program
     */
    private void processQuit() {

    }

    public void terminal() {
        try {
            InputStreamReader isReader = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isReader);
            System.out.print(">> ");
            String command = br.readLine();
            while (true) {
                if (command.startsWith("detect ")) {
                    String[] cmdLine = command.split(" ");
                    processDetect(cmdLine[1]);
                } else if (command.startsWith("disconnect ")) {
                    String[] cmdLine = command.split(" ");
                    processDisconnect(Short.parseShort(cmdLine[1]));
                } else if (command.startsWith("quit")) {
                    processQuit();
                } else if (command.startsWith("attach ")) {
                    String[] cmdLine = command.split(" ");
                    processAttach(cmdLine[1], Integer.parseInt(cmdLine[2]),
                            cmdLine[3], Short.parseShort(cmdLine[4]));
                } else if (command.equals("start")) {
                    processStart();
                } else if (command.equals("connect ")) {
                    String[] cmdLine = command.split(" ");
                    processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                            cmdLine[3], Short.parseShort(cmdLine[4]));
                } else if (command.equals("neighbors")) {
                    //output neighbors
                    processNeighbors();
                } else {
                    //invalid command
                    break;
                }
                System.out.print(">> ");
                command = br.readLine();
            }
            isReader.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
