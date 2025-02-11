package fr.profi.mzdb.server;

import fr.profi.mzdb.serialization.SerializationCallback;

/**
 * Interface for any MzdbServer implementation.
 */
public interface IMzdbServer {

  void initialize(int port);

  void addCallBack(SerializationCallback c);

  void processRequests();

  void interrupt();

}
