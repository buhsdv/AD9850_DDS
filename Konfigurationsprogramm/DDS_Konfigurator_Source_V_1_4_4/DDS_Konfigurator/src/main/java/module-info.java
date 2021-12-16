import com.dds_konfigurator.*;
/**
 *  <p>DDS Configurator</p>
 *  This program is used to communicate with an DIY DDS-Frequency generator based on an ADS8950 and an arduino yun.
 *  <p>
 *  The program has the following functionality: <br>
 *  - Set a static frequency in the range from 10Hz - 40MHz <br>
 *  - Set a linear sweep with the parameters <i>{start frequency, end frequency, number of steps and sweeptime}</i> <br>
 *  - Set a logarithmic sweep with the parameters <i>{start frequency, number of decades and sweeptime}</i> <br>
 *  </p>
 *
 * @author Tim Fischer and Hannes Krause
 * @version 1.4.4
 * @since 2021-11-07 (Last Build: 08.12.2021)
 *
 *
 * @see App
 * @see Main
 * @see MainController
 * @see Connection
 * @see AlertBox
 */
module com.example.dds_konfigurator {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.dds_konfigurator to javafx.fxml;
    exports com.dds_konfigurator;
}

/*
javadoc com.dds_konfigurator -classpath 'C:\Program Files\Java\javafx-sdk-17.0.1\lib' -sourcepath .\src\main\java\ --module-path 'C:\Program Files\Java\javafx-sdk-17.0.1\lib' -author -version -private -d .\Javadoc\
 */