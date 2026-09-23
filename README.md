# msDataConsumer

Defines a socket server to receive msData. The process of theses data depends on send command : 
write mzdb file
call callBack with read data

## Release History

### 1.4.0 (snapshot)

* Add method name, injection volume and vial information in RunMetaData  
* [Dev] Updated mzdb-access and mzdb-processing dependencies to use dynamic `classifier`

### 1.3.0
Add a Read Acquisition MetaData method 
Refactoring, Error code improvement, add information (time range...)

### 1.2.2
First release: Define a socket server to read data from and create mzdb file
