package OverlayController;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class RequestHandler implements Runnable {
    private DataAccessControl dac;
    private Request request;
    private int clientId;


    public RequestHandler(DataAccessControl dac, Request request, int clientId) {
        this.dac = dac;
        this.request = request;
        this.clientId = clientId;
    }


    @Override
    public void run() {
        try {
            dac.waitAllUpdatedClient(clientId);
            if(!dac.hasFlux(request.getFluxId())){
                System.out.println("Request handler: Initializing flux "+request.getFluxId()+"\nFirst step to: "+dac.getFirstNodeIp()+":"+dac.getFirstNodePort());

                dac.updateTable(request);

                Socket outSocket = null;
                DataOutputStream out = null;
                boolean notConnected = true;

                while(notConnected) {
                    try {
                        outSocket = new Socket(dac.getFirstNodeIp(), dac.getFirstNodePort()+request.getFluxId());
                        out = new DataOutputStream(outSocket.getOutputStream());
                        notConnected = false;
                    } catch (IOException e) {
                        System.out.println("Flux " + request.getFluxId() + ": First node connection falied waiting and trying again!");
                        try {
                            Thread.sleep(2000);//2 seconds
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }


                int i = 0;

                while(i<5){
                    byte[] sBuf = {(byte)i,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};

                    out.write(sBuf);
                    out.flush();

                    System.out.println("Flux "+request.getFluxId()+" sent: "+Arrays.toString(sBuf));
                    i++;
                    Thread.sleep(10000);
                }
                byte[] sBuf = {0};

                out.write(sBuf);
                out.flush();

                /*Video info
                int imagenb = 0;
                VideoStream video = new VideoStream(dac.getFluxFilePath(request.getFluxId())); //OverlayController.VideoStream object used to access video frames
                int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
                int VIDEO_LENGTH = 500; //length of the video in frames

                dac.updateTable(request);

                while(imagenb < VIDEO_LENGTH){
                    byte[] sBuf = new byte[16384];

                    video.getnextframe(sBuf);
                    out.write(sBuf);
                    out.flush();

                    imagenb++;
                }*/
            }else dac.updateTable(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
