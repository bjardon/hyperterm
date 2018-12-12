package com.bjardon.hyperterm.services;

import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ReadPortService extends ScheduledService<String> {

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
    protected Task<String> createTask() {
        return new Task<String>() {
            private final SerialPort port = getPort();

            @Override
            protected String call() {
                if (port.bytesAvailable() == 0)
                    return null;

                byte[] buffer = new byte[port.bytesAvailable()];

                if(port.readBytes(buffer, buffer.length) == 0) return null;

                return new String(buffer);
            }
        };
    }
}
