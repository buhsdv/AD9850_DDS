% exemplarische  Darstellung logarithmischer Sweep
close all 
clear all


%% mathematische Modellierung 
%Sweepen von 0 bis 5000ms
x=0:0.001:5
%Frequenzänderung: f(t) = f0*k.^t mit k = (f1/f0).^(1/T)
%                                     k = (Endfreq./Anfangsfreq.)^(1/Sweepzeit in s)
k=(100/1).^(1/5)
%Berechnung eines sinusförmigen Signals mit zeitlich exponentiell zunehmender Frequenz
%aufgrund zeitlich logarithmischer Phasenänderung
%Phasenänderung:
phi = 2*pi*1+2*pi*((k.^x-1)/(log(k)))
%Sinussignal:
y=sin(phi)
%Ausgabe Funktionsgraph
plot(y)
xlabel('Zeit (ms)')
ylabel('Amplitude')
grid on 