package fr.profi.thermoreader.model;

import fr.profi.mzdb.serialization.SerializationReader;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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

  public void read(SerializationReader reader) throws IOException {
    this.filePath = reader.readString();
    this.acqName = reader.readString();
    this.acqDescription = reader.readString();
    this.sampleName = reader.readString();
    this.operator = reader.readString();
    this.durationMin = reader.readInt64();
    this.timeRange = reader.readArrayInt64();
    this.acqTime = new Date(reader.readInt64());
    this.methodName=reader.readString();
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

  @Override
  public String toString() {
    return acqName+" - "+acqDescription+" - "+sampleName+" - "+operator+" - "+acqTime;
  }
}
