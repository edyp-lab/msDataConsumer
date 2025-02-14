package fr.profi.msdata.consumer;

import fr.profi.mzdb.client.MzdbWriterApi;
import fr.profi.mzdb.db.model.Software;
import fr.profi.mzdb.db.model.params.ParamTree;
import fr.profi.mzdb.model.AcquisitionMode;
import fr.profi.mzdb.model.MzDBMetaData;
import fr.profi.mzdb.serialization.SerializationReader;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import fr.profi.mzdb.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is used to create and fill mzdb file using specified SerializationReader
 *
 */
public class MzdbController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MzdbController.class);

    private MzdbWriterApi m_mzdbWriterApi = null;

    public MzdbController() {

    }


    public String initializeMzdb(SerializationReader reader) {

        m_mzdbWriterApi = new MzdbWriterApi();

        try {
            String path = reader.readString(); // 1

            //MzDBMetaData mzDbMetaData = new MzDBMetaData();
            //mzDbMetaData.read(reader);  // 2

            AcquisitionMode srcAcqMode = AcquisitionMode.getEnum(reader); // 2

            return m_mzdbWriterApi.initializeMzdb(path, srcAcqMode);

        } catch (Exception e) {
            LOGGER.error("error in initializeMzdb", e);
            return "KO:"+e.getMessage();
        }
    }

    public String addMzdbMetaData(SerializationReader reader) {


        try {
            MzDBMetaData mzDbMetaData = new MzDBMetaData();
            mzDbMetaData.read(reader);

            String version = "";

            try {
                Properties properties = new Properties();
                properties.load(MzdbController.class.getResourceAsStream("msDataConsumer.properties"));
                version = properties.getProperty("msdataConsumer.version", "");
                System.out.println("msData Consumer Version : "+version);

            } catch (Exception e) {
                LOGGER.warn("error in addMzdbMetaData : can not get current version");
            }

            List<Software> softwares = mzDbMetaData.getSoftwares();
            int id = softwares.size()+1;
            Software software = new Software(id, "MzdbServerWriter", version, new ParamTree());
            mzDbMetaData.getSoftwares().add(software);

            return m_mzdbWriterApi.addMzdbMetaData(mzDbMetaData);

        } catch (Exception e) {
            LOGGER.error("error in addMzdbMetaData", e);
            return "KO:"+e.getMessage();
        }
    }

    /*Throw exception if an error occurs */
    public String addspectrum(SerializationReader reader)   {

        long id = -1;
        String result;
        try {
            Spectrum spectrum = new Spectrum(reader);
            id = spectrum.getHeader().getSpectrumId();
            DataEncoding dataEncoding = new DataEncoding(reader);

            result = m_mzdbWriterApi.addspectrum(spectrum, dataEncoding);
            reader.resetStream(); // must be done at the end : avoid the stream to exceed max size when adding multiple spectrum

        } catch (Exception e) {
           LOGGER.error("error in add spectrum {} ",id, e);
           throw new RuntimeException( /*return*/ "KO:"+e.getMessage());
        }

        if(!result.equals("OK"))
            throw new RuntimeException( /*return*/ "KO from m_mzdbWriterApi, spectrum (id) : " +result);

        return "OK";
    }



    public String closedb() {
        return m_mzdbWriterApi.closedb();

    }



    public String test(SerializationReader reader) {

        try {
            String r = reader.readString();
            int value = reader.readInt32();


            return "OK:"+r+value;
        } catch (IOException e) {
            LOGGER.error("error in test", e);
            return "KO:"+e.getMessage();
        }


    }
}
