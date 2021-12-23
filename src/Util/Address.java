package Util;

import java.io.Serializable;
import java.util.Objects;

public class Address implements Serializable {
    private String ip;
    private int port;

    public Address(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Address{" + ip + ':'+ port + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return port == address.port &&
                Objects.equals(ip, address.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
