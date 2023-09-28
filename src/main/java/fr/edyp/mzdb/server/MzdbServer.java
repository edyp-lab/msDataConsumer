package fr.edyp.mzdb.server;

import fr.profi.mzdb.client.MethodKeys;
import fr.profi.mzdb.serialization.SerializationReader;
import fr.profi.mzdb.serialization.SerializationWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class MzdbServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MzdbServer.class);


    public static void processRequests(int port) {

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            MzdbController mzdbController = new MzdbController();



            System.out.println("Listen to "+port);

            Socket sockClient = serverSocket.accept();

            System.out.println("Client Connected");

            InputStream inputStream = sockClient.getInputStream();
            SerializationReader reader = new SerializationReader(inputStream);

            OutputStream outputStream = sockClient.getOutputStream();
            SerializationWriter writer = new SerializationWriter(outputStream, 1024);

            int cmd = 1;

            while (true) {

                int methodKey = reader.readInt32();

                System.out.println("Cmd : "+cmd+"   "+methodKey);
                cmd++;

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
                    case MethodKeys.METHOD_KEY_ADD_MZDB_METADATA: {
                        response = mzdbController.addMzdbMetaData(reader);
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
                        outputStream.close();
                        System.exit(0);
                    }
                    default: {
                        response = "KO:Unknow Request";
                    }

                }


                writer.writeString(response);
                writer.flush();



            }
        } catch (Exception e) {
            LOGGER.error("Server Error", e);
        }
    }
}
