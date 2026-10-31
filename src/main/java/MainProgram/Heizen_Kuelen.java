package MainProgram;

// Zweipunktregler: über OG kühlen, unter UG heizen, dazwischen bleibt der letzte Zustand
import essentail.control;

import java.io.IOException;

public class Heizen_Kuelen {

    public static final int OG = 375; // 37,5 °C
    public static final int UG = 350; // 35,0 °C

    public static void init () throws IOException {
        control.fan(false);
        control.heater(true);
    }

    // ein Durchlauf des Reglers, gibt die gemessene Temperatur zurück
    public static int regel_schritt(int OG,int UG) throws IOException {
        int temp = control.get_temp();
        if (temp > OG) {
            control.fan(true);
            control.heater(false);
        } else if (temp < UG) {
            control.fan(false);
            control.heater(true);
        }
        return temp;
    }

    public static void flus_controll(int OG,int UG) throws InterruptedException {
        while (true) {
            try {
                int temp = regel_schritt(OG, UG);
                String modus = control.fan_is_on() ? "kühlen" : "heizen";
                System.out.printf("Temperatur %5.1f °C -> %s%n", temp / 10.0, modus);
            } catch (IOException e) {
                // Server weg oder falsche Antwort: melden und beim nächsten Durchlauf neu versuchen
                System.out.println("Fehler: " + e.getMessage());
            }
            Thread.sleep(1000); // abfrage jede sekunde
        }//while
    }//mehtode

    public static void main (String []args) throws InterruptedException {
        while (true) {
            try {
                init();
                break;
            } catch (IOException e) {
                System.out.println("Fehler beim Start: " + e.getMessage() + " - neuer Versuch in 1 s");
                Thread.sleep(1000);
            }
        }
        flus_controll(OG,UG);
    }

}
