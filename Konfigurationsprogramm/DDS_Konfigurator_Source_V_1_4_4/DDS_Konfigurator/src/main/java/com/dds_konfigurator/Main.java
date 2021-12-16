package com.dds_konfigurator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

/**
 * This is the Main class of the Program, it is call by the App-Class and sets up the program an the GUI
 * <p>The in and outputs are stored in a FXML file and are handled by the "MainController" class </p>
 * @see MainController
 * @see Connection
 * @see App
 * @see AlertBox
 */
public class Main extends Application {

    /** The width of the main scene */
    public int sceneWidth = 600;

    /** The height of the main scene */
    public int sceneHeight = 450;

    /** The title of the main scene */
    public String title = "DDS Konfigurator";

    /** The path to the logo of the main scene */
    public String pathLogo = "logo/logo.png";

    /** The path to the FXML of the main scene */
    public String pathMainFXML = "fxml/mainLayout.fxml";

    /** The path to the CSS file of the Dark Theme */
    public String pathModernDarkStylesheet = "css/Modern_Dark.css";

    /**
     * This method is used to generate a GUI-window (Stage)
     * @param mainStage The main window of the program
     * @throws IOException Throws an IOException, if an error occurs during the launch of the GUI-window
     * @see IOException
     */
    @Override
    public void start(Stage mainStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(pathMainFXML));
        Scene scene = new Scene(fxmlLoader.load(), sceneWidth, sceneHeight);
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource(pathModernDarkStylesheet)).toExternalForm());

        mainStage.setTitle(title);
        mainStage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResource(pathLogo)).toExternalForm()));
        mainStage.setScene(scene);
        mainStage.setResizable(false);
        mainStage.show();
        mainStage.setOnCloseRequest(e -> closeProgram());
    }

    /**
     * This method is used to launch the GUI program
     * @param args Unused
     * @see Application
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * This method is called when the program gets closed by the user, it can be used to save settings
     */
    private void closeProgram(){
        System.out.println("Program terminated");
    }
}