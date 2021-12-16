package OverlayController;

public class OverlayController {
    public static void main(String[] args) {
        DataAccessControl dac = new DataAccessControl();

        Thread t = new Thread(new NodeListener(dac));
        t.start();

        RequestListener ch = new RequestListener(dac);
        ch.run();
    }

}
