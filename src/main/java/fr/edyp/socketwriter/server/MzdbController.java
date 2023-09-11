package fr.edyp.socketwriter.server;

import com.almworks.sqlite4java.SQLiteException;
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

    private MzDBWriter m_writer = null;

    public MzdbController() {

    }


    public String initializeMzdb(SerializationReader reader) {

        try {
            String path = reader.readString(); // 1

            MzDBMetaData mzDbMetaData = new MzDBMetaData();
            mzDbMetaData.read(reader);  // 2

            AcquisitionMode srcAcqMode = AcquisitionMode.getEnum(reader); // 3
            boolean isDIA = (srcAcqMode != null && srcAcqMode.equals(fr.profi.mzdb.model.AcquisitionMode.SWATH));

            File destinationFile = new File(path);
            BBSizes defaultBBsize = new BBSizes(5, 10000, 15, 0);

            m_writer = new MzDBWriter(destinationFile, mzDbMetaData, defaultBBsize, isDIA);
        } catch (Exception e) {
            LOGGER.error("error in initializeMzdb", e);
            return "KO:"+e.getMessage();
        }
        return "OK";
    }

    public String addspectrum(SerializationReader reader)  throws IOException, SQLiteException {

        try {
            Spectrum spectrum = new Spectrum(reader);

            SpectrumHeader spectrumHeader = spectrum.getHeader();


            SpectrumMetaData spectrumMetaData = new SpectrumMetaData(
                    spectrumHeader.getSpectrumId(),
                    spectrumHeader.getParamTreeAsString(null),
                    spectrumHeader.getScanListAsString(null),
                    spectrumHeader.getPrecursorListAsString(null));


            DataEncoding dataEncoding = new DataEncoding(reader);

            m_writer.insertSpectrum(spectrum, spectrumMetaData, dataEncoding);
        } catch (Exception e) {
            LOGGER.error("error in addspectrum", e);
            return "KO:"+e.getMessage();
        }

        return "OK";
    }


    public String closedb() {


        if (m_writer != null) {
            m_writer.close();
            return "OK";
        }
        LOGGER.error("error in closedb");
        return "KO";


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
