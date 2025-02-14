package fr.profi.msdata.serialization;

import fr.profi.thermoreader.model.RunMetaData;

import java.util.List;

public abstract class SerializationCallback {

  /**
   * This methos will be call by ThermoReadController once raw file metadata will be read.
   *
   * @param acqFilePath : file where RunMetaData is read from
   * @param result : list of metadata read ... Only one ?
   * @param success : Did metadata reader task succeed.
   */
  public abstract void run(String acqFilePath, List<RunMetaData> result, boolean success);

}
