package OverlayNode;


import Util.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TableUpdatesControl {
    private HashMap<Integer, ArrayList<Address>> fluxTable; //Tabela de mapeamento de fluxos
    private ReentrantLock tableLock; //Lock para gerir concorrencias no acesso à tabela
    private ReentrantLock updateLock;
    private Condition tableUpdateCond;
    private boolean tableUpdated;

    /**
     * Método construtor
     */
    public TableUpdatesControl(){
        this.tableLock = new ReentrantLock();
        this.updateLock = new ReentrantLock();
        this.tableUpdateCond = this.updateLock.newCondition();
        this.tableUpdated = false;

    }

    public void setFluxTable(HashMap<Integer, ArrayList<Address>> fluxTable) {
        tableLock.lock();
        this.fluxTable = (HashMap<Integer, ArrayList<Address>>) fluxTable.clone();
        tableLock.unlock();
    }

    public ArrayList<Address> getFluxArrayCopy(int fluxID) {
        tableLock.lock();
        ArrayList<Address> ret = (ArrayList<Address>) fluxTable.get(fluxID).clone();
        tableLock.unlock();
        return ret;
    }

    public ArrayList<Address> getFluxArray(int fluxID) {
        tableLock.lock();
        ArrayList<Address> ret = fluxTable.get(fluxID);
        tableLock.unlock();
        return ret;
    }

    public boolean hasUpdated(ArrayList<Address> tableAux, int fluxID){
        tableLock.lock();
        boolean ret = tableAux.equals(fluxTable.get(fluxID));
        tableLock.unlock();
        return ret;
    }

    public void tableRemove(int fluxID){
        tableLock.lock();
        fluxTable.remove(fluxID);
        tableLock.unlock();
    }

    public HashMap<Integer, ArrayList<Address>> getFluxTable() {
        return fluxTable;
    }

    public boolean fluxTableContains(int fluxID) {
        tableLock.lock();
        boolean ret = fluxTable.containsKey(fluxID);
        tableLock.unlock();
        return ret;
    }

    public void signalTableUpdate(){
        this.tableUpdated = true;
        this.tableUpdateCond.signalAll();
    }

    public Set<Map.Entry<Integer, ArrayList<Address>>> getFluxTableSet(){
        tableLock.lock();
        Set<Map.Entry<Integer, ArrayList<Address>>> ret = fluxTable.entrySet();
        tableLock.unlock();
        return ret;
    }


    public void waitTableUpdated() throws InterruptedException {
        while(!this.tableUpdated){
            this.updateLock.lock();
            this.tableUpdateCond.await();
            this.updateLock.unlock();
        }
        this.tableUpdated = false;
    }
}
