package socs.network.message;

import socs.network.node.RouterDescription;

import java.io.Serializable;

public class Path implements Serializable {
    private RouterDescription current;
    private int distance = 0;
    private Path next = null;

    public Path(RouterDescription router, Path p, int n) {
        this.current = router;
        this.next = p;
        this.distance = n;
    }
}
