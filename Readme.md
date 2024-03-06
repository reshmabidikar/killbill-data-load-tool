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
java -jar .\killbill-data-load-tool-1.0.1-SNAPSHOT-shaded.jar <config-file-path>
````

Note that `<config-file-path>` is optional and if not specified, the default [config.properties]((https://github.com/reshmabidikar/killbill-data-load-tool/blob/main/src/main/resources/config.properties)) will be used

## Configuration

The tool can be configured by setting appropriate properties in a config file. The path of the config file can be specified as a command line argument. If not, the default [config.properties]((https://github.com/reshmabidikar/killbill-data-load-tool/blob/main/src/main/resources/config.properties)) will be used.

