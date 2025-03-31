package fr.profi.msdata.consumer;

import fr.profi.msdata.serialization.SerializationCallback;
import fr.profi.mzdb.serialization.SerializationReader;
import fr.profi.thermoreader.model.AcquisitionErrorCode;
import fr.profi.thermoreader.model.RunMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This controller is used to read acquisition metadata from specified SerializationReader and
 * send it to specified callback.
 *
 */
public class MetaDataReadController {

  private static final Logger logger = LoggerFactory.getLogger(MetaDataReadController.class);

  private SerializationCallback m_callback;

  public void setCallBack(SerializationCallback callback){
    m_callback =callback;
  }

  public String readRawMetaData(SerializationReader reader){
    try {
      RunMetaData md = new RunMetaData();
      md.read(reader);
      logger.debug(" READ Run MetaData for file : {}  ->  {}", md.getFilePath(), md);
      if(m_callback!=null) {
        List<RunMetaData> result = new ArrayList<>();
        result.add(md);
        if(md.getAcqErrorCode().equals(AcquisitionErrorCode.NO_ERROR))
          m_callback.run(md.getFilePath(), result, true);
        else
          m_callback.run(md.getFilePath(), result, false);
      }

      return "OK";

    } catch (Exception e) {
      logger.error("Error in readRawMetaData", e);
      if(m_callback!=null)
        m_callback.run("", new ArrayList<>(), false);
      return "KO:"+e.getMessage();
    }
  }

}
