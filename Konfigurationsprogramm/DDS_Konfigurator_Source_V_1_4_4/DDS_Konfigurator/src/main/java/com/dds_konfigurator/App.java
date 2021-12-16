package com.dds_konfigurator;

/**
 * This class is used to generate a runnable instance of the program
 */
public class App {

    /* Definition of global info about the program */
    /** Definition of version for later use */
    public static String version = "1.4.4";
    /** Definition of build date for later use */
    public static String date = "2021-12-05";
    /** Definition of JRE version for later use */
    public static String jreVersion = "17.0.1";
    /** Definition of JavaFX version for later use */
    public static String jfxVersion = "17.0.1";

    /**
     * This main method is used to start the program
     * This is due to the fact, that for running the jar the first class that is called should not extend Application
     * @param args You can give the programs some args (unused at the moment)
     * @see Main
     */
    public static void main(String[] args){
        Main.main(args);
    }


}
