package socs.network.message;

import socs.network.node.Link;
import socs.network.node.RouterDescription;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseMessage implements Serializable {
    public RouterDescription from;
    public RouterDescription to;
    public String seq;

    protected BaseMessage(RouterDescription from, RouterDescription to) {
        this.from = from;
        this.to = to;
        this.seq = UUID.randomUUID().toString();
    }


    public abstract void executeMessage(Link currentLink);
}
