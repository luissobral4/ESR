import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class OverlayNode{

    public static void runFluxes(TableUpdatesControl tableUpdtCtrl) throws IOException {
        for(Map.Entry<int, HashMap<int, String>> ent : tableUpdtCtrl.getFluxTableSet()){
            Thread flux = new Thread(new FluxConnection(ent.getKey(),ent.getValue().size(),tableUpdtCtrl));
            flux.start();
        }
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(args[0],Integer.parseInt(args[1]));;
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            TableUpdatesControl tableUpdtCtrl = new TableUpdatesControl();
            byte[] greeting = new byte[1];

            out.write(greeting);

            HashMap<int, HashMap<int,String>> read = (HashMap<int,HashMap<int,String>>) in.readObject();
            tableUpdtCtrl.setFluxTable(read);
            runFluxes(tableUpdtCtrl);

            while(!read.get(0).get(0).equals("0.0.0.0")) {
                read = (HashMap<int,HashMap<int,String>>) in.readObject();
                tableUpdtCtrl.signalTableUpdate();
                tableUpdtCtrl.setFluxTable(read);

            }
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}
