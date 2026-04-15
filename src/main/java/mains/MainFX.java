package mains;



import javafx.application.Application;

import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;

import javafx.scene.Scene;

import javafx.stage.Stage;



public class MainFX extends Application {

    @Override

    public void start(Stage stage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/user/fxml/login.fxml"));

        Scene scene = new Scene(root, 1920, 1080);

        scene.getStylesheets().add(getClass().getResource("/user/css/style.css").toExternalForm());

        stage.setScene(scene);

        stage.setMaximized(true);

        stage.setFullScreen(true);

        stage.show();

        stage.setTitle("Login - Phantom App");

    }

}

