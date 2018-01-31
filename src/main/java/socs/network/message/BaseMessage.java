package socs.network.message;

import socs.network.node.Link;

import java.io.Serializable;

public abstract class BaseMessage implements Serializable {
    public abstract void executeMessage(Link currentLink);
}
