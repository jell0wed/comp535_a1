package socs.network;

import socs.network.node.Router;
import socs.network.util.Configuration;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("usage: <physical ip address> <physical port> <simulated ip> <weight>");
            System.exit(1);
        }

        Router r = new Router(Integer.parseInt(args[0]), args[1]);
        r.terminal();
    }
}
