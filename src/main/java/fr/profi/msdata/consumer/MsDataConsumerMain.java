package fr.profi.msdata.consumer;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import fr.profi.msdata.serialization.SerializationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class MsDataConsumerMain {

 public static class ServerParameters {
   @Parameter(names = {"-p", "--port"}, description = "Port to listen on. Client should use same port. ")
   public Integer port = 8090;

   @Parameter(names = {"-t", "--threaded"}, description = "Use Threaded implementation to allow multiples clients connections.")
   public Boolean threaded = false;

   @Parameter(names = {"-h", "--help"}, help = true)
   public boolean help;

 }

  private static final Logger LOGGER = LoggerFactory.getLogger(MsDataConsumerMain.class);

  private static MsDataConsumerMain m_instance;
  private IMsDataConsumer server;

  private boolean isInitialized = false;
  private MsDataConsumerMain(){

  }

 public static MsDataConsumerMain getInstance(){
   if(m_instance == null)
     m_instance = new MsDataConsumerMain();

   return m_instance;
 }


  public void initServer(String[] args){
    LOGGER.info("... Initialize msData Consumer  ");
    ServerParameters parameters = new ServerParameters();
    JCommander cmd = JCommander.newBuilder().addObject(parameters).build();
    cmd.parse(args);
    if(parameters.help) {
      cmd.usage();
      System.exit(0);
    }

    if(parameters.threaded)
      server =  MsDataThreadedConsumer.getInstance();
    else
      server =  MsDataConsumer.getInstance();
    server.initialize(parameters.port);

    isInitialized = true;
  }

  public void start(){
    Thread.setDefaultUncaughtExceptionHandler((t1, e) -> {
      System.out.println("An exception Caught in Thread "+ t1.getName()+" exception = " + e);
      MsDataConsumerMain.getInstance().interrupt(true);
    });

    LOGGER.info("... Start msData Consumer...  ");
    if(!isInitialized)
      throw new RuntimeException("MsDataConsumerMain is not initialized ! Call initServer first");
    Thread t = new Thread(() -> server.processRequests(), "MsDatConsumer-Thread");
    t.setDaemon(true);
    t.start();

  }

  /**
   * Initialize the msData Consumer Main and start it to listen for client
   * @param args parameter to used for server initialization
   */
    public static void main(String[] args) {

      System.out.println("msData Consumer ... initializing ");
      MsDataConsumerMain.getInstance().initServer(args);
      MsDataConsumerMain.getInstance().start();

      LOGGER.info("\n -------------------------------------------------------------- \n");
      LOGGER.info("\n ------------------ Enter \"exit\" to stop server ------------- \n");
      LOGGER.info("\n -------------------------------------------------------------- \n");

      while (true) {
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.nextLine();

        if (userInput.equals("exit")) {
          MsDataConsumerMain.getInstance().interrupt(true);
        }
      }
    }

    public void addCallBack(SerializationCallback callback){
      server.addCallBack(callback);
    }

    public void interrupt(boolean doSysExit) {
        LOGGER.info("... Stop msData Consumer...  ");
        server.interrupt();
        if(doSysExit)
          System.exit(0);
    }

}
