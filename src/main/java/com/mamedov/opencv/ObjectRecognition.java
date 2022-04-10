package com.mamedov.opencv;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class ObjectRecognition extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ObjectRecognition.fxml"));
            BorderPane root = loader.load();
            root.setStyle("-fx-background-color: #9a9a9a;");
            Scene scene = new Scene(root, 1300, 600);
            stage.setTitle("Object Recognition");
            stage.setScene(scene);
            stage.show();

            ObjectRecognitionController controller = loader.getController();
            stage.setOnCloseRequest((we -> controller.setClosed()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}