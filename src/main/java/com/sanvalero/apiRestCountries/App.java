package com.sanvalero.apiRestCountries;

import com.sanvalero.apiRestCountries.util.R;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Creado por @author: Javier
 * el 11/04/2021
 */
public class App extends Application {

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(new AppController());
        loader.setLocation(R.getUI("api_rest_countries.fxml"));
        VBox vBox = loader.load();

        Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("API RestCountries");
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
