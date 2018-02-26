package socs.network.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.SOSPFPacket;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class Router {
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    private ExecutorService connectedPortsThreadPool = Executors.newFixedThreadPool(10);
    private Thread incomingConnectionThread;
    private ServerSocket routerSock;

    protected LinkStateDatabase lsd;
    private int nextAvailPort = 0;
    private boolean listen = true;

    RouterDescription routerDesc = new RouterDescription();
    Link[] ports = new Link[4]; //assuming that all routers are with 4 ports

    public Router(int port, String simulatedIP) {
        routerDesc.processPortNumber = port;
        routerDesc.processIPAddress = "0.0.0.0";
        routerDesc.simulatedIPAddress = simulatedIP;

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

        this.incomingConnectionThread.start();
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
     * to establish the connection via socket, you need to identify the process IP and process Port;
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
        targetRouter.status = RouterStatus.INIT;

        // create a new link and connect to target router
        Link newLink = Link.establishConnection(this, targetRouter);
        this.ports[this.nextAvailPort] = newLink;
        this.connectedPortsThreadPool.submit(newLink::listenForIncomingCommands);


        // make sure an initial link is created
        LSA currentRouterLSA = this.lsd.getDiscoveredRouter(this.routerDesc.simulatedIPAddress);
        Optional<LinkDescription> targetLinkOpt = currentRouterLSA
                .links
                .stream()
                .filter(x -> x.linkID.equalsIgnoreCase(targetRouter.simulatedIPAddress))
                .findAny();
        if(!targetLinkOpt.isPresent()) {
            LinkDescription newLinkDesc = new LinkDescription();
            newLinkDesc.linkID = targetRouter.simulatedIPAddress;
            newLinkDesc.portNum = this.nextAvailPort;
            newLinkDesc.tosMetrics = weight;
            newLinkDesc.status = RouterStatus.INIT;

            currentRouterLSA.links.add(newLinkDesc);
        }

        this.nextAvailPort++;
    }

    /**
     * broadcast Hello to neighbors
     */
    private void processStart() {
        // broadcast HELLO to every neighbors
        for(int i = 0; i < this.nextAvailPort; i++) {

            SOSPFPacket helloPak = SOSPFPacket.createHelloPak(this.routerDesc, this.ports[i].getRemoteRouterDesc());

            this.ports[i].send(helloPak);
        }
    }

    // broadcast LINKSTATE_UPDATE to all neighbours
    public void synchronizeLSD() {
        //TODO: sequence numbers for LSA's not taken into account here
        Vector<LSA> lsaVector = new Vector<>();
        for (Map.Entry<String, LSA> entry : lsd._store.entrySet()) {
            lsaVector.add(entry.getValue());
        }
        for(int i = 0; i < this.nextAvailPort; i++) {
            SOSPFPacket lsp = SOSPFPacket.createLSP(this.routerDesc, this.ports[i].getRemoteRouterDesc(), lsaVector);
            this.ports[i].send(lsp);
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
        for(int i = 0; i < this.nextAvailPort; i++) {
            System.out.println(this.ports[i].getRemoteRouterDesc().simulatedIPAddress);
        }
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

    // to be used on final link state databases
    // constructs weighted graph using BFS
    // WeightedGraph class does not allow duplicate values along a path
    public WeightedGraph contructWeightedGraph() {

        Queue<WeightedGraph> queue = new LinkedList<>();
        WeightedGraph root = new WeightedGraph(routerDesc.getSimulatedIPAddress(), 0, null);

        // add children to queue
        LSA currentNodeLSA = lsd._store.get(routerDesc.getSimulatedIPAddress());
        for (LinkDescription link : currentNodeLSA.links) {
            WeightedGraph childNode = new WeightedGraph(link.linkID, link.tosMetrics, root);
            queue.add(childNode);
            root.addChild(childNode);
        }

        while(!queue.isEmpty()) {
            WeightedGraph currentNode = queue.poll();

            // add current node's children to queue
            currentNodeLSA = lsd._store.get(currentNode.getValue());
            for (LinkDescription link : currentNodeLSA.links) {
                WeightedGraph childNode = new WeightedGraph(link.linkID, link.tosMetrics, currentNode);
                queue.add(childNode);
                currentNode.addChild(childNode);
            }
        }

        return root;
    }
    public LinkStateDatabase getLinkStateDatabase() {
        return lsd;
    }

    public int getPortNumber(Link l) {
        return Arrays.asList(this.ports).indexOf(l);
    }

    public RouterDescription getRouterDesc() {
        return routerDesc;
    }

    public List<Link> getConnectedPorts() {
        return Arrays.stream(this.ports).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
