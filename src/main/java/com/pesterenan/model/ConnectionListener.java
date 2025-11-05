package com.pesterenan.model;

import krpc.client.Connection;
import krpc.client.services.SpaceCenter;

public interface ConnectionListener {
    void onConnectionChanged(Connection newConnection, SpaceCenter newSpaceCenter);
}
