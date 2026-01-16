package at.ac.hcw.chatty.controller;

import at.ac.hcw.chatty.ChattyApp;
import at.ac.hcw.chatty.service.ChatConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class WaitingScreenController {
    @FXML
    private void handleCancel() {
        ChatConnection.getInstance().disconnect();
        ChattyApp.showConnectionScreen();
    }
}
