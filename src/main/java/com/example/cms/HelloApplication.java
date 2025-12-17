package com.example.cms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.cms.database.Database;

public class HelloApplication extends Application {

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Database.createTables();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cms/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Course Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}