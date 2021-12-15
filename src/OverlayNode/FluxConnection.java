package OverlayNode;

import Util.Address;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluxConnection implements Runnable {
    private boolean debug = false;
    private int fluxID; //Id do fluxo que esta thread trata
    private FluxControl fluxCtrl; //Objeto de controlo do fluxo
    private TableUpdatesControl tableUpdtCtrl; //Objeto de controlo dos updates da tabela de fluxos

    public FluxConnection(int fluxID,
                          int nOutputs,
                          TableUpdatesControl tableUpdtCtrl) throws IOException {
        this.fluxID = fluxID;
        this.fluxCtrl = new FluxControl(nOutputs);
        this.tableUpdtCtrl = tableUpdtCtrl;
    }


    @Override
    public void run() {
        try {
            AtomicBoolean runningIn = new AtomicBoolean(true);
            Thread inThread = new Thread(new FluxConnectionInput(fluxID,tableUpdtCtrl,runningIn));
            inThread.start();

            ArrayList<AtomicBoolean> threadList = new ArrayList<>();
            for (Address adr : tableUpdtCtrl.getFluxArray(fluxID)) {
                 AtomicBoolean runningOut = new AtomicBoolean(true);
                 Thread outThread = new Thread(new FluxConnectionOutput(fluxCtrl, adr, debug, fluxID,runningOut));
                 outThread.start();
                 threadList.add(runningOut);
            }
            while(tableUpdtCtrl.fluxTableContains(fluxID)){
                ArrayList<Address> tableAux = tableUpdtCtrl.getFluxArrayCopy(fluxID);
                tableUpdtCtrl.waitTableUpdated();

                if (tableUpdtCtrl.hasUpdated(tableAux, fluxID)) {
                    runningIn.set(false);
                    for (AtomicBoolean b : threadList) {
                        b.set(false);
                    }
                    runningIn = new AtomicBoolean(true);
                    inThread = new Thread(new FluxConnectionInput(fluxID, tableUpdtCtrl, runningIn));
                    inThread.start();
                    threadList = new ArrayList<>();
                    for (Address adr : tableUpdtCtrl.getFluxArray(fluxID)) {
                        AtomicBoolean runningOut = new AtomicBoolean(true);
                        Thread outThread = new Thread(new FluxConnectionOutput(fluxCtrl, adr, debug, fluxID, runningOut));
                        outThread.start();
                        threadList.add(runningOut);
                    }
                }
            }
        }catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}

