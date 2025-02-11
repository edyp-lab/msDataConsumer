package fr.profi.mzdb.server;

import fr.profi.mzdb.client.MethodKeys;
import fr.profi.mzdb.serialization.SerializationCallback;
import fr.profi.mzdb.serialization.SerializationReader;
import fr.profi.mzdb.serialization.SerializationWriter;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * IMzdbServer implementation using a Multithreading, allowing multiple clients to connect at the same time
 * !!! WARNING !!! This implementation should not be used to write mzdb files => Thread issue in mzdbWriter
 */
public class MzdbThreadedServer implements IMzdbServer{

    private static MzdbThreadedServer m_instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(MzdbThreadedServer.class);

    private static boolean m_interrupt = false;
    private ServerSocket m_serverSocket;
    private SerializationCallback callback;

    private final List<SocketClientRunnable> runningThread;
    private MzdbThreadedServer(){
        LOGGER.warn("  !!! WARNING !!! This implementation should not be used to write mzdb files => Thread issue in mzdbWriter. ");
        runningThread = new ArrayList<>();
    }

    public static MzdbThreadedServer getInstance(){
        if(m_instance==null)
            m_instance = new MzdbThreadedServer();
        return m_instance;
    }


    public void addCallBack(SerializationCallback c){
        callback = c;
        addRunnableCallback(callback);
    }

    public void initialize(int port){
        try {
            m_serverSocket = new ServerSocket(port);

            try {
                Properties properties = new Properties();
                properties.load(MzdbController.class.getResourceAsStream("mzdbServerWriter.properties"));
                String version = properties.getProperty("mzdbServer.version", "");
                System.out.println("Mzdb Server Writer Version : "+version);

            } catch (Exception e) {
                LOGGER.warn("error in addMzdbMetaData : can not get current version");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error opening server Socket ",e);
        }
    }

    public void processRequests() {
        try {

            if(m_serverSocket == null){
                throw new RuntimeException("Mzdb Server has not been initialized. Call initialize first.");
            }


            String message = "Listen to "+m_serverSocket.getLocalPort();
            LOGGER.info(message);
            int nbClient =1;

            while (true){ // Launch a thread per client. Until server is interrupted

               if (isInterrupted()) {
                    return;
                }

                Socket sockClient = m_serverSocket.accept();

                message = "Client "+(nbClient++) +" Connected. Running "+(getRunnableSize()+1);
                LOGGER.info(message);

                ThermoReadController readController = new ThermoReadController();
                readController.setCallBack(callback);
                SocketClientRunnable runnable  = new SocketClientRunnable(sockClient, readController);
                addRunnable(runnable);
                new Thread(runnable).start();
            }

        } catch (Exception e) {
            LOGGER.error("Server Error", e);
        }
    }

    protected synchronized boolean isInterrupted(){
        return m_interrupt;
    }

    public  synchronized void doInterrupt(){
        m_interrupt = true;
    }

    protected synchronized void addRunnable(SocketClientRunnable runnable){
        if(m_interrupt && runningThread.isEmpty())
            interrupt();
        runningThread.add(runnable);
    }

    protected synchronized void removeRunnable(SocketClientRunnable runnable){
        runningThread.remove(runnable);
        if(m_interrupt && runningThread.isEmpty())
            interrupt();
    }

    protected synchronized boolean isRunnableEmpty(){
        return runningThread.isEmpty();
    }
    protected synchronized int getRunnableSize(){
        return runningThread.size();
    }
    protected synchronized void addRunnableCallback(SerializationCallback c){
         runningThread.forEach(r -> {
             r.m_rawController.setCallBack(c);
         });
    }

    public void interrupt() {
        LOGGER.info("Stop MzdbThreadedServer  ");
        doInterrupt();
        closeServer();
    }
    private void closeServer(){
        //Do not wait clients have finished... close all
        if (!isRunnableEmpty()) {
            runningThread.forEach(SocketClientRunnable::stopRunning);
        }
        if(m_serverSocket != null) {
            try {
                m_serverSocket.close();
            } catch (IOException ignore) {
               ignore.printStackTrace();
            }
        }
    }


    class SocketClientRunnable implements Runnable{

        private final Socket m_sockClient;

        private boolean isRunning = true;

        private final ThermoReadController m_rawController;
        public SocketClientRunnable(Socket sockClient,ThermoReadController rawController){
            m_sockClient = sockClient;
            m_rawController = rawController;
        }

        private synchronized void interrupt(){
            isRunning = false;
        }

        private synchronized boolean keepRunning() {
            return isRunning;
        }

        private void stopRunning(){
            try {
                m_sockClient.close();
                removeRunnable(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            CircularFifoQueue<String> messages = new CircularFifoQueue<>(20);
            try {

                //Controller specific to client
                //Should not use Threads for MzdbController !
                MzdbController mzdbController = new MzdbController();

                InputStream inputStream = m_sockClient.getInputStream();
                SerializationReader reader = new SerializationReader(inputStream);

                OutputStream outputStream = m_sockClient.getOutputStream();
                SerializationWriter writer = new SerializationWriter(outputStream, 1024);


                int cmd = 1;
                LOGGER.info(" Enter Client {} Wait data loop...  ",m_sockClient.getLocalPort());
                while (keepRunning()) {

                    messages.add("Thread "+Thread.currentThread().getId()+" - command "+cmd+" - client "+ m_sockClient);
                    int methodKey = reader.readInt32();

                    //System.out.println("Cmd : "+cmd+"   "+methodKey);
                    cmd++;

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
                            response = m_rawController.readRawMetaData(reader);
                            break;
                        }
                        case MethodKeys.METHOD_KEY_FINISHED: {
                            LOGGER.info(" Exit Client {} ", m_sockClient.getLocalPort());
                            outputStream.close();
                            interrupt(); // exit
                            break;
                        }
                        default: {
                            response = "KO:Unknow Request "+methodKey;
                            LOGGER.error(response);
                        }
                    }

                    messages.add("END Thread "+Thread.currentThread().getId()+" - command "+cmd+" - client "+ m_sockClient);
                    if(response!=null) {
                        writer.writeString(response);
                        writer.flush();
                    }
                }

                stopRunning();
            } catch (Exception e ){
                LOGGER.error("ERROR in Thread {} - client {}", Thread.currentThread().getId(), m_sockClient);
                e.printStackTrace();
                messages.forEach(LOGGER::info);
                throw new RuntimeException(e);
            }

        }
    }
}
