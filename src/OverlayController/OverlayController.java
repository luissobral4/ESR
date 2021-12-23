package OverlayController;

public class OverlayController {
    public static void main(String[] args) {
        DataAccessControl dac = new DataAccessControl();

        Thread nl = new Thread(new NodeListener(dac));
        nl.start();

        ClientListener ch = new ClientListener(dac);
        ch.run();
    }

}
