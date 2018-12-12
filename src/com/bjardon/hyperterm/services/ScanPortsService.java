package com.bjardon.hyperterm.services;


import com.fazecast.jSerialComm.SerialPort;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ScanPortsService extends Service<ObservableList<SerialPort>> {

    @Override
    protected Task<ObservableList<SerialPort>> createTask() {
        return new Task<ObservableList<SerialPort>>() {
            @Override
            protected ObservableList<SerialPort> call() throws Exception {
                Thread.sleep(500);
                return FXCollections.observableArrayList(SerialPort.getCommPorts());
            }
        };
    }

}
