import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TableUpdatesControl {
    private HashMap<int, HashMap<int, String>> fluxTable; //Tabela de mapeamento de fluxos
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

    public void setFluxTable(HashMap<int, HashMap<int, String>> fluxTable) {
        this.fluxTable = (HashMap<int, HashMap<int, String>>) fluxTable.clone();
    }

    public HashMap<int, String> getFluxTableCopy(int fluxID) {
        return (HashMap<int, String>) fluxTable.get(fluxID).clone();
    }

    public boolean hasUpdated(HashMap<int, String> tableAux, int fluxID){
        return tableAux.equals(fluxTable.get(fluxID));
    }

    public void tableRemove(int fluxID){
        tableLock.lock();
        fluxTable.remove(fluxID);
        tableLock.unlock();
    }

    public boolean fluxTableContains(int fluxID) {
        return fluxTable.containsKey(fluxID);
    }

    public Set<Map.Entry<int, String>> getFluxTableEntrySet(int fluxID) {
        return fluxTable.get(fluxID).entrySet();
    }

    public Set<Map.Entry<int, HashMap<int, String>>> getFluxTableSet() {
        return fluxTable.entrySet();
    }

    public void signalTableUpdate(){
        this.tableUpdated = true;
        this.tableUpdateCond.signalAll();
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
