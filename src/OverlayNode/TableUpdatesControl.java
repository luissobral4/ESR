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
    private HashMap<Integer,ReentrantLock> tableUpdateLocks;
    private HashMap<Integer,Condition> tableUpdateConds;
    private boolean tableUpdated;

    /**
     * Método construtor
     */
    public TableUpdatesControl(){
        this.fluxTable = new HashMap<>();
        this.tableLock = new ReentrantLock();
        this.tableUpdateLocks = new HashMap<>();
        this.tableUpdateConds = new HashMap<>();
        this.tableUpdated = false;

    }

    public void setFluxTable(HashMap<Integer, ArrayList<Address>> fluxTable) {
        tableLock.lock();
        Set<Integer> s2 = fluxTable.keySet();
        for(Integer i : s2){
            if(!fluxTableContains(i)){
                this.tableUpdateLocks.put(i,new ReentrantLock());
                this.tableUpdateConds.put(i,this.tableUpdateLocks.get(i).newCondition());
            }
        }

        this.fluxTable = (HashMap<Integer, ArrayList<Address>>) fluxTable.clone();
        System.out.println("New Flux Table: "+fluxTable.toString());
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
        return !ret;
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
        for(Integer fluxId : fluxTable.keySet()) {
            this.tableUpdateLocks.get(fluxId).lock();
            this.tableUpdated = true;
            this.tableUpdateConds.get(fluxId).signalAll();
            this.tableUpdateLocks.get(fluxId).unlock();
        }

    }

    public Set<Map.Entry<Integer, ArrayList<Address>>> getFluxTableSet(){
        tableLock.lock();
        Set<Map.Entry<Integer, ArrayList<Address>>> ret = fluxTable.entrySet();
        tableLock.unlock();
        return ret;
    }


    public void waitTableUpdated(int fluxId) throws InterruptedException {
        while(!this.tableUpdated){
            this.tableUpdateLocks.get(fluxId).lock();
            System.out.println("Flux[" + fluxId + "] updates thread waits...");
            this.tableUpdateConds.get(fluxId).await();
            System.out.println("Flux[" + fluxId + "] updates thread resumes!");
            this.tableUpdateLocks.get(fluxId).unlock();
        }
        this.tableUpdated = false;
    }
}
