import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;


public class OTTVideo extends JFrame implements Runnable {

    JLabel label;

    //RTP variables:
    //----------------
    DatagramPacket rcvdp; //UDP packet containing the video frames (to send)A
    DatagramSocket RTPsocket; //socket to be used to receive UDP packet
    DatagramSocket RTPsocket2; //socket to be used to send UDP packet
    int RTP_dest_port = 25000; //destination port for RTP packets
    InetAddress ClientIPAddr; //Client IP address
    InetAddress ClientIPAddr2; //Client IP address

    //Video constants:
    //------------------
    //int imagenb = 0; //image nb of the image currently transmitted
    //VideoStream video; //VideoStream object used to access video frames
    //static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
    //static int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
    //static int VIDEO_LENGTH = 500; //length of the video in frames

    Timer sTimer; //timer used to send the images at the video frame rate
    byte[] sBuf; //buffer used to store the images to send to the client

    //--------------------------
    //Constructor
    //--------------------------
    public OTTVideo(String ipR,String ipS) {
        //init para a parte do cliente
        //--------------------------
        sTimer = new Timer(20, new clientTimerListener());
        sTimer.setInitialDelay(0);
        sTimer.setCoalesce(true);
        sBuf = new byte[15000]; //allocate enough memory for the buffer used to receive data from the server

        try {
            ClientIPAddr = InetAddress.getByName(ipR);
            ClientIPAddr2 = InetAddress.getByName(ipS);
            RTPsocket = new DatagramSocket(RTP_dest_port,ClientIPAddr); //init RTP socket
            RTPsocket2 = new DatagramSocket(RTP_dest_port,ClientIPAddr2); //init RTP socket
            //ClientIPAddr = InetAddress.getByName("127.0.0.1");
        } catch (SocketException e) {
            System.out.println("Servidor: erro no socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Servidor: erro no video: " + e.getMessage());
        }

        //Handler to close the main window
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //stop the timer and exit
                sTimer.stop();
                System.exit(0);
            }});

        //GUI:
        label = new JLabel("Send frame #        ", JLabel.CENTER);
        getContentPane().add(label, BorderLayout.CENTER);

        sTimer.start();

    }

    //------------------------------------
    //main
    //------------------------------------
    public void run()
    {
    }


    class clientTimerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            //Construct a DatagramPacket to receive data from the UDP socket
            rcvdp = new DatagramPacket(sBuf, sBuf.length);

            try{
                //receive the DP from the socket:
                RTPsocket.receive(rcvdp);

                //create an RTPpacket object from the DP
                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

                //print important header fields of the RTP packet received:
                System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());

                //print header bitstream:
                rtp_packet.printheader();

                //send datagram packet
                RTPsocket2.send(rcvdp);

            }
            catch (IOException ioe) {
                System.out.println("Exception caught: "+ioe);
            }
        }
    }
}//end of Class Servidor
