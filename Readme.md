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

## Logging

By default, the data load tool uses the default logback file which is configured to log to the console.

You can however customize this as follows:

1. Create a logback.xml file as follows (Replace `<log_file_path>` with the actual path where you want the logs to be created):
````
<!--
  ~Copyright 2014-2024 The Billing Project, LLC
  ~
  ~ The Billing Project licenses this file to you under the Apache License, version 2.0
  ~ (the "License"); you may not use this file except in compliance with the
  ~ License.  You may obtain a copy of the License at:
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  ~ WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  ~ License for the specific language governing permissions and limitations
  ~ under the License.
  -->
<configuration>
    <property name="LOGS_DIR" value="<log_file_path>" />
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%date [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOGS_DIR}/dataloadtool.out</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <!-- rollover daily -->
        <fileNamePattern>${LOGS_DIR:-./logs}/killbill-%d{yyyy-MM-dd}.%i.out.gz</fileNamePattern>
        <maxHistory>3</maxHistory>
        <cleanHistoryOnStart>true</cleanHistoryOnStart>
        <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
        <!-- or whenever the file size reaches 100MB -->
        <maxFileSize>100MB</maxFileSize>
        </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>%date [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="info">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
````
2. Add the following property to the config file:
````
logback.configurationFile=<path of logback xml>
````