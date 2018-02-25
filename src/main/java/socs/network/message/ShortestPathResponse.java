package socs.network.message;

import socs.network.node.Link;
import socs.network.node.RouterDescription;

public class ShortestPathResponse extends BaseMessage {
    private Path shortestPath;

    public ShortestPathResponse(Path p, RouterDescription from, RouterDescription to) {
        super(from, to);
        this.shortestPath = p;
    }

    @Override
    public void executeMessage(Link currentLink) {

    }
}
