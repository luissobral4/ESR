package OverlayController;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class NodeHandler implements Runnable{
    int nodeId;
    DataAccessControl dac;
    DataOutputStream out;


    public NodeHandler(int nodeId, DataAccessControl dac, DataOutputStream out) throws IOException {
        this.nodeId = nodeId;
        this.dac = dac;
        this.out = out;
    }


    @Override
    public void run() {
        try {
            byte[] currByteArr = dac.getByteArrayTable(nodeId);
            out.write(currByteArr);
            out.flush();
            while(dac.isAlive()){
                dac.waitTableUpdate();
                if (!Arrays.equals(currByteArr, dac.getByteArrayTable(nodeId))) {
                    currByteArr = dac.getByteArrayTable(nodeId);
                    out.write(currByteArr);
                    out.flush();
                }
            }
        }catch (IOException | InterruptedException e) {
                e.printStackTrace();
                dac.nodeRemove(nodeId);
        }
    }
}
