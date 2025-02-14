package fr.profi.msdata.consumer;

import fr.profi.msdata.serialization.SerializationCallback;

/**
 * Interface for any MsDataConsumer implementation.
 */
public interface IMsDataConsumer {

  void initialize(int port);

  void addCallBack(SerializationCallback c);

  void processRequests();

  void interrupt();

}
