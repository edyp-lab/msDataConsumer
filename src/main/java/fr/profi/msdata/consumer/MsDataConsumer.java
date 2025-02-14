package fr.profi.msdata.consumer;

import fr.profi.mzdb.client.MethodKeys;
import fr.profi.msdata.serialization.SerializationCallback;
import fr.profi.mzdb.serialization.SerializationReader;
import fr.profi.mzdb.serialization.SerializationWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;


public class MsDataConsumer implements IMsDataConsumer {

    private static MsDataConsumer m_instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(MsDataConsumer.class);

    private ServerSocket m_serverSocket;
    private static Socket m_sockClient = null;
    private static boolean m_interrupt = false;
    private MetaDataReadController rawController;

    private MsDataConsumer(){

    }

    public static MsDataConsumer getInstance(){
        if(m_instance==null)
            m_instance = new MsDataConsumer();
        return m_instance;
    }

    public  synchronized void doInterrupt(){
        m_interrupt = true;
    }

    private synchronized boolean isInterrupted(){
        return m_interrupt;
    }

    public void addCallBack(SerializationCallback c){
        rawController.setCallBack(c);
    }

    public void initialize(int port){
        try {
            m_serverSocket = new ServerSocket(port);
            rawController = new MetaDataReadController();

            try {
                Properties properties = new Properties();
                properties.load(MsDataConsumer.class.getResourceAsStream("msDataConsumer.properties"));
                String version = properties.getProperty("msdataConsumer.version", "");
                LOGGER.info("msData Consumer Version : "+version);

            } catch (Exception e) {
                LOGGER.warn("error in initialize : can not get current version");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error opening server Socket ",e);
        }
    }

    public void processRequests() {
        try {

            if(m_serverSocket == null){
                throw new RuntimeException("msData Consumer has not been initialized. Call initialize first.");
            }

            while (true) { //Until server is interrupted

                if (isInterrupted()) {
                    return; //Sever closed
                }

                String message = "Listen to " + m_serverSocket.getLocalPort();
                LOGGER.info(message);

                //get "next" Socket client
                m_sockClient = m_serverSocket.accept();

                message = "Client Connected";
                LOGGER.info(message);

                InputStream inputStream = m_sockClient.getInputStream();
                SerializationReader reader = new SerializationReader(inputStream);

                OutputStream outputStream = m_sockClient.getOutputStream();
                SerializationWriter writer = new SerializationWriter(outputStream, 1024);

                MzdbController mzdbController = new MzdbController();

                //int cmd = 1;
                LOGGER.info(" Enter Client Wait data loop...  ");
                boolean exitClient = false;
                while (!exitClient) {

                    int methodKey = reader.readInt32();
//
//                    if (isInterrupted()) {
//                        exitServer();
//                    }
                    //System.out.println("Cmd : "+cmd+"   "+methodKey);
                    //cmd++;

                    String response = null;
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
                        case MethodKeys.METHOD_KEY_ADD_ACQ_METADATA: {
                            response = rawController.readRawMetaData(reader);
                            break;
                        }
                        case MethodKeys.METHOD_KEY_FINISHED: {
                            LOGGER.info(" Exit Client  ");
                            outputStream.close();
                            inputStream.close();
                            m_sockClient.close();
                            exitClient = true;
                            break;
                        }
                        default: {
                            response = "KO:Unknow Request "+methodKey;
                            LOGGER.error(response);
                        }
                    }

                    if(response!=null) {
                        writer.writeString(response);
                        writer.flush();
                    }

                } //While processing client

            } // keep server up
        } catch (Exception e) {
            LOGGER.error("Processing - Server Error ", e);
            throw new RuntimeException(e);
        }
    }


    public void interrupt() {
        LOGGER.debug(" --- Stop msData Consumer ");
        doInterrupt(); //notify process before exiting
        try {
            if (m_sockClient != null) {
                m_sockClient.close();
            }
            if(m_serverSocket!=null) {
                m_serverSocket.close();
            }
        } catch (Exception e) {
            LOGGER.error(" Error stoping msData Consumer "+e.getMessage());
        }
    }
}
