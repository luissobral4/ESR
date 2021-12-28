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
    DatagramPacket senddp; //UDP packet containing the video frames (to send)A
    DatagramSocket RTPsocket; //socket to be used to receive UDP packet
    int RTP_dest_port = 25000; //destination port for RTP packets
    InetAddress ClientIPAddr; //Client IP address

    //Video constants:
    //------------------
    //int imagenb = 0; //image nb of the image currently transmitted
    //VideoStream video; //VideoStream object used to access video frames
    //static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
    //static int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
    //static int VIDEO_LENGTH = 500; //length of the video in frames

    //Timer sTimer; //timer used to send the images at the video frame rate
    byte[] sBuf; //buffer used to store the images to send to the client

    //--------------------------
    //Constructor
    //--------------------------
    public OTTVideo(String ip) {
        //init para a parte do cliente
        //--------------------------
        //sTimer = new Timer(20, new clientTimerListener());
        //sTimer.setInitialDelay(0);
        //sTimer.setCoalesce(true);
        sBuf = new byte[15000]; //allocate enough memory for the buffer used to receive data from the server

        try {
            RTPsocket = new DatagramSocket(RTP_dest_port); //init RTP socket
            ClientIPAddr = InetAddress.getByName(ip);

        } catch (SocketException e) {
            System.out.println("Servidor: erro no socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Servidor: erro no video: " + e.getMessage());
        }



        //GUI:
        label = new JLabel("Send frame #        ", JLabel.CENTER);
        getContentPane().add(label, BorderLayout.CENTER);

        //sTimer.start();

    }

    //------------------------------------
    //main
    //------------------------------------
    public void run()
    {
      int seq = 0;
      while(seq != 500){
        //Construct a DatagramPacket to receive data from the UDP socket
        rcvdp = new DatagramPacket(sBuf, sBuf.length);

        try{
            //receive the DP from the socket:
            RTPsocket.receive(rcvdp);

            //create an RTPpacket object from the DP
            RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

            seq = rtp_packet.getsequencenumber();

            //print important header fields of the RTP packet received:
            System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());

            //print header bitstream:
            rtp_packet.printheader();

            //get to total length of the full rtp packet to send
            int packet_length = rtp_packet.getlength();

            //retrieve the packet bitstream and store it in an array of bytes
            byte[] packet_bits = new byte[packet_length];
            rtp_packet.getpacket(packet_bits);

            //send the packet as a DatagramPacket over the UDP socket
            senddp = new DatagramPacket(packet_bits, packet_length, ClientIPAddr, RTP_dest_port);

            //send datagram packet
            RTPsocket.send(senddp);

        }
        catch (IOException ioe) {
            System.out.println("Exception caught: "+ioe);
        }

      }

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
                RTPsocket.send(rcvdp);

            }
            catch (IOException ioe) {
                System.out.println("Exception caught: "+ioe);
            }
        }
    }
}//end of Class Servidor
