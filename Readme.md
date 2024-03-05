# killbill-data-load-tool

Tool that can be used to create data on a KB instance

## Build 

To build the code:

````
mvn clean package
````

## Run

````
java -jar .\killbill-data-load-tool-1.0.1-SNAPSHOT-shaded.jar
````

## Configuration

The tool can be configured by setting appropriate values for the properties in the [config](https://github.com/reshmabidikar/killbill-data-load-tool/blob/main/src/main/resources/config.properties) file. Note that if the tool is being run as a jar file, you will need to build the jar again after setting the appropriate properties.


