package borni.top.ntfy_gui.Controller;

import borni.top.ntfy_gui.Model.ConfigRepository;
import borni.top.ntfy_gui.Model.NtfyClient;
import borni.top.ntfy_gui.Model.NtfyResponse;
import borni.top.ntfy_gui.Model.ServerConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MainController {

    @FXML private ComboBox<ServerConfig> serverComboBox;
    @FXML private Button editServerButton;
    @FXML private ComboBox<String> tagComboBox;
    @FXML private TextField titleField;
    @FXML private TextArea messageArea;
    @FXML private Button sendButton;

    private ConfigRepository configRepository;
    private NtfyClient ntfyClient;
    private ObservableList<ServerConfig> serverList;

    @FXML
    public void initialize() {
        configRepository = new ConfigRepository();
        ntfyClient = new NtfyClient();

        serverList = FXCollections.observableArrayList(configRepository.loadServers());
        serverComboBox.setItems(serverList);
        if (!serverList.isEmpty()) {
            serverComboBox.getSelectionModel().selectFirst();
        }

        tagComboBox.setItems(FXCollections.observableArrayList(
                "empty", "warning", "key", "email", "skull", "computer"
        ));
        tagComboBox.getSelectionModel().selectFirst();

        titleField.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        messageArea.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        serverComboBox.valueProperty().addListener((observable, oldValue, newValue) -> validateForm());

        validateForm();
    }

    private void validateForm() {
        String title = titleField.getText() == null ? "" : titleField.getText();
        String message = messageArea.getText() == null ? "" : messageArea.getText();
        boolean hasServer = serverComboBox.getValue() != null;

        boolean titleTooLong = title.length() > 64;
        boolean messageTooLong = message.length() > 255;
        boolean titleEmpty = title.isBlank();
        boolean messageEmpty = message.isBlank();

        if (titleTooLong) {
            titleField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        } else {
            titleField.setStyle("");
        }
        if (messageTooLong) {
            messageArea.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        } else {
            messageArea.setStyle("");
        }

        sendButton.setDisable(!hasServer || titleEmpty || messageEmpty || titleTooLong || messageTooLong);
    }

    @FXML
    private void onSendClicked(ActionEvent event) {
        ServerConfig selectedServer = serverComboBox.getValue();
        String title = titleField.getText();
        String message = messageArea.getText();
        String tag = tagComboBox.getValue();

        NtfyResponse response = ntfyClient.sendMessage(selectedServer, title, message, tag);

        showAlertAndReset(response);
    }

    private void showAlertAndReset(NtfyResponse response) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Üzenet állapota");

        switch (response.status()) {
            case SUCCESS -> {
                alert.setContentText("Üzenet elküldve");
            }
            case UNAUTHORIZED -> {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setContentText("Azonosítási hiba");
            }
            case INVALID_REQUEST ->  {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setContentText("Csatorna nem található");
            }
            case SERVER_NOT_FOUND ->  {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setContentText("Szerver nem található");
            }
            default ->  {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setContentText("Ismeretlen hiba történt");
            }
        }

        alert.showAndWait();

        tagComboBox.getSelectionModel().selectFirst();
        titleField.clear();
        messageArea.clear();
    }

    @FXML
    private void onAddServerClicked(ActionEvent event) {
        openServerDialog(null);
    }

    @FXML
    private void onEditServerClicked(ActionEvent event) {
        ServerConfig selected = serverComboBox.getValue();
        if (selected != null) {
            openServerDialog(selected);
        }
    }

    private void openServerDialog(ServerConfig serverToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/borni/top/ntfy_gui/ServerDialog.fxml"));

            javafx.scene.Parent root = loader.load();
            ServerDialogController dialogController = loader.getController();
            dialogController.setInitData(configRepository, serverList, serverToEdit);

            Stage stage = new Stage();
            stage.setTitle(serverToEdit == null ? "Új szerver hozzáadása" : "Szerver módosítása");
            stage.setScene(new javafx.scene.Scene(root));

            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            validateForm();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Nem sikerült megnyitni a beállítások ablakot.");
        }
    }
}
