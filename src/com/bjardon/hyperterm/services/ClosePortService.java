package com.bjardon.hyperterm.services;

import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ClosePortService extends Service<Boolean> {

    private ObjectProperty<SerialPort> port = new SimpleObjectProperty<>();

    public SerialPort getPort() {
        return port.get();
    }

    public void setPort(SerialPort port) {
        this.port.set(port);
    }

    public ObjectProperty<SerialPort> portProperty() {
        return port;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                if (port.get() == null)
                    return false;
                return port.get().closePort();
            }
        };
    }
}
