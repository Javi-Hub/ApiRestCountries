package com.sanvalero.apiRestCountries.util;

import com.sun.tools.attach.AgentInitializationException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Creado por @author: Javier
 * el 11/04/2021
 */
public class AlertUtils {

    public static void showWarning (String message){
        Alert alertWarning = new Alert(Alert.AlertType.WARNING);
        alertWarning.setTitle("WARNING");
        alertWarning.setHeaderText("ATENTION");
        alertWarning.setContentText(message);
        alertWarning.show();
    }

    public static Optional<ButtonType> showConfirm (String message){
        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        alertConfirm.setTitle("Application Filter");
        alertConfirm.setHeaderText("CONFIRM");
        alertConfirm.setContentText(message);
        return alertConfirm.showAndWait();

    }

}
