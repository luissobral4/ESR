import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TableUpdatesControl {
    private HashMap<int, HashMap<int, InetAddress>> fluxTable; //Tabela de mapeamento de fluxos
    private ReentrantLock tableLock; //Lock para gerir concorrencias no acesso à tabela
    private ReentrantLock updateLock;
    private Condition tableUpdateCond;
    private boolean tableUpdated;

    /**
     * Método construtor
     */
    public TableUpdatesControl(int nOutputs){
        this.tableLock = new ReentrantLock();
        this.updateLock = new ReentrantLock();
        this.tableUpdateCond = this.updateLock.newCondition();
        this.tableUpdated = false;

    }


    public void tableUpdated() throws InterruptedException {
        while(!this.tableUpdated){
            this.updateLock.lock();
            this.tableUpdateCond.await();
            this.updateLock.unlock();
        }

    }
}
