package fr.edyp.mzdb.writer;

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.mzdb.BBSizes;
import fr.profi.mzdb.io.writer.MzDBWriter;
import fr.profi.mzdb.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MzdbWriterApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(MzdbWriterApi.class);

    private MzDBWriter m_writer = null;

    public MzdbWriterApi() {

    }

    public String initializeMzdb(String path, MzDBMetaData mzDbMetaData, AcquisitionMode srcAcqMode) {

        try {

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

    public String addspectrum(Spectrum spectrum, DataEncoding dataEncoding)  throws IOException, SQLiteException {

        try {

            SpectrumHeader spectrumHeader = spectrum.getHeader();

            SpectrumMetaData spectrumMetaData = new SpectrumMetaData(
                    spectrumHeader.getSpectrumId(),
                    spectrumHeader.getParamTreeAsString(null),
                    spectrumHeader.getScanListAsString(null),
                    spectrumHeader.getPrecursorListAsString(null));

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

}
