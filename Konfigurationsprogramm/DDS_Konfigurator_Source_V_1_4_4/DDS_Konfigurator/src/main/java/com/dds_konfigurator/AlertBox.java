package com.dds_konfigurator;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import java.util.Objects;

/**
 * This class is used to display a AlertBox if the User needs to be alerted
 */
public class AlertBox {

    /**
     *
     * This method is used to diplay the alertbox.
     *
     * @param title This is the title of the alertbox
     * @param message This is the custom message that will be displayed
     */
    public static void display(String title, String message) {

        // The width of the main scene
        int alertSceneWidth = 450;

        // The height of the main scene
        int alertSceneHeight = 200;

        // The path to the CSS file of the Dark Theme
        String pathModernDarkStylesheet = "css/Modern_Dark.css";

        // The path to the warning logo for the window icon
        String pathWarningLogo = "logo/warning.png";

        // Create a new Stage for the alert
        Stage alertWindow = new Stage();
        //Block user interaction until window is closed
        alertWindow.initModality(Modality.APPLICATION_MODAL);
        //Set parameters for simple window
        alertWindow.setTitle(title);
        alertWindow.setWidth(alertSceneWidth);
        alertWindow.setHeight(alertSceneHeight);
        alertWindow.setResizable(false);
        alertWindow.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResource(pathWarningLogo)).toExternalForm()));

        //General error text
        Label generalLabel1 = new Label();
        Label generalLabel2 = new Label();
        generalLabel1.setText("Es ist zu einem Fehler gekommen!");
        generalLabel2.setText("Bitte folgende Dinge ändern:");

        //Custom error text
        Label errorLabel = new Label();
        errorLabel.setText(message);

        //Button for closing the window
        Button closeButton = new Button("OK");
        //Lamda expression for closing the window
        closeButton.setOnAction(e -> alertWindow.close());

        //Simple layout
        VBox alertLayout = new VBox(10);
        alertLayout.getChildren().addAll(generalLabel1, generalLabel2, errorLabel, closeButton);
        alertLayout.setAlignment(Pos.CENTER);

        //Create the scene out of the layout and the stylesheet
        Scene alertScene = new Scene(alertLayout);
        alertScene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource(pathModernDarkStylesheet)).toExternalForm());
        alertWindow.setScene(alertScene);
        alertWindow.showAndWait();
    }
}
