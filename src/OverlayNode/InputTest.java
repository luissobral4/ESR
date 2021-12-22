package OverlayNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class InputTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket s = new Socket("127.0.0.1",4242);
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        int i = 0;
        while(i < 5){
            byte[] barr = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
            barr[0] = (byte) i;
            dos.write(barr);
            dos.flush();

            System.out.println("Wrote: " + Arrays.toString(barr));
            Thread.sleep(20000);
            i++;
        }
        byte[] last = {0};
        dos.write(last);
        dos.flush();
    }
}
