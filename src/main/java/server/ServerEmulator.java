package server;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/*
 * Emulator für den Server (das Gerät mit Temperatursensor, Lüfter und Heizung)
 *
 * Protokoll, rekonstruiert aus dem Client-Code (je eine UDP-Nachricht als ASCII-Text):
 *   "AE,?"          -> "AE,362"        Analogeingang: Temperatur in Zehntelgrad
 *   "DE,?"          -> "DE,hlllllll"   Digitaleingänge: Rückmeldung der 8 Ausgänge, h = an, l = aus
 *   "DA,1xxxxxxx"   -> "DA,10000000"   Digitalausgänge setzen: 1 = an, 0 = aus, x = nicht ändern;
 *                                      Antwort ist der neue Zustand aller Ausgänge
 *   alles andere    -> "ER,<anfrage>"
 *
 * Kanal 1 (Index 0) = Lüfter, Kanal 2 (Index 1) = Heizung
 *
 * Starten: java server.ServerEmulator [port]   (Standard 29060)
 */
public class ServerEmulator {

    static final int KANAELE = 8;
    static final int LUEFTER = 0;
    static final int HEIZUNG = 1;

    // einfaches Temperaturmodell, Werte pro Sekunde
    static final double UMGEBUNG = 20.0;        // °C
    static final double HEIZLEISTUNG = 1.0;     // °C pro Sekunde
    static final double VERLUST = 0.01;         // Anteil der Differenz zur Umgebung, der ohne Lüfter verloren geht
    static final double LUEFTER_VERLUST = 0.1;  // zusätzlicher Anteil, wenn der Lüfter läuft

    static boolean ausgaenge[] = new boolean[KANAELE];
    static double temp = UMGEBUNG;
    static long letzte_zeit = System.nanoTime();
    static Random rauschen = new Random();

    public static void main( String args[] ) throws Exception {
        int port = args.length > 0 ? Integer.parseInt( args[0] ) : 29060;

        try ( DatagramSocket socket = new DatagramSocket( port ) ) {
            System.out.println( "Server-Emulator läuft auf UDP-Port " + port );
            byte data[] = new byte[1024];

            while ( true ) {
                // Auf Anfrage warten
                DatagramPacket packet = new DatagramPacket( data, data.length );
                socket.receive( packet );
                String anfrage = new String( packet.getData(), 0, packet.getLength(), StandardCharsets.US_ASCII ).trim();

                simuliere();
                String antwort = beantworte( anfrage );

                // Antwort nur so lang wie nötig zurückschicken
                byte out[] = antwort.getBytes( StandardCharsets.US_ASCII );
                socket.send( new DatagramPacket( out, out.length, packet.getAddress(), packet.getPort() ) );

                System.out.printf( "%-12s -> %-12s | %5.1f °C | Lüfter %-3s | Heizung %s%n",
                        anfrage, antwort, temp, an_aus( LUEFTER ), an_aus( HEIZUNG ) );
            }
        }
        catch ( BindException e ) {
            System.out.println( "Port " + port + " ist schon belegt - läuft noch ein anderer Server?" );
        }
    }

    static String beantworte( String anfrage ) {
        if ( anfrage.equals( "AE,?" ) ) {
            // Messwert mit etwa ±0,1 °C Rauschen
            long wert = Math.round( temp * 10 + rauschen.nextGaussian() );
            return "AE," + wert;
        }
        if ( anfrage.equals( "DE,?" ) ) {
            StringBuilder s = new StringBuilder( "DE," );
            for ( boolean a : ausgaenge ) s.append( a ? 'h' : 'l' );
            return s.toString();
        }
        if ( anfrage.startsWith( "DA," ) ) {
            String bits = anfrage.substring( 3 );
            if ( bits.length() != KANAELE || !bits.matches( "[01x]+" ) ) return "ER," + anfrage;

            for ( int i = 0; i < KANAELE; i++ ) {
                if ( bits.charAt( i ) == '1' ) ausgaenge[i] = true;
                if ( bits.charAt( i ) == '0' ) ausgaenge[i] = false;
            }
            StringBuilder s = new StringBuilder( "DA," );
            for ( boolean a : ausgaenge ) s.append( a ? '1' : '0' );
            return s.toString();
        }
        return "ER," + anfrage;
    }

    // Temperatur seit der letzten Anfrage weiterrechnen
    static void simuliere() {
        long jetzt = System.nanoTime();
        double sekunden = ( jetzt - letzte_zeit ) / 1e9;
        letzte_zeit = jetzt;

        // in kleinen Schritten rechnen, damit auch lange Pausen stabil bleiben
        while ( sekunden > 0 ) {
            double dt = Math.min( 0.1, sekunden );
            double verlust = ausgaenge[LUEFTER] ? VERLUST + LUEFTER_VERLUST : VERLUST;
            double aenderung = -( temp - UMGEBUNG ) * verlust;
            if ( ausgaenge[HEIZUNG] ) aenderung += HEIZLEISTUNG;
            temp += aenderung * dt;
            sekunden -= dt;
        }
    }

    static String an_aus( int kanal ) {
        return ausgaenge[kanal] ? "AN" : "AUS";
    }
}
