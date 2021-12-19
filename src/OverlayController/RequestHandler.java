package OverlayController;

import java.io.DataOutputStream;
import java.net.Socket;

public class RequestHandler implements Runnable {
    DataAccessControl dac;
    Request request;


    public RequestHandler(DataAccessControl dac, Request request) {
        this.dac = dac;
        this.request = request;
    }


    @Override
    public void run() {
        try {
            if (dac.hasFlux(request.getFluxId())){
                dac.updateTable(request); //OverlayController.NodeConnection threads activate
            } else {
                Socket outSocket = new Socket(dac.getFirstNodeIp(), dac.getFirstNodePort());

                DataOutputStream out = new DataOutputStream(outSocket.getOutputStream());

                //Video info
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
