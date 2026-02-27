package fr.profi.thermoreader.model;

import fr.profi.mzdb.serialization.SerializationReader;

import java.io.IOException;
import java.util.Date;

public class RunMetaData {

  String filePath;
  String acqName;
  String acqDescription;
  String sampleName;
  String operator;
  long durationMin;
  Date acqTime;
  long[] timeRange;
  String methodName;
  AcquisitionErrorCode acqError;

  public void read(SerializationReader reader) throws IOException {
    this.filePath = reader.readString();
    this.acqError = AcquisitionErrorCode.get(reader.readInt32());
    if(acqError.equals(AcquisitionErrorCode.NO_ERROR)) {
      this.acqName = reader.readString();
      this.acqDescription = reader.readString();
      this.sampleName = reader.readString();
      this.operator = reader.readString();
      this.durationMin = reader.readInt64();
      this.timeRange = reader.readArrayInt64();
      this.acqTime = new Date(reader.readInt64());
      this.methodName = reader.readString();
    }
  }
  public String getFilePath() {
    return filePath;
  }

  public String getAcqName() {
    return acqName;
  }

  public String getAcqDescription() {
    return acqDescription;
  }

  public Date getAcqTime() {
    return acqTime;
  }

  public String getSampleName() {
    return sampleName;
  }

  public String getOperator() {
    return operator;
  }

  public long getDurationMin() {
    return durationMin;
  }

  public AcquisitionErrorCode getAcqErrorCode() {
    return acqError;
  }

  @Override
  public String toString() {
    return "Acquisition " + acqName+" - "+ ((acqError != AcquisitionErrorCode.NO_ERROR) ? acqError : acqDescription+" - "+sampleName+" - "+operator+" - "+acqTime);
  }
}
