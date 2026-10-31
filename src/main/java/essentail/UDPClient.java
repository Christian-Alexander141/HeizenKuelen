package essentail;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
public class UDPClient {

    static final String HOST = "localhost";
    static final int PORT = 29060;
    static final int TIMEOUT = 2000; // ms, so lange wird auf eine Antwort gewartet

    // sendet genau eine Anfrage und gibt genau eine Antwort zurück
    public static String from_client(String quest) throws IOException {
        try ( DatagramSocket socket = new DatagramSocket() ) {
            socket.setSoTimeout( TIMEOUT );

            // zunächst die Anfrage an den Server
            byte data[] = quest.getBytes( StandardCharsets.US_ASCII );
            InetAddress ia = InetAddress.getByName( HOST );
            DatagramPacket packet = new DatagramPacket( data, data.length, ia, PORT );
            socket.send( packet );

            // Jetzt vom Server was empfangen
            byte buffer[] = new byte[1024];
            packet = new DatagramPacket( buffer, buffer.length );
            socket.receive( packet );

            // nur die empfangenen Bytes verwenden, nicht den ganzen Puffer
            return new String( packet.getData(), 0, packet.getLength(), StandardCharsets.US_ASCII ).trim();
        }
        catch ( SocketTimeoutException e ) {
            throw new IOException( "keine Antwort von " + HOST + ":" + PORT + " (läuft der Server?)" );
        }
    }
}
