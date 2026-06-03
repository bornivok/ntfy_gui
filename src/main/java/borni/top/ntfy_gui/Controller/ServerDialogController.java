package borni.top.ntfy_gui.Controller;

import borni.top.ntfy_gui.Model.ConfigRepository;
import borni.top.ntfy_gui.Model.ServerConfig;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServerDialogController {

    @FXML private TextField nameField;
    @FXML private TextField urlField;
    @FXML private CheckBox authCheckBox;
    @FXML private TextField tokenField;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    private ConfigRepository configRepository;
    private ObservableList<ServerConfig> serverList;
    private ServerConfig currentServer;

    @FXML
    private void initialize() {
        tokenField.disableProperty().bind(authCheckBox.selectedProperty().not());

        nameField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        urlField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        validateForm();
    }

    private void validateForm() {
        boolean isValid = !nameField.getText().isBlank() && !urlField.getText().isBlank();
        saveButton.setDisable(!isValid);
    }

    public void setInitData(ConfigRepository configRepo, ObservableList<ServerConfig> serverList, ServerConfig server) {
        this.configRepository = configRepo;
        this.serverList = serverList;
        this.currentServer = server;

        if (server != null) {
            nameField.setText(server.getName());
            urlField.setText(server.getUrl());
            authCheckBox.setSelected(server.isAuthRequired());
            if (server.getToken() != null) {
                tokenField.setText(server.getToken());
            }
        } else {
            deleteButton.setDisable(false);
        }
    }

    @FXML
    private void onSaveClicked(ActionEvent event) {
        String name = nameField.getText();
        String url = urlField.getText();
        boolean authReq = authCheckBox.isSelected();
        String token = authReq ? tokenField.getText() : null;

        if (currentServer == null) {
            ServerConfig newServer = new ServerConfig(name, url, authReq, token);
            serverList.add(newServer);
        } else {
            currentServer.setName(name);
            currentServer.setUrl(url);
            currentServer.setAuthRequired(authReq);
            currentServer.setToken(token);

            serverList.set(serverList.indexOf(currentServer), currentServer);
        }
        configRepository.saveServers(serverList);
        closeWindow();
    }

    @FXML
    private void onDeleteClicked(ActionEvent event) {
        if (currentServer != null) {
            serverList.remove(currentServer);
            configRepository.saveServers(serverList);
            closeWindow();
        }
    }

    @FXML
    private void onCancelClicked(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
