import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NodeConnection implements Runnable{
    int nodeId;
    DataAccessControl dac;
    DataOutputStream out;


    public NodeConnection(int nodeId, DataAccessControl dac, DataOutputStream out) throws IOException {
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
                if (!currByteArr.equals(dac.getByteArrayTable(nodeId))) {
                    out.write(currByteArr);
                    out.flush();
                }
            }
        }catch (IOException e) {
                e.printStackTrace();
        }
    }
}
