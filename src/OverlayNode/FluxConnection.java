package OverlayNode;

import Util.Address;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluxConnection implements Runnable {
    private boolean debug = true;
    private int fluxID; //Id do fluxo que esta thread trata
    private FluxControl fluxCtrl; //Objeto de controlo do fluxo
    private TableUpdatesControl tableUpdtCtrl; //Objeto de controlo dos updates da tabela de fluxos

    public FluxConnection(int fluxID,
                          int nOutputs,
                          TableUpdatesControl tableUpdtCtrl) throws IOException {
        this.fluxID = fluxID;
        this.fluxCtrl = new FluxControl(debug,nOutputs,fluxID);
        this.tableUpdtCtrl = tableUpdtCtrl;
    }


    @Override
    public void run() {
        try {
            AtomicBoolean runningIn = new AtomicBoolean(true);
            Thread inThread = new Thread(new FluxConnectionInput(fluxID,tableUpdtCtrl,fluxCtrl,runningIn));
            inThread.start();

            HashMap<Address,AtomicBoolean> threadBools = new HashMap<>();
            int outId = 0;

            HashMap<Address,Integer> currOutputs = new HashMap<>();
            for (Address adr : tableUpdtCtrl.getFluxArray(fluxID)) {
                 AtomicBoolean runningOut = new AtomicBoolean(true);
                 Thread outThread = new Thread(new FluxConnectionOutput(fluxCtrl, adr, debug, fluxID,runningOut,outId));
                 outThread.start();
                 threadBools.put(adr,runningOut);
                 currOutputs.put(adr,outId);
                 outId++;
            }
            while(tableUpdtCtrl.fluxTableContains(fluxID)){
                ArrayList<Address> tableAux = tableUpdtCtrl.getFluxArrayCopy(fluxID);
                tableUpdtCtrl.waitTableUpdated(fluxID);


                ArrayList<Address> fArr = tableUpdtCtrl.getFluxArrayCopy(fluxID);
                ArrayList<Address> removals = new ArrayList<>();
                ArrayList<Address> aditions = new ArrayList<>();
                for(Address a : currOutputs.keySet()) {
                    if(!fArr.contains(a)){
                        removals.add(a);
                    }
                    else fArr.remove(a);
                }
                for(Address a : fArr){
                    if(!currOutputs.containsKey(a)){
                        aditions.add(a);
                    }
                }


                if(tableUpdtCtrl.hasUpdated(tableAux, fluxID)){
                    for (Address a : removals) {
                        threadBools.get(a).set(false);
                    }
                    for (Address adr : aditions) {
                        AtomicBoolean runningOut = new AtomicBoolean(true);
                        Thread outThread = new Thread(new FluxConnectionOutput(fluxCtrl, adr, debug, fluxID, runningOut,outId));
                        outThread.start();
                        threadBools.put(adr, runningOut);
                        outId++;
                    }

                }
            }
        }catch (InterruptedException | IOException e) {
            e.printStackTrace();
            System.out.println("EXCEPTION FLUXCONN");
        }
    }
}

