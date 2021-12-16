package com.dds_konfigurator;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

/**
 * This Class is used to connect to the JavaFX GUI which is formatted inside a FXML file.<br>
 * The names of the inputs are defined by the "id"-tag in the FXML file
 * The class has an implemented basic input validation
 */
public class MainController {

    /** Define Textfield-Input for connection (IP and Port of the DDS) (link to fxml) */
    @FXML
    public TextField ipInput, portInput;

    /** Define Textfield-Inputs for the frequency input of the static frequency (link to fxml) */
    @FXML
    public TextField staticfMhzInput, staticfKhzInput, staticfHzInput;

    /** Define Textfield-Inputs for the start frequency of the linear sweep (link to fxml) */
    @FXML
    public TextField startfLaufMhzInput, startfLaufKhzInput, startfLaufHzInput;

    /** Define Textfield-Inputs for the end frequency of the linear sweep (link to fxml) */
    @FXML
    public TextField endfLaufMhzInput, endfLaufKhzInput, endfLaufHzInput;

    /** Define Textfield-Inputs for the number of steps and the sweeptime of the linear sweep (link to fxml) */
    @FXML
    public TextField nStepInput, tmsLinInput;

    /** Define Textfield-Inputs for the start frequency of the logarithmic sweep (link to fxml) */
    @FXML
    public TextField startLogfLaufMhzInput, startLogfLaufKhzInput, startLogfLaufHzInput;

    /** Define Textfield-Inputs for the number of decades and the sweeptime of the logarithmic sweep (link to fxml) */
    @FXML
    public TextField nDecInput, nStepLogInput, tmsLogInput;

    /** Define Labels for the About page */
    @FXML
    public static Label versionLabel, dateLabel, jreLabel, jfxLabel;


    /**
     * This method is called, when the "Test Connection" Button is pressed, it sends a teststring to
     * the configured IP and Port
     */
    @FXML
    public void connectTestButtonClicked() {
        //validate input
        if(isValidNetwork(ipInput, portInput)){
            Connection.sendToServer(ipInput.getText(), Integer.parseInt(portInput.getText()), buildString(4));
            System.out.println("IP: " + ipInput.getText());
            System.out.println("Port: " + portInput.getText());
            System.out.println();
        }
    }

    /**
     * This method is called when the user presses the "Senden" Button in the "Statische Frequenz" tab
     * or when "Enter" is pressed in the last field of "Statische Frequenz"
     */
    @FXML
    public void staticfSendButtonClicked() {
        if(isValidNetwork(ipInput, portInput) && isValidFrequency(staticfMhzInput, staticfKhzInput,staticfHzInput)){
            Connection.sendToServer(ipInput.getText(), Integer.parseInt(portInput.getText()), buildString(1));
            System.out.println("Frequenz: " + staticfMhzInput.getText() + "." + staticfKhzInput.getText() + "." + staticfHzInput.getText());
        }
    }

    /**
     * This method is called when the user presses the "Senden" Button in the "Linearer Frequenzgang" tab
     * or when "Enter" is pressed in the last field "Sweepzeit" in the "Linearer Frequenzgang" tab
     */
    @FXML
    public void linfLaufSendButtonClicked() {
        //Validate network info
        if(isValidNetwork(ipInput, portInput)){
            //Validate start and end frequency
            if((isValidFrequency(startfLaufMhzInput, startfLaufKhzInput,startfLaufHzInput)) && (isValidFrequency(endfLaufMhzInput, endfLaufKhzInput,endfLaufHzInput))) {
                //Validate steps and sweeptime input
                if((isValidSteps(nStepInput) && isValidTime(tmsLinInput))){
                    Connection.sendToServer(ipInput.getText(), Integer.parseInt(portInput.getText()), buildString(2));
                }
            }
        }
    }

    /**
     * This method is called when the user presses the "Senden" Button in the "Logarithmischer Frequenzgang" tab
     * or when "Enter" is pressed in the last field "Sweepzeit" in the "Logarithmischer Frequenzgang" tab
     */
    @FXML
    public void logfLaufSendButtonClicked() {
        //Validate network info
        if(isValidNetwork(ipInput, portInput)){
            //Validate start and end frequency
            if(isValidFrequency(startLogfLaufMhzInput, startLogfLaufKhzInput,startLogfLaufHzInput)) {
                //Validate decades, steps and sweeptime input
                if((isValidDecades(nDecInput)) && (isValidSteps(nStepLogInput) && isValidTime(tmsLogInput))){
                    Connection.sendToServer(ipInput.getText(), Integer.parseInt(portInput.getText()), buildString(3));
                }
            }
        }
    }

    /**
     * This method is used to build a String, in a format, that is usable by the DDS, it has the form: <br>
     * Mode 1: <i>{mode;static frequency;0;0;0;0}</i> <br>
     * Mode 2: <i>{mode;start frequency;end frequency;0; number steps; sweeptime}</i> <br>
     * Mode 3: <i>{mode;start frequency;end frequency; number decades; 0; sweeptime}</i> <br>
     * Mode 4: <i>{Teststring}</i>
     * @param mode This is used to set the mode <i>{1 = static frequency; 2 = linear wobble; 3 = logarithmic wobble; 4 = testmode}</i>
     * @return Returns the complete String that was build
     */
    public String buildString(int mode) {

        //Definition of control-characters
        char STX = 0x02;                                              //Start of text
        char ETX = 0x03;                                              //End of text
        char EOT = 0x04;                                              //End of transmission

        String dataString = null;

        //Build a complete Frequency out of the three parts (Mhz+kHz+Hz)
        String staticf = (staticfMhzInput.getText() + staticfKhzInput.getText() + staticfHzInput.getText());
        String linStartf = (startfLaufMhzInput.getText() + startfLaufKhzInput.getText() + startfLaufHzInput.getText());
        String linEndf = (endfLaufMhzInput.getText() + endfLaufKhzInput.getText() + endfLaufHzInput.getText());
        String logStartf = (startLogfLaufMhzInput.getText() + startLogfLaufKhzInput.getText() + startLogfLaufHzInput.getText());

        //Build the data string by the selected mode
        switch (mode) {
            case 0 -> System.out.println("Mode 0 not yet defined!");
            case 1 -> dataString = STX + "1" + ";" + staticf + ";" + "0" + ";" + "0" + ";" + "0" + ";" + "0" + ETX + EOT;
            case 2 -> dataString = STX + "2" + ";" + linStartf + ";" + linEndf + ";" + "0" + ";" + nStepInput.getText() + ";" + tmsLinInput.getText() + ETX + EOT;
            case 3 -> dataString = STX + "3" + ";" + logStartf + ";" + "0" + ";" + nDecInput.getText() + ";" + nStepLogInput.getText() + ";" + tmsLogInput.getText() + ETX + EOT;
            //Testmode for testing if the Transmission is successful
            case 4 -> dataString = STX + "Teststring" + ETX + EOT;
            //Error exception if wrong mode is given to the method
            default -> System.out.println("mode not defined!");
        }

        return dataString;
    }

    /**
     * This method is used to show a "About" window
     * @throws IOException Throws an IOException if an error occurs during the launch
     */
    @FXML
    public void showAbout() throws IOException {

        //load layout for the about page
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/about.fxml"));
        Scene aboutScene = new Scene(fxmlLoader.load(), 450, 300);
        aboutScene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("css/Modern_Dark.css")).toExternalForm());

        //settings for the about page
        Stage aboutStage = new Stage();
        aboutStage.initModality(Modality.APPLICATION_MODAL);
        aboutStage.setOpacity(1);
        aboutStage.setTitle("About");
        aboutStage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResource("logo/logo.png")).toExternalForm()));
        aboutStage.setScene(aboutScene);
        aboutStage.setResizable(false);

        //Show about page
        aboutStage.showAndWait();
    }

    /*
     * Input validation methods for the different input types
     */

    /**
     * Input Validation for a given IP and port
     * @param ip The IP textfield that will be checked
     * @param port The port textfield that will be checked
     * @return Return true if input is valid, false if input is invalid
     */
    public boolean isValidNetwork(TextField ip, TextField port){
        //Pattern for IPv4 adress
        String ipPattern = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        //Test if user entered ip and port
        if((!ip.getText().isEmpty()) && (!port.getText().isEmpty())){
            //test if IP is a valid IPv4
            if(ip.getText().matches(ipPattern))
                try{
                    int portInput = Integer.parseInt(port.getText());
                    //check if port is greater than 0
                    return portInput > 0;
                }
                catch (NumberFormatException e){
                    // show alertbox
                    AlertBox.display("Ungültiger Port","Bitte korrekten Port eingeben!");
                    System.out.println("Bitte korrekten Port eingeben!");
                    return false;
                }
            else{
                // show alertbox
                AlertBox.display("Ungültige IP","Bitte korrekte IP eingeben!");
                System.out.println("Bitte korrekte IP eingeben!");
                return false;
            }
        }
        else {
            // show alertbox
            AlertBox.display("Keine Verbindungsinformationen", "Bitte IP und Port eingeben!");
            System.out.println("Bitte IP und Port eingeben");
            return false;
        }
    }

    /**
     * Input Validation for a given frequency
     * @param MHz The Mhz textfield that will be checked
     * @param kHz The khz textfield that will be checked
     * @param Hz The Hz textfield that will be checked
     * @return Return true if input is valid, false if input is invalid
     */
    public boolean isValidFrequency(TextField MHz, TextField kHz, TextField Hz){
        if ((!MHz.getText().isEmpty()) && (!kHz.getText().isEmpty()) && (!Hz.getText().isEmpty())){
            try{
                int MHzInput = Integer.parseInt(MHz.getText());
                int kHzInput = Integer.parseInt(kHz.getText());
                int HzInput = Integer.parseInt(Hz.getText());
                int freqInput = (MHzInput*1000000) + (kHzInput*1000) + HzInput;
                System.out.println(freqInput);
                if((freqInput <= 40000000) && (freqInput >= 10)){
                    return true;
                }
                else{
                    // show alertbox
                    AlertBox.display("Ungültige Frequenz", "Die Frequenz muss zwischen 10Hz und 40MHz liegen!");
                    System.out.println("Die Frequenz muss zwischen 10Hz und 40MHz liegen!");
                    return false;
                }
            }
            catch(NumberFormatException e){
                // show alertbox
                AlertBox.display("Ungültige Frequenz", "Bitte gültige Frequenz eingeben");
                System.out.println("Bitte gültige Frequenz eingeben");
                return false;
            }
        }
        else{
            // show alertbox
            AlertBox.display("Leeres Textfeld", "Es darf kein Textfeld leer bleiben!");
            System.out.println("Es darf kein Textfeld leer bleiben!");
            return false;
        }
    }

    /**
     * Input Validation for a given steps
     * @param steps The steps textfield that will be checked
     * @return Return true if input is valid, false if input is invalid
     */
    public boolean isValidSteps(TextField steps){
        //test if input field is empty
        if (!steps.getText().isEmpty()){
            //test if input is an integer an in the given range
            try{
                int stepsInput = Integer.parseInt(steps.getText());
                if((stepsInput <= 2000) && (stepsInput >= 10)){
                    return true;
                }
                else{
                    // show alertbox
                    AlertBox.display("Ungültige Anzahl Schritte", "Die Anzahl der Schritte muss zwischen 10 und 2000 liegen!");
                    System.out.println("Die Anzahl der Schritte muss zwischen 10 und 2000 liegen!");
                    return false;
                }
            }
            catch(NumberFormatException e){
                // show alertbox
                AlertBox.display("Ungültige Anzahl Schritte", "Bitte gültige Anzahl Schritte eingeben");
                System.out.println("Bitte gültige Anzahl Schritte eingeben");
                return false;
            }
        }
        else{
            // show alertbox
            AlertBox.display("Leeres Textfeld", "Es darf kein Textfeld leer bleiben!");
            System.out.println("Es darf kein Textfeld leer bleiben!");
            return false;
        }
    }

    /**
     * Input Validation for a given decades
     * @param decades The decades textfield that will be checked
     * @return Return true if input is valid, false if input is invalid
     */
    public boolean isValidDecades(TextField decades){
        //test if input field is empty
        if (!decades.getText().isEmpty()){
            //test if input is an integer an in the given range
            try{
                int stepsInput = Integer.parseInt(decades.getText());
                if((stepsInput <= 7) && (stepsInput >= 1)){
                    return true;
                }
                else{
                    // show alertbox
                    AlertBox.display("Ungültige Anzahl Dekaden", "Die Anzahl der Dekaden muss zwischen 1 und 7 liegen!");
                    System.out.println("Die Anzahl der Dekaden muss zwischen 1 und 7 liegen!");
                    return false;
                }
            }
            catch(NumberFormatException e){
                // show alertbox
                AlertBox.display("Ungültige Anzahl Dekaden", "Bitte gültige Anzahl Dekaden eingeben");
                System.out.println("Bitte gültige Anzahl Dekaden eingeben");
                return false;
            }
        }
        else{
            // show alertbox
            AlertBox.display("Leeres Textfeld", "Es darf kein Textfeld leer bleiben!");
            System.out.println("Es darf kein Textfeld leer bleiben!");
            return false;
        }
    }

    /**
     * Input Validation for a given sweeptime
     * @param time The sweeptime textfield that will be checked
     * @return Return true if input is valid, false if input is invalid
     */
    public boolean isValidTime(TextField time){
        //test if input field is empty
        if (!time.getText().isEmpty()){
            //test if input is an integer an in the given range
            try{
                int stepsInput = Integer.parseInt(time.getText());
                if((stepsInput <= 10000) && (stepsInput >= 500)){
                    return true;
                }
                else{
                    // show alertbox
                    AlertBox.display("Ungültige Sweepzeit", "Die Sweepzeit muss zwischen 500ms und 10000ms liegen!");
                    System.out.println("Die Sweepzeit muss zwischen 500ms und 10000ms liegen!");
                    return false;
                }
            }
            catch(NumberFormatException e){
                // show alertbox
                AlertBox.display("Ungültige Sweepzeit", "Bitte gültige Sweepzeit eingeben");
                System.out.println("Bitte gültige Sweepzeit eingeben");
                return false;
            }
        }
        else{
            // show alertbox
            AlertBox.display("Leeres Textfeld", "Es darf kein Textfeld leer bleiben!");
            System.out.println("Es darf kein Textfeld leer bleiben!");
            return false;
        }
    }
}



/*

Internal Comment for understanding what the String format is

String : {(STX)a;b;c;d;e;f(ETX)(EOT)}

 a     =    mode            {1,2,3,(4#)}         int         (alle modi)     Gibt den gewünschten Modus an
 b     =    firstFreq       {10 - 40000000}     long int    (alle modi)     Gibt die erste Frequenz an
 c     =    secFreq         {20 - 40000000}     long int    (Modus 2)       Gibt die zweite Frequenz an
 d     =    numberDec       {1 - 7}             int         (Modus 3)       Gibt die Anzahl der Dekaden an
 e     =    numberSteps     {1 - 2000}          int         (Modus 2,3)       Gibt die Anzahl der Schritte an
 f     =    timeSweep       {500 - 10000}       int         (Modus 2,3)     Gibt die Zeit des Frequenzlaufes an

# Mode 4 is a test mode to test the connection

 */