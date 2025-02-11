package fr.profi.mzdb.server;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import fr.profi.mzdb.serialization.SerializationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class MzdbServerMain {

 public static class ServerParameters {
   @Parameter(names = {"-p", "--port"}, description = "Port to listen on. Client should use same port. ")
   public Integer port = 8090;

   @Parameter(names = {"-t", "--threaded"}, description = "Use Threaded implementation to allow multiples clients connections.")
   public Boolean threaded = false;

   @Parameter(names = {"-h", "--help"}, help = true)
   public boolean help;

 }

  private static final Logger LOGGER = LoggerFactory.getLogger(MzdbServerMain.class);

  private static MzdbServerMain m_instance;
  private IMzdbServer server;

  private boolean isInitialized = false;
  private MzdbServerMain(){

  }

 public static MzdbServerMain getInstance(){
   if(m_instance == null)
     m_instance = new MzdbServerMain();

   return m_instance;
 }


  public void initServer(String[] args){
    LOGGER.info("... Initialize Mzdb Server  ");
    ServerParameters parameters = new ServerParameters();
    JCommander cmd = JCommander.newBuilder().addObject(parameters).build();
    cmd.parse(args);
    if(parameters.help) {
      cmd.usage();
      System.exit(0);
    }

    if(parameters.threaded)
      server =  MzdbThreadedServer.getInstance();
    else
      server =  MzdbServer.getInstance();
    server.initialize(parameters.port);

    isInitialized = true;
  }

  public void start(){
    Thread.setDefaultUncaughtExceptionHandler((t1, e) -> {
      System.out.println("An exception Caught in Thread "+ t1.getName()+" exception = " + e);
      MzdbServerMain.getInstance().interrupt(true);
    });

    LOGGER.info("... Start Mzdb Server...  ");
    if(!isInitialized)
      throw new RuntimeException("MzdbServerMain is not initialized ! Call initServer first");
    Thread t = new Thread(() -> server.processRequests(), "MzdbServerImplem-Thread");
    t.setDaemon(true);
    t.start();

  }

  /**
   * Initialize the Mzdb Server Main and start it to listen for client
   * @param args parameter to used for server initialization
   */
    public static void main(String[] args) {

      System.out.println("MzdbServer Main ... initializing ");
      MzdbServerMain.getInstance().initServer(args);
      MzdbServerMain.getInstance().start();

      LOGGER.info("\n -------------------------------------------------------------- \n");
      LOGGER.info("\n ------------------ Enter \"exit\" to stop server ------------- \n");
      LOGGER.info("\n -------------------------------------------------------------- \n");

      while (true) {
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.nextLine();

        if (userInput.equals("exit")) {
          MzdbServerMain.getInstance().interrupt(true);
        }
      }
    }

    public void addCallBack(SerializationCallback callback){
      server.addCallBack(callback);
    }

    public void interrupt(boolean doSysExit) {
        LOGGER.info("... Stop Mzdb Server...  ");
        server.interrupt();
        if(doSysExit)
          System.exit(0);
    }

}
