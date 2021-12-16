package com.dds_konfigurator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This Class is used to connect to the Arduino via TCP-Connection
 */
public class Connection {

    /**
     * This method is called to send a String to the Arduino via TCP
     * @param IP This is the IP of the Server to connect to
     * @param port This is the Port to connect to
     * @param Message This is the String that will be send
     */
    public static void sendToServer(String IP, int port, String Message) {
        //Try to establish a connection
        try (Socket socket = new Socket(IP, port)) {
            //Send data
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(Message);
            System.out.println("Message send: " + Message);
            System.out.println("To: " + IP + ":" + port);

        } catch (UnknownHostException ex) {
            //Build exception String
            String unknownHostExceptionString = "Server not found: ".concat(ex.getMessage());
            //Give out error message
            System.out.println(unknownHostExceptionString);
            AlertBox.display("Übermittlungsfehler", unknownHostExceptionString);

        } catch (IOException ex) {
            //Build exception String
            String unknownIOExceptionString = "IO Error: ".concat(ex.getMessage());
            //Give out error message
            System.out.println(unknownIOExceptionString);
            AlertBox.display("Übermittlungsfehler", unknownIOExceptionString);
        }
    }
}
