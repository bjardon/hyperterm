package com.bjardon.hyperterm.services;

import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.Arrays;

public class WritePortService extends Service<Integer> {

    private StringProperty tx = new SimpleStringProperty();
    private ObjectProperty<SerialPort> port = new SimpleObjectProperty<>();

    public void setTx(String tx) {
        this.tx.set(tx);
    }

    public String getTx() {
        return tx.get();
    }

    public StringProperty txProperty() {
        return tx;
    }

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
    protected Task<Integer> createTask() {
        return new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                final String tx = getTx();
                final SerialPort port = getPort();
                return port.writeBytes(tx.getBytes(), tx.length());
            }
        };
    }
}
