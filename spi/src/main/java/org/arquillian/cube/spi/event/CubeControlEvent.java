package org.arquillian.cube.spi.event;

import org.jboss.arquillian.core.spi.event.Event;

public abstract class CubeControlEvent implements Event {

    private String cubeId;

    public CubeControlEvent(String cubeId) {
        this.cubeId = cubeId;
    }

    public String getCubeId() {
        return cubeId;
    }
}
