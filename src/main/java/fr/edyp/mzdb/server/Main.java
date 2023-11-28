package fr.edyp.mzdb.server;


public class Main {


    public static void main(String args[]) {

            int port = 8090;
            if (args.length >= 1) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException nfe) {
                }
            }

            MzdbServer.processRequests(port);

    }

    public static void interrupt() {
        MzdbServer.interrupt();
    }


}
