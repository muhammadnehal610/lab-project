package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.example.util.SceneManager;
import org.example.util.SessionManager;
import org.example.util.UserSession;


public class StudentDashboardController {

    @FXML
    void handleLogut(ActionEvent event){
        SessionManager.clearSession();

        UserSession.getInstance().logout();

        SceneManager.redirectToLogin(event);
    }
}