package com.bjardon.hyperterm.services;

import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class OpenPortService extends Service<Boolean> {

    private ObjectProperty<SerialPort> port = new SimpleObjectProperty<>();

    public void setPort(SerialPort port) {
        this.port.set(port);
    }

    public SerialPort getPort() {
        return port.get();
    }

    public ObjectProperty<SerialPort> portProperty() {
        return port;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                port.get().setBaudRate(9600);
                port.get().setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0,0);
                return port.get().openPort();
            }
        };
    }
}
