# killbill-data-load-tool

Tool that can be used to create data on a KB instance. 

It does the following:

* Starting from the configured [startDate](https://github.com/reshmabidikar/killbill-data-load-tool/blob/166ed65f3b7c26a8e2f23371175d5668413882c3/src/main/resources/config.properties#L28), runs for the configured [number of days](https://github.com/reshmabidikar/killbill-data-load-tool/blob/166ed65f3b7c26a8e2f23371175d5668413882c3/src/main/resources/config.properties#L27)
* On each day, creates the configured [number of accounts](https://github.com/reshmabidikar/killbill-data-load-tool/blob/166ed65f3b7c26a8e2f23371175d5668413882c3/src/main/resources/config.properties#L25) and the configured [number of subscriptions per account](https://github.com/reshmabidikar/killbill-data-load-tool/blob/166ed65f3b7c26a8e2f23371175d5668413882c3/src/main/resources/config.properties#L26) 

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


