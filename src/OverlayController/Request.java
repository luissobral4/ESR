package OverlayController;

import Util.Address;

import java.util.Objects;

public class Request {
    private int fluxId;
    private Address adr;

    public Request(int fluxId, String ip, int port){
        this.fluxId = fluxId;
        this.adr = new Address(ip,port);
    }


    public int getFluxId() {
        return fluxId;
    }

    public String getIp() {
        return adr.getIp();
    }

    public int getPort() {
        return adr.getPort();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return fluxId == request.fluxId &&
                adr.getPort() == request.getPort() &&
                Objects.equals(adr.getIp(), request.getIp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluxId, adr.getIp(), adr.getPort());
    }
}
