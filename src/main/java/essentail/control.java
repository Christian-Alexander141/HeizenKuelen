package essentail;

import java.io.IOException;

public class control {

    // Kanäle am Digitalausgang (DA) bzw. Digitaleingang (DE), gezählt ab 0
    static final int FAN_CHANNEL = 0;
    static final int HEATER_CHANNEL = 1;

    // Temperatur in Zehntelgrad, z.B. 375 = 37,5 °C
    public static int get_temp() throws IOException {
        String temp_request = "AE,?";
        String answer = UDPClient.from_client(temp_request); // z.B. "AE,362"
        String value = value_of(answer, "AE");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IOException("Temperatur ist keine Zahl: \"" + answer + "\"");
        }
    }


    public static void fan(boolean set) throws IOException {
        switch_output(FAN_CHANNEL, set);
    }

    public static void heater(boolean set) throws IOException {
        switch_output(HEATER_CHANNEL, set);
    }

    public static boolean fan_is_on() throws IOException {
        return is_on(FAN_CHANNEL);
    }

    public static boolean heater_is_on() throws IOException {
        return is_on(HEATER_CHANNEL);
    }

    // Status eines Kanals über DE abfragen, 'h' = an
    static boolean is_on(int channel) throws IOException {
        String status = value_of(UDPClient.from_client("DE,?"), "DE"); // z.B. "hlllllll"
        if (status.length() <= channel) {
            throw new IOException("Kanal " + channel + " fehlt in der DE-Antwort: \"" + status + "\"");
        }
        return status.charAt(channel) == 'h';
    }

    // nur schalten, wenn der Status noch nicht stimmt
    static void switch_output(int channel, boolean set) throws IOException {
        if (is_on(channel) != set) {
            char bits[] = "xxxxxxxx".toCharArray(); // x = Kanal nicht verändern
            bits[channel] = set ? '1' : '0';
            value_of(UDPClient.from_client("DA," + new String(bits)), "DA");
        }
    }

    // "AE,362" -> "362", wirft eine Exception, wenn die Antwort nicht zur Anfrage passt
    static String value_of(String answer, String expected) throws IOException {
        String parts[] = answer.split(",", 2);
        if (parts.length != 2 || !parts[0].equals(expected)) {
            throw new IOException("unerwartete Antwort vom Server: \"" + answer + "\"");
        }
        return parts[1];
    }

}
