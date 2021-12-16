/******************************************************************************************************************
 * Arduino sketch for a simple stand-alone DDS-Frequency-Generator with sine and rectangular output based on the 
 * AD9850 plus optional remote setting of all parameters via wifi (requires Arduino Yun + Java-programm (Name des Files))
 *   
 * Created 04/12/2021
 * Tim Fischer 
 * Johannes Krause
 *
 * Use this code freely!
 * 
 * This frequency generator is able to output a sinusoidal and rectangular signal within a frequency range between   
 * 10Hz and 40Mhz. The user can choose if the output frequency should be static or be repeatedly changed as a linear
 * or logarithmic sweep. The start-/end-frequency, number of passed decades (logarithmic sweep only), duration and
 * resolution (number of single steps) of either sweep are fully adjustable.
 * All parameters can be adjusted by running through the included menu-structure using a rotary-encoder and are 
 * displayed on a 2-line lcd. For a more comfortable user-experience the DDS-generator can be controlled with an ex-
 * ternal Java-program using the WLAN-interface of the Yun, offering a clear graphical user interface for easy 
 * adjustment of all parameters.
 *
 *the following libraries were included in this project:
 * AD9850           https://github.com/F4GOJ/AD9850                                     by F4GOJ Christophe f4goj@free.fr   2014
 * ClickEncoder     https://github.com/0xPIT/encoder                                    by karl@pitrich.com, Copyright (c)  2010-2014
 * SimpleMenu       https://github.com/tinkersprojects/LCD-Simple-Menu-Library          by Tinkers Projects, Copyright (c)  2020
 * 
 * LiquidCrystal                                                                        by Arduino foundation
 * TimerOne                                                                             by Jesse Tane, Jérôme Despatis, Michael Polli, Dan Clemens, Paul Stoffregen (Arduino foundation)
 * Bridge                                                                               by Arduino foundation
 * BridgeServer                                                                         by Arduino foundation
 * BridgeClient                                                                         by Arduino foundation
 * 
 * 
 * AD9850 datasheet at http://www.analog.com/static/imported-files/data_sheets/AD9850.pdf
 *
 *****************************************************************************************************************/

#include <SimpleMenu.h>
#include <LiquidCrystal.h>
#include <ClickEncoder.h>
#include <TimerOne.h>
#include <AD9850.h>
#include <Bridge.h>
#include <BridgeServer.h>
#include <BridgeClient.h>

//definition of used pins for LCD
#define LCD_RS            12
#define LCD_EN            13
#define LCD_D4            7
#define LCD_D5            6
#define LCD_D6            5
#define LCD_D7            4

//definition how big LCD is
#define LCD_CHARS         16
#define LCD_LINES         2

//definition of rotary encoder Pins
#define ROTARY_SW         A2
#define ROTARY_DT         A1
#define ROTARY_CLK        A0

//definition of button Pins
#define BUTTON_LEFT       A3
#define BUTTON_MIDDLE     A4
#define BUTTON_RIGHT      A5

//definition of AD9850 Pins
#define AD9850_W_CLK      8
#define AD9850_FQ_UD      9
#define AD9850_DATA       11
#define AD9850_RESET      10

//definition for TCP connection
#define PORT 255

//Variables for AD9850
unsigned long int staticFreq = 10;
unsigned long int linStartFreq = 10;
unsigned long int logStartFreq = 10;
unsigned long int linEndFreq = 10;
unsigned long int linSweepTime = 0;
unsigned long int logSweepTime = 0;
unsigned long int startFreq = 0;
unsigned long int stopFreq = 0;
unsigned long int trimFreq = 124999500;
int phase = 0;
int linSteps = 200;
int logSteps = 200;
int logDecades = 0;
boolean output = false;

//variables for rotary-encoder
unsigned long int lastValue;
unsigned long int changeValue;
int stepMode = 0;
int valueMode = 0;
boolean turnRight = false;
boolean turnLeft = false;
boolean doBreak = false;
boolean doSelect = false;

//variables for TCP connection
int mode = 0;
int nDec = 0;
int nStep = 0;
int tms = 0;

char thisChar;
String currentString;
char workString[30];
boolean alreadyConnected = false;

//general function heads
void setHighValue(long int *value, int mode); 
void info();
void goBack();

//function heads for TCP connection
void startBridge();
void getTCPData();

//funtion heads for frequency output
void setStaticFreq();
void setLinStartFreq();
void setLogStartFreq();
void setLinEndFreq();
void staticFreqStart();
void setLinSweeptime();
void setLogSweeptime();
void linFreqStart();
void logFreqStart();
void linSweep(long int freqStart, long int freqStop, int nstep, long int tms);
void logSweep(long int freqStart, int nDec, int nstep, int tms);

//Submenu-structure for setting parameters for static frequency
SimpleMenu MenuSubStatic[4] = {
  SimpleMenu("Frequenz", setStaticFreq),
  SimpleMenu("Phase", &phase, 0, 90),
  SimpleMenu("Starten", staticFreqStart),
  SimpleMenu("Zurueck", goBack)
};
//Submenu-structure for setting parameters for linear sweep
SimpleMenu MenuSubLin[6] = {
  SimpleMenu("Startfrequenz", setLinStartFreq),
  SimpleMenu("Endfrequenz", setLinEndFreq),
  SimpleMenu("Anzahl Schritte", &linSteps, 50, 500),
  SimpleMenu("Sweepzeit", setLinSweeptime),
  SimpleMenu("Starten", linFreqStart),
  SimpleMenu("Zurueck", goBack)
};
//Submenu-structure for setting parameters for logarithmic sweep
SimpleMenu MenuSubLog[6] = {
  SimpleMenu("Startfrequenz", setLogStartFreq),
  SimpleMenu("Anzahl Dekaden", &logDecades, 0, 7),
  SimpleMenu("Anzahl Schritte", &logSteps, 50, 500),
  SimpleMenu("Sweepzeit", setLogSweeptime),
  SimpleMenu("Starten", logFreqStart),
  SimpleMenu("Zurueck", goBack)
};
//Main-menu structure for choosing between different output-signals
SimpleMenu Menu[4] = {
  SimpleMenu("Statische Freq.", 4, MenuSubStatic),
  SimpleMenu("Lin. Freqlauf", 6, MenuSubLin),
  SimpleMenu("Log. Freqlauf", 6, MenuSubLog),
  SimpleMenu("Info", info)
};


BridgeServer server(PORT);
SimpleMenu TopMenu(4, Menu);
//create display
LiquidCrystal lcd = LiquidCrystal(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7);

//define pointer and work-value for rotary encoder
ClickEncoder *encoder;
int16_t last, value;

//ISR to react if encoder is used
void timerIsr() {
  encoder->service();
}

//scroll through menu  
void display(SimpleMenu *_menu) {
  lcd.clear();
  lcd.print(">");
  lcd.print(_menu->name);

  SimpleMenu *next = TopMenu.next();
  if (next != NULL)
  {
    lcd.setCursor(1, 1);
    lcd.print(next->name);
  }
}
//show value of variable directly included in menu-structure
void displayValue(SimpleMenu *_menu) {
  lcd.clear();
  lcd.print(_menu->name);
  lcd.setCursor(0, 1);
  lcd.print(_menu->getValue());
}


void setup()
{
  //start serial communication
  pinMode(13, OUTPUT);
  digitalWrite(13,HIGH);
  Serial.begin(9600);
  startBridge();
  digitalWrite(13,LOW);
  lcd.clear();

  //create new encoder-object 
  encoder = new ClickEncoder(A0, A1, A2, 4);
  //enable/disable acceleration-sensitivity of rotary encoder
  encoder->setAccelerationEnabled(false);
  Timer1.initialize(1000);
  Timer1.attachInterrupt(timerIsr);
  last = encoder->getValue();

  //Start AD9850 and calibrate it
  DDS.begin(AD9850_W_CLK, AD9850_FQ_UD, AD9850_DATA, AD9850_RESET);
  DDS.calibrate(trimFreq);

  //start up menu
  lcd.begin(16, 2);
  TopMenu.begin(display, displayValue);
}

//main loop which is executed every cycle
void loop()
{
  readRotaryEncoder();
  getTCPData();
}

//analyse, if and which action is performed with rotary-encoder
void readRotaryEncoder() {

//check if and how button is pressed
  ClickEncoder::Button b = encoder->getButton();
  if (b != ClickEncoder::Open) {
    switch (b) {
      case ClickEncoder::Clicked:
        TopMenu.select();
        doSelect = 1;
        delay(150);
        break;
      case ClickEncoder::Held:
        TopMenu.back();
        doBreak = 1;
        delay(1000);
        //Serial.println("Going back");
        break;
    }
  }

//get value of rotary encoder and decide if it is turned left or right
  value += encoder->getValue();

  if (value > last) {
    last = value;
    TopMenu.up();
    turnLeft = 1;
    delay(100);
  }
  else   if (value < last) {
    last = value;
    TopMenu.down();
    turnRight = 1;
    delay(100);
  }
}


void getTCPData(){
  // Get clients coming from server
  BridgeClient client = server.accept();

  // There is a new client?
  if (client) {
    if (!alreadyConnected) {
      // clead out the input buffer:
      client.flush();
      client.write(0x11);
      alreadyConnected = true;
    }

    if (client.available() > 0) {
      // read the bytes incoming from the client:
      if(thisChar = client.read() == 0x02){
        while ((thisChar = client.read()) != 0x03){
          if(thisChar != 0x04){
            //add all char in buffer to one String (currentString)
            currentString.concat(thisChar);
          }
          else{
            //String wrongly terminated
            break;
          }
        }
        //Send a confirmation, that the transmission was completed succesfully
        client.write(0x06);

        //Convert String to char-Array (atoi only works with char array)
        currentString.toCharArray(workString, 30);

        //cut String into the single parameters
        mode = atoi(strtok(workString, ";"));
        startFreq = atol(strtok(NULL, ";"));
        stopFreq = atol(strtok(NULL, ";"));
        nDec = atoi(strtok(NULL, ";"));
        nStep = atoi(strtok(NULL, ";"));
        tms = atoi(strtok(NULL, ";"));

        //start the function depending on the chosen mode
        switch(mode) {
          case 1:
            staticFreq = startFreq;
            goBack();
            goBack();
            TopMenu.index(0);
            TopMenu.select();
            TopMenu.index(2);
            TopMenu.select();
            TopMenu.select();
            break;
          case 2:
            linStartFreq = startFreq;
            linEndFreq = stopFreq;
            linSteps = nStep;
            linSweepTime = tms;
            goBack();
            goBack();
            TopMenu.index(1);
            TopMenu.select();
            TopMenu.index(4);
            TopMenu.select();
            TopMenu.select();
            break;
          case 3:
            logStartFreq = startFreq;
            logDecades = nDec;
            logSteps = nStep;
            logSweepTime = tms;
            goBack();
            goBack();
            TopMenu.index(2);
            TopMenu.select();
            TopMenu.index(4);
            TopMenu.select();
            TopMenu.select();
            break;
          case 4:
            //Testcase to test the connection
            break;
        }

        //Clear strings for next transmission
        workString[0] = 0;
        currentString = "";

      }
      else {
        //Serial.println("Wrong String send!");
      }
      //terminate TCP connection
      client.stop();
    }
  }
  }

//set static frequency
void setStaticFreq() {
  lcd.clear();
  setHighValue(&staticFreq, 1);
}
//set start frequency of linear sweep
void setLinStartFreq() {
  lcd.clear();
  setHighValue(&linStartFreq, 1);
}
//set start frequency of logarithmic sweep
void setLogStartFreq() {
  lcd.clear();
  setHighValue(&logStartFreq, 1);
}
//set end ferquency of linear sweep
void setLinEndFreq() {
  lcd.clear();
  setHighValue(&linEndFreq, 1);
}
//set duration of one linear sweep cycle in ms
void setLinSweeptime() {
  lcd.clear();
  setHighValue(&linSweepTime, 2);
}
//set duration of one logarithmic sweep cycle in ms
void setLogSweeptime() {
  lcd.clear();
  setHighValue(&logSweepTime, 2);
}

//start/stop output of previously set static frequency:
void staticFreqStart() {
  //formatting of lcd
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("f=");
  lcd.setCursor(2, 0);
  lcd.print(staticFreq);
  lcd.setCursor(11, 0);
  lcd.print("Ph=");
  lcd.setCursor(14, 0);
  lcd.print(phase);
  
  while (1) {
    readRotaryEncoder();
    //leave loop, if Encoder is pressed long
    if (doBreak == 1) {
      doBreak = 0;
      output = 0;
      break;
    }
    //start/stop output, if Encoder is clicked
    if (doSelect == 1) {
      doSelect = 0;
      if (output == 0) {
        output = 1;
      }
      else {
        output = 0;
      }
    }
    //print on lcd, if output-signal is turned on or off
    if (output == 1) {
      DDS.setfreq(staticFreq, phase);
      lcd.setCursor(0, 1);
      lcd.print("Output: ON  ");
    }
    else if (output == 0) {
      DDS.setfreq(0, 0);
      lcd.setCursor(0, 1);
      lcd.print("Output: OFF ");
    }

  }
  //stop frequency output, if loop is left
  DDS.setfreq(0, 0);
}



//start/stop output of previously set linear sweep:
void linFreqStart() {
  while (1) {
    readRotaryEncoder();
    //leave loop, if Encoder is pressed long
    if (doBreak == 1) {
      doBreak = 0;
      break;
    }
    //call actual function for linear sweep
    linSweep(linStartFreq, linEndFreq, linSteps, linSweepTime);
    delay(100);
  }
  DDS.setfreq(0, 0);
}

//start/stop output of previously set logarithmic sweep:
void logFreqStart() {
  while (1) {
    readRotaryEncoder();
    //leave loop, if Encoder is pressed long
    if (doBreak == 1) {
      doBreak = 0;
      break;
    }
    //call actual function for logarithmic sweep
    logSweep(logStartFreq, logDecades, logSteps, logSweepTime);
    delay(100);
  }
  DDS.setfreq(0, 0);
}


//Settings

void info() {
  lcd.clear();
  lcd.print("DDS  f-Generator");
  lcd.setCursor(0, 1);
  lcd.print(" Hannes und Tim ");
  delay(5000);
}
void goBack() {
  TopMenu.back();
  TopMenu.back();
  delay(200);
}

/*
   Mode 1 : Frequency
   Mode 2 : Time
*/
void setHighValue(long int *value, int mode) {
  valueMode = mode;
  stepMode = 0;
  while (1) {
  //leave loop, if RotaryEncoder is held long    
    readRotaryEncoder();
    if (doBreak == 1) {
      doBreak = 0;
      break;
    }
//increment step mode by clicking the encoder
    if (doSelect == 1) {
      doSelect = 0;
      stepMode++;
      if (stepMode > 3) {
        stepMode = 0;
      }
    }

    switch (valueMode) {
      case 1:
        //if Encoder is turned right:
        if (turnRight == 1) {
          turnRight = 0;
          *value += changeValue;
          if (*value > 40000000) {
            *value = lastValue;
          }
        }
        //if Encoder is turned left:
        else if (turnLeft == 1) {
          turnLeft = 0;
          *value -= changeValue;
          if (*value < 10) {
            *value = lastValue;
          }
        }


        //definig different step sizes
        switch (stepMode) {
          //1 step = 10Hz
          case 0:
            changeValue = 10;
            lcd.setCursor(0, 1);
            lcd.print("Step:  10Hz");
            break;
          case 1:
            changeValue = 100;
            lcd.setCursor(0, 1);
            lcd.print("Step: 100Hz");
            break;
          case 2:
            changeValue = 10000;
            lcd.setCursor(0, 1);
            lcd.print("Step: 10KHz");
            break;
          case 3:
            changeValue = 1000000;
            lcd.setCursor(0, 1);
            lcd.print("Step:  1MHz");
            break;
        }


        //show current frequency on lcd, update display only if frequency changed
        if (*value != lastValue) {
          lastValue = *value;
          lcd.setCursor(0, 0);
          lcd.print("               ");
          lcd.setCursor(0, 0);
          lcd.print(*value);
        }
        break;
      case 2:
        //if Encoder is turned right:
        if (turnRight == 1) {
          turnRight = 0;
          *value += changeValue;
          if (*value > 60000) {
            *value = lastValue;
          }
        }
        //if Encoder is turned left:
        else if (turnLeft == 1) {
          turnLeft = 0;
          *value -= changeValue;
          if (*value < 10) {
            *value = lastValue;
          }
        }


        //definig different step sizes
        switch (stepMode) {
          //1 step = 10ms
          case 0:
            changeValue = 10;
            lcd.setCursor(0, 1);
            lcd.print("Step:  10ms");
            break;
          case 1:
            changeValue = 100;
            lcd.setCursor(0, 1);
            lcd.print("Step: 100ms");
            break;
          case 2:
            changeValue = 1000;
            lcd.setCursor(0, 1);
            lcd.print("Step:    1s");
            break;
          case 3:
            changeValue = 10000;
            lcd.setCursor(0, 1);
            lcd.print("Step:   10s");
            break;
        }


        //show current time in ms on lcd, update display only if time changed
        if (*value != lastValue) {
          lastValue = *value;
          lcd.setCursor(0, 0);
          lcd.print("               ");
          lcd.setCursor(0, 0);
          lcd.print(*value);

        }
    }
  }
}
//sweep frequency linear
void linSweep(long int freqStart, long int freqStop, int nstep, long int tms) {
// freqStart: start-frequency
// freqStop: stop-frequency
// nstep: number of sweep-steps 
// tms: sweeptime in ms

  //local variables for changing display output
  boolean page = true;
  unsigned long previousMillis = 0;
  unsigned long interval = 2000;
  //local variables for linear sweep
  long int i, dFreq, freq, dt;
  unsigned long t1;

  //calculate change of frequency per step
  dFreq = (freqStop - freqStart) / (nstep - 1);
  //calculate max. duration of one step
  dt = tms / (nstep - 1);
  //loop for the output of the frequency in relation to step-number,
  //each for a certain amount of time (dt)
  for (i = 0; i < nstep; i++) {
    //leave loop, if Encoder is held long
    readRotaryEncoder();
    if (doBreak == 1) {
      break;
    }
    //change page, if one page was shown for the duration in ms defined in interval
    if ((millis() - previousMillis) > interval) {
      previousMillis = millis();
      lcd.clear();
      if (page == 0) {
        page = 1;
      }
      else {
        page = 0;
      }
    }
    //formatting of first page
    if (page == 0) {
      lcd.setCursor(0, 0);
      lcd.print("Start:");
      lcd.print(linStartFreq);
      lcd.print("Hz");
      lcd.setCursor(0, 1);
      lcd.print("End:");
      lcd.print(linEndFreq);
      lcd.print("Hz");
    }
    //formatting of second page
    else if (page == 1) {
      lcd.setCursor(0, 0);
      lcd.print("Zeit:");
      lcd.print(linSweepTime);
      lcd.print("ms");
      lcd.setCursor(0, 1);
      lcd.print("Schritte:");
      lcd.print(linSteps);
    }
    
    t1 = millis();
    //calculate output-frequency for each step
    freq = freqStart + (i * dFreq);
    //output calculated frequency 
    DDS.setfreq(freq, phase);
    //wait, until next step can be executed
    while ((millis() - t1) < dt) {}
  }
}

//sweep frequency logarithmically
void logSweep(long int freqStart, int nDec, int nstep, int tms) {
// freqStart: start frequency
// nDec: number of decades 
// nstep: number of sweep-steps
// tms: sweep time in ms

  //local variables for changing display output
  boolean page = true;
  unsigned long previousMillis = 0;
  unsigned long interval = 2000;
  //local variables for logarithmic sweep
  long int i, freqStop, freq, res, dt;
  //i: counter for loop
  //freqStop: end frequency of sweep
  //res: resolution of conversion in logarithmic values
  //dt: time for one step
  double y;
  unsigned long t1;
  
  //resolution of conversion in logarithmic values (here 16 bit)
  res = 65536;  
  //calculation of stop-frequency                                          
  freqStop = freqStart * pow(10, nDec);   
  //calculate max. duration of one step                
  dt = tms / (nstep - 1);
  //loop for the output of the frequency in relation to step-number,
  //each for a certain amount of time (dt)
  for (i = 0; i < nstep; i++) {
    readRotaryEncoder();
    //leave loop, if Encoder is held long
    if (doBreak == 1) {
      break;
    }
    //change page, if one page was shown for the duration in ms defined in interval
    if ((millis() - previousMillis) > interval) {
      previousMillis = millis();
      lcd.clear();
      if (page == 0) {
        page = 1;
      }
      else {
        page = 0;
      }
    }
    //formatting of first page
    if (page == 0) {
      lcd.setCursor(0, 0);
      lcd.print("Start:");
      lcd.print(logStartFreq);
      lcd.print("Hz");
      lcd.setCursor(0, 1);
      lcd.print("Dekaden:");
      lcd.print(logDecades);
    }
    //formatting of second page
    else if (page == 1) {
      lcd.setCursor(0, 0);
      lcd.print("Zeit:");
      lcd.print(logSweepTime);
      lcd.print("ms");
      lcd.setCursor(0, 1);
      lcd.print("Schritte:");
      lcd.print(logSteps);
    }
    
    t1 = millis();
    //compare with determination of tables for PWM https://www.mikrocontroller.net/articles/LED-Fading
    //calculate exponential increase of y in relation of i
    y = pow(2, (log(res) / log(2)) * (i + 1) / nstep) - 1;  
    //calculate frequency                                
    freq = freqStart + ((freqStop - freqStart) / (res - 1.0)) * y;
    DDS.setfreq(freq, phase);
    //wait until next step can be executed
    while ((millis() - t1) < dt) {}                       
  }
}

  void startBridge(){
  // Bridge startup
  pinMode(13, OUTPUT);
  digitalWrite(13, LOW);
  Bridge.begin();
  digitalWrite(13, HIGH);
  server.begin();
  }
