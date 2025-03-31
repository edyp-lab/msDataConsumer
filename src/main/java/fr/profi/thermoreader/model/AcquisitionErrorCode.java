package fr.profi.thermoreader.model;

public enum AcquisitionErrorCode {
  
    NO_ERROR(0),
    IN_ACQUISITION(1),
    NO_SCANS(2),
    FILE_NOT_FOUND(3),
    READ_ERROR(4),
    UNKNOWN_ERROR(10);

    private final int m_code;
    AcquisitionErrorCode(int code) {
      m_code=code;
    }
    public int getCode() {
      return m_code;
    }

    public static AcquisitionErrorCode get(int code) {
      for (AcquisitionErrorCode errorCode : AcquisitionErrorCode.values()) {
        if (errorCode.getCode() == code) {
          return errorCode;
        }
      }
      return UNKNOWN_ERROR;
    }

}
