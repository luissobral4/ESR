import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FluxHandler implements Runnable {
    DataAccessControl dac;
    Request request;
    Socket socket;


    public FluxHandler(DataAccessControl dac, Request request, Socket socket) {
        this.dac = dac;
        this.request = request;
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            if (dac.tableHasFlux(request.getFluxId())) {
                dac.updateTable(request); //NodeConnection threads activate
            } else {
                Socket outSocket = new Socket(dac.getFirstNodeIp(), dac.getFirstNodePort());

                DataOutputStream out = new DataOutputStream(outSocket.getOutputStream());

                //Video info
                int imagenb = 0;
                VideoStream video = new VideoStream(dac.getFluxFilePath()[request.getFluxId()]); //VideoStream object used to access video frames
                int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
                int VIDEO_LENGTH = 500; //length of the video in frames

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
