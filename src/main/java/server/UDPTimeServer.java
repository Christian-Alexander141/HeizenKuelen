package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

public class UDPTimeServer
{
    public static void main( String args[] ) {
        String temp_antwort= "DE,abcdefgh";

        try {
            byte data[] = new byte[ 1024 ];

            DatagramPacket packet;
            DatagramSocket socket = new DatagramSocket( 29060 );

            while ( true )
            {
                // Auf Anfrage warten

                packet = new DatagramPacket( data, data.length );
                socket.receive( packet );

                // Empfänger auslesen, aber Paketinhalt ignorieren

                InetAddress adress = packet.getAddress();
                int         port   = packet.getPort();

                System.out.println( "Anfrage von " + packet.getAddress() +
                        " am Port " + packet.getPort() );

                // Paket für Empfänger zusammenbauen
                // hier kann die information gesedet werden





                String s = temp_antwort;
                s.getBytes( 0, s.length(), data, 0 );
                packet = new DatagramPacket(data,data.length,adress,port);
                socket.send( packet );

                Thread.sleep( 10000 );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e );
        }
    }
}