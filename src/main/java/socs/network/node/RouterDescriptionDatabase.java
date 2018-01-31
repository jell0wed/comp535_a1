package socs.network.node;

import java.util.HashMap;

public class RouterDescriptionDatabase {
    private HashMap<String, RouterDescription> discoveredRouters = new HashMap<>();

    public boolean hasRouterBeenDiscovered(RouterDescription desc) {
        return this.discoveredRouters.containsKey(desc.simulatedIPAddress);
    }

    public void insertNewlyDiscoveredRouter(RouterDescription desc) {
        RouterDescription newDesc = desc;
        newDesc.updateStatus(RouterStatus.INIT);

        this.discoveredRouters.put(newDesc.simulatedIPAddress, newDesc);
    }

    public void insertDiscoveredRouter(RouterDescription desc) {
        RouterDescription newDesc = desc;

        this.discoveredRouters.put(newDesc.simulatedIPAddress, newDesc);
    }

    public RouterDescription getDiscoveredRouter(RouterDescription desc) {
        return this.discoveredRouters.get(desc.simulatedIPAddress);
    }

    public void updateDiscoveredRouter(RouterDescription desc) {
        this.discoveredRouters.put(desc.simulatedIPAddress, desc);
    }
}
