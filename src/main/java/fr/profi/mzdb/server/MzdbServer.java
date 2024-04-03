package fr.profi.mzdb.server;

import fr.profi.mzdb.client.MethodKeys;
import fr.profi.mzdb.serialization.SerializationReader;
import fr.profi.mzdb.serialization.SerializationWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;


public class MzdbServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MzdbServer.class);


    private static Socket m_sockClient = null;
    private static boolean m_interrupt = false;

    public static void processRequests(int port) {

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            MzdbController mzdbController = new MzdbController();

            String version = "";

            try {
                Properties properties = new Properties();
                properties.load(MzdbController.class.getResourceAsStream("mzdbServerWriter.properties"));
                version = properties.getProperty("mzdbServer.version", "");
                System.out.println("Mzdb Server Writer Version : "+version);

            } catch (Exception e) {
                LOGGER.warn("error in addMzdbMetaData : can not get current version");
            }


            if (m_interrupt) {
                return;
            }

            String message = "Listen to "+port;
            LOGGER.info(message);

            m_sockClient = serverSocket.accept();

            message = "Client Connected";
            LOGGER.info(message);


            InputStream inputStream = m_sockClient.getInputStream();
            SerializationReader reader = new SerializationReader(inputStream);

            OutputStream outputStream = m_sockClient.getOutputStream();
            SerializationWriter writer = new SerializationWriter(outputStream, 1024);

            //int cmd = 1;

            while (true) {

                int methodKey = reader.readInt32();

                //System.out.println("Cmd : "+cmd+"   "+methodKey);
                //cmd++;

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
                        LOGGER.error(response);
                    }

                }


                writer.writeString(response);
                writer.flush();



            }
        } catch (Exception e) {
            LOGGER.error("Server Error", e);

        }
    }

    public static void interrupt() {
        if (m_sockClient == null) {
            return;
        }

        try {
            m_interrupt = true;
            m_sockClient.close();
        } catch (Exception e) {

        }
    }
}
