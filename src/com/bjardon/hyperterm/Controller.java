package com.bjardon.hyperterm;

import com.bjardon.hyperterm.resources.Strings;
import com.bjardon.hyperterm.services.*;
import com.bjardon.hyperterm.util.AlertManager;
import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private TextArea terminalTextArea;
    @FXML
    private TextField commandTextField;
    @FXML
    private ComboBox<SerialPort> portsComboBox;
    @FXML
    private Button scanButton;
    @FXML
    private Button connectButton;
    @FXML
    private Button sendButton;
    @FXML
    private ProgressIndicator workingIndicator;
    @FXML
    private Circle connectedIndicator;
    @FXML
    private Label deviceCountLabel;

    private ScanPortsService scanPortsService = new ScanPortsService();
    private OpenPortService openPortService = new OpenPortService();
    private ClosePortService closePortService = new ClosePortService();
    private ReadPortService readPortService = new ReadPortService();
    private WritePortService writePortService = new WritePortService();

    private BooleanProperty connected = new SimpleBooleanProperty();
    private ObjectProperty<Paint> connectedFill = new SimpleObjectProperty<>();

    private ObjectProperty<ObservableList<SerialPort>> ports = new SimpleObjectProperty<>();
    private ObjectProperty<SerialPort> port = new SimpleObjectProperty<>(null);

    private final int INCOMING = 38;
    private final int OUTGOING = 83;
    private final int MESSAGE = 0;

    private void log(String s, int logLevel) {
        if (s != null)
            if (!s.isEmpty()) {
                switch (logLevel) {
                    case INCOMING:
                        s = "[>] " + s;
                        break;
                    case OUTGOING:
                        s = "[<] " + s;
                        break;
                    case MESSAGE:
                        s = "[!] " + s;
                        break;
                    default:
                        break;
                }
                this.terminalTextArea.appendText(s + "\n");
            }
    }

    @FXML
    public void scan(ActionEvent event) {

        if(readPortService.isRunning())
            readPortService.cancel();

        closePortService.setPort(this.port.get());
        closePortService.restart();

        this.closePortService.setOnSucceeded(event1 -> {
            this.connected.set(false);
            this.scanPortsService.restart();

            if ((Boolean) event1.getSource().getValue()) {
                log("Desconectado de " + port.get().getDescriptivePortName(), MESSAGE);
                this.connectedFill.set(Color.YELLOW);
            }

            this.port.set(null);
        });

        this.scanPortsService.setOnSucceeded(event1 -> {
            this.ports.set((ObservableList<SerialPort>) event1.getSource().getValue());
            if (this.ports.get().isEmpty()) {
                AlertManager.show(Alert.AlertType.ERROR, Strings.Errors.ERROR_OCCURRED, Strings.Errors.DEVICE_NOT_CONNECTED, Strings.Errors.DEVICE_NOT_CONNECTED_DESCRIPTION);
                return;
            }
            this.deviceCountLabel.setText(ports.get().size() + " dispositivos encontrados");
        });
    }

    @FXML
    public void connect(ActionEvent event) {

        if (portsComboBox.getValue() == null) {
            AlertManager.show(Alert.AlertType.ERROR, Strings.Errors.ERROR_OCCURRED, Strings.Errors.DEVICE_NOT_SELECTED, Strings.Errors.DEVICE_NOT_SELECTED_DESCRIPTION);
            return;
        }

        openPortService.setPort(this.portsComboBox.getValue());
        openPortService.setOnSucceeded(event1 -> {
            if(!(Boolean) event1.getSource().getValue()) {
                AlertManager.show(Alert.AlertType.ERROR, Strings.Errors.ERROR_OCCURRED, Strings.Errors.FAILED_CONNECTION, Strings.Errors.FAILED_CONNECTION_DESCRIPTION);
                return;
            }

            this.port.set(openPortService.getPort());
            this.connectedFill.set(Color.LIME);
            log("Conectado a " + this.port.get().getDescriptivePortName(), MESSAGE);
            this.connected.set(true);

            readPortService.setPort(this.port.get());
            readPortService.restart();
        });

        openPortService.restart();
    }

    @FXML
    public void send(ActionEvent event) {
        String tx = this.commandTextField.getText();
        this.commandTextField.clear();
        //tx = tx.trim();
        log(tx, OUTGOING);
        writePortService.setPort(this.port.get());
        writePortService.setTx(tx);
        writePortService.setOnSucceeded(event1 -> {
            if ((Integer)event1.getSource().getValue() < 0)
                AlertManager.show(Alert.AlertType.ERROR, Strings.Errors.ERROR_OCCURRED, Strings.Errors.FAILED_CONNECTION, Strings.Errors.FAILED_CONNECTION_DESCRIPTION);
            log("W: " + event1.getSource().getValue(), MESSAGE);
        });
        writePortService.restart();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.connectedFill.set(Color.YELLOW);

        portsComboBox.setConverter(new StringConverter<SerialPort>() {
            @Override
            public String toString(SerialPort object) {
                return object.getDescriptivePortName();
            }

            @Override
            public SerialPort fromString(String string) {
                return null;
            }
        });

        readPortService.setOnSucceeded(event2 -> log((String) event2.getSource().getValue(), INCOMING));
        readPortService.setDelay(Duration.millis(500));

        this.portsComboBox.itemsProperty().bind(this.ports);
        this.workingIndicator.visibleProperty().bind(scanPortsService.runningProperty().or(openPortService.runningProperty()));

        this.portsComboBox.disableProperty().bind(this.connected);
        this.connectButton.disableProperty().bind(this.connected);
        this.sendButton.disableProperty().bind(this.connected.not());
        this.commandTextField.disableProperty().bind(this.connected.not());

        this.connectedIndicator.fillProperty().bind(this.connectedFill);
    }
}
