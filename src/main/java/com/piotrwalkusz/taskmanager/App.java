package com.piotrwalkusz.taskmanager;

import com.piotrwalkusz.taskmanager.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Task Manager");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(200);

        // Handle application close - save active session
        primaryStage.setOnCloseRequest(event -> controller.onApplicationClose());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
