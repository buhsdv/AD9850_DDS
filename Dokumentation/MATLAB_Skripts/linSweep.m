% exemplarische  Darstellung linearer Sweep
close all 
clear all


%% mathematische Modellierung 
%Sweepen von 0 bis 5000ms
x=0:0.001:5
%Frequenzänderung; (Anfangsfrequenz-Endfrequenz)/Sweepzeit
c=(11-1)/(5)
%Berechnung eines sinusförmigen Signals mit zeitlich linear zunehmender Frequenz
%aufgrund zeutlich quadratischer Phasenänderung
phi = 2*pi+2*pi*(c*0.5*x.^2+x)
y=sin(phi)
%Ausgabe Funktionsgraph
plot(y)
xlabel('Zeit (ms)')
ylabel('Amplitude')
grid on 

