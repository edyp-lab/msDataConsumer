package fr.edyp.mzdb.server;

import com.almworks.sqlite4java.SQLiteException;
import fr.edyp.mzdb.writer.MzdbWriterApi;
import fr.profi.mzdb.BBSizes;
import fr.profi.mzdb.io.writer.MzDBWriter;
import fr.profi.mzdb.model.AcquisitionMode;
import fr.profi.mzdb.model.MzDBMetaData;
import fr.profi.mzdb.serialization.SerializationReader;

import java.io.File;
import java.io.IOException;
import fr.profi.mzdb.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MzdbController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MzdbController.class);

    private MzdbWriterApi m_mzdbWriterApi = null;

    public MzdbController() {

    }


    public String initializeMzdb(SerializationReader reader) {

        m_mzdbWriterApi = new MzdbWriterApi();

        try {
            String path = reader.readString(); // 1

            MzDBMetaData mzDbMetaData = new MzDBMetaData();
            mzDbMetaData.read(reader);  // 2

            AcquisitionMode srcAcqMode = AcquisitionMode.getEnum(reader); // 3

            return m_mzdbWriterApi.initializeMzdb(path, mzDbMetaData, srcAcqMode);

        } catch (Exception e) {
            LOGGER.error("error in initializeMzdb", e);
            return "KO:"+e.getMessage();
        }
    }

    public String addspectrum(SerializationReader reader)  throws IOException, SQLiteException {

        try {
            Spectrum spectrum = new Spectrum(reader);
            DataEncoding dataEncoding = new DataEncoding(reader);

            m_mzdbWriterApi.addspectrum(spectrum, dataEncoding);

        } catch (Exception e) {
            LOGGER.error("error in addspectrum", e);
            return "KO:"+e.getMessage();
        }

        return "OK";
    }


    public String closedb() {
        return m_mzdbWriterApi.closedb();

    }



    public String test(SerializationReader reader)  throws IOException, SQLiteException {

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
