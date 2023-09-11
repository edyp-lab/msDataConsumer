package fr.edyp.socketwriter.server;

import fr.profi.mzdb.serialization.SerializationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class MzdbServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MzdbServer.class);


    public static void processRequests(int port) {

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            MzdbController mzdbController = new MzdbController();

            while (true) {

                Socket sockClient = serverSocket.accept();

                InputStream inputStream = sockClient.getInputStream();
                SerializationReader reader = new SerializationReader(inputStream);

                PrintWriter outputStream = new PrintWriter(sockClient.getOutputStream());

                int methodKey = reader.readInt32();

                String response;
                switch (methodKey) {
                    case MethodKeys.METHOD_KEY_INITIALIZE_MZDB: {
                        response = mzdbController.initializeMzdb(reader);
                        break;
                    }
                    case MethodKeys.METHOD_KEY_ADD_SPECTRUM: {
                        response = mzdbController.addspectrum(reader);
                        break;
                    }
                    case MethodKeys.METHOD_KEY_CLOSE_MZDB: {
                        response = mzdbController.closedb();
                        break;
                    }
                    case MethodKeys.METHOD_KEY_TEST: {
                        response = mzdbController.test(reader);
                        break;
                    }
                    case MethodKeys.METHOD_KEY_EXIT: {
                        System.exit(0);
                    }
                    default: {
                        response = "KO:Unknow Request";
                    }

                }


                outputStream.print(response);
                outputStream.flush();
                outputStream.close();


            }
        } catch (Exception e) {
            LOGGER.error("Server Error", e);
        }
    }
}
