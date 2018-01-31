package socs.network.message;

import socs.network.node.Link;
import socs.network.node.RouterDescription;

import java.io.Serializable;

public abstract class BaseMessage implements Serializable {
    public RouterDescription from;
    public RouterDescription to;

    public abstract void executeMessage(Link currentLink);
}
