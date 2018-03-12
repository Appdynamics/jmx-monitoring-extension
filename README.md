# JMX Monitoring Extension

## Use Case

The JMX Monitoring extension collects metrics from a JMX based messaging server and uploads them to the AppDynamics Metric Browser. 

## Prerequisites 

JMX must be enabled in your JMX based server for this extension to gather metrics. Please make sure you have all the permissions before deploying the extension.
In order to use this extension, you do need a [Standalone JAVA Machine Agent](https://docs.appdynamics.com/display/PRO44/Java+Agent) or [SIM Agent](https://docs.appdynamics.com/display/PRO44/Server+Visibility). 
For more details on downloading these products, please  visit [download.appdynamics.com](https://download.appdynamics.com/).

The extension needs to be able to connect to the JMX based server in order to be collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.

## Installation

1. To build from source, clone this repository and run 'mvn clean install'. This will produce a JMXMonitor-VERSION.zip in the target directory. 
Alternatively, download the latest release archive from [Github](https://github.com/Appdynamics/jmx-monitoring-extension/releases).
2. Unzip the file JMXMonitor-[version].zip into `<MACHINE_AGENT_HOME>/monitors/`.
3. In the newly created directory "JMXMonitor", edit the config.yml configuring the parameters (See Configuration section below).
4. Make sure you place it in the right directory on your computer. 
5. Restart the machineagent
6. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | JMX Monitor  .
7. If you're monitoring multiple JMX instances, follow the above steps for every JMX instance that you want to monitor.

**NOTE:** Please place the extension in the **"monitors"** directory of your Machine Agent installation directory. Do not place the extension in the "extensions" directory of your Machine Agent installation directory.

## Configuration

Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the JMX connection parameters by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/JMXMonitor/`. 
Here is a sample config.yml file

```
### ANY CHANGES TO THIS FILE DOES NOT REQUIRE A RESTART ###
#This will create this metric in all the tiers, under this path
#metricPrefix: Custom Metrics|JMXMonitor

#This will create it in specific Tier. Replace <TIER_NAME>
metricPrefix: "Server|Component:<TIER_NAME or ID>|Custom Metrics|JMXMonitor"


# List of JMX Servers
servers:
  -   displayName: ""
#     displayName is a required field. This will be your server name that will show up in metric path.

#     You can either use just a host and port to connect or use your full serviceURL to make the connection
#     Do not choose both, comment one out and only use the other.
      host: ""
      port:

#      serviceUrl: ""

      username: ""
      password: ""

#     You can either use the normal password or encrypt your password and provide the encrypted Password and encryptionKey.
#     Do not provide both, only provide one and comment out the other.

#      encryptedPassword: ""
#      encryptionKey: ""

# number of concurrent tasks.
# This doesn't need to be changed unless many servers are configured
numberOfThreads: 10

#timeout for the thread
threadTimeout: 30
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#                                      List of metrics
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#Glossary of terms(These terms are used as properties for each metric):
#   alias
#   aggregationType
#   timeRollUpType
#   clusterRollUpType                                                                                                                                                                                                                                                                                                                                                                                                                                                                            }
#   multiplier -->not for derived metrics
#   convert --> not for derived metrics
#   delta --> not for derived metrics
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# The configuration of different metrics from various mbeans of jmx server
# The mbeans are already configured.This does not need to be changed unless
# someone needs to configure on their own.
mbeans:
  # This Mbean will extract out Broker metrics
  - objectName: "org.apache.activemq:type=Broker,brokerName=*"
    mbeanKeys: ["type", "brokerName"]
    metrics:
      include:
        - StorePercentUsage: "Store Percent Usage"

  # This Mbean will extract out Queue metrics
  # This example also shows how you can change the default properties of a metric.
  - objectName: "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=*"
    mbeanKeys: ["type", "brokerName","destinationType","destinationName"]
    metrics:
      include:
        - AverageEnqueueTime: "AverageEnqueueTime"
          clusterRollUpType: "AVERAGE"
          timeRollUpType: "SUM"
          aggregationType: "SUM"

        - ConsumerCount: "ConsumerCount"
          clusterRollUpType: "AVERAGE"
          timeRollUpType: "SUM"
          aggregationType: "SUM"


  # Composite Metrics can be set in the following way
  - objectName: "java.lang:type=Memory"
    mbeanKeys: ["type"]
    metrics:
      include:
        - HeapMemoryUsage.committed : "Heap Memory Usage|Committed"
```
2. There are a few fields that you need to make sure are filled in correctly. 
Once done with them, they should allow you to establish a successful connection
 with your server. They are : 
```
servers:
  -   displayName: ""
      host: ""
      port:

#      serviceUrl: ""

      username: ""
      password: ""

#      encryptedPassword: ""
#      encryptionKey: ""
```
* displayName: This will be the name of your server that you would like to see on the metric browser.
* host: This is the HostURL that is used with a port to create a connection with the JMX Server.
* serviceUrl: This is the full URL with host and port that is used to establish a connection. 

**You should either use HOST AND PORT or just the SERVICEURL in order to establish a connection.**

* username: List the username, if any, that is needed to establish a connection.
* password: List the password associated with the username that is needed to establish a connection.
* encryptedPassword: In case you would like to use an encrypted password, use this field.
* encryptionKey: If you use an encryptedPassword, please provide the key here as well in order for the system to decrypt your password.

**You should either use the Normal PASSWORD or the encryptedPassword and encryptionKey in order to establish a connection. Please read below to find more information on Password Encryption.**

3. Configure the "tier" under which the metrics need to be reported. This can be done by changing the value of `<TIER NAME OR TIER ID>` in
     metricPrefix: "Server|Component:`<TIER NAME OR TIER ID>`|Custom Metrics|JMX Monitor". For example,
    
```
     metricPrefix: "Server|Component:Extensions tier|Custom Metrics|JMX Monitor"
```
## Metrics

You can use this extension to get all metrics that are available through the JMX Messaging service. In order to do so though, you will have to make sure that all metrics are defined correctly.
Please follow the next few steps in order to get this right.
1. You will have to list each mBean separately in the config.yml file. 
For each mBean you will have to add an **objectName**, **mbeanKeys** and **metrics** tag.
The following example shows exactly how you should do that. 
* You will have to each and every **mBeanKey** that is listed in the **objectName**.
* Under **metrics** is where you have the ability to include all the metrics that you would like to monitor.
```
mbeans:
  - objectName: "org.apache.activemq:type=Broker,brokerName=*"
    mbeanKeys: ["type", "brokerName"]
    metrics:
      include:
        - StorePercentUsage: "Store Percent Usage"

```
2. There are several properties that are associated with each metric. They are: 
    * alias
    * aggregationType
    * timeRollUpType
    * clusterRollUpType
    * multiplier
    * convert
    * delta
   
   This format enables you to change some of the metric properties from what the default configurations are.

    In Order to use them for each metric, please use the following example.
```
  - objectName: "org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=*"
    mbeanKeys: ["type", "brokerName","destinationType","destinationName"]
    metrics:
      include:
        - AverageEnqueueTime: "Average Enqueue Time"
          clusterRollUpType: "AVERAGE"
          timeRollUpType: "SUM"
          aggregationType: "SUM"
```

3. This extension can also be used to get values from **composite objects**. 
In order to do so, you have to list the metric name as is and then specify the path with a **"|"** followed my the composite attribute.
In this example we see that HeapMemoryUsage is a composite object that has 4 values associated with it. 
Now in order to monitor them, you list the property and then in the alias name, add the **"|"** followed by the attribute name in order to get all of the attributes associated with HeapMemoryUsage under one folder in the metric browser.

```
  - objectName: "java.lang:type=Memory"
    mbeanKeys: ["type"]
    metrics:
      include:
        - HeapMemoryUsage.committed : "Heap Memory Usage|Committed"
        - HeapMemoryUsage.init : "Heap Memory Usage|Initialized"
        - HeapMemoryUsage.max : "Heap Memory Usage|Max"
        - HeapMemoryUsage.used : "Heap Memory Usage|Sum"
```
    
## Contributing
Always feel free to fork and contribute any changes directly here on [GitHub].

## Community
Find out more in the [AppDynamics Exchange] community.

## Troubleshooting ##

Please follow the steps listed in this [troubleshooting-document] in order to troubleshoot your issue. 
These are a set of common issues that customers might have faced during the installation of the extension. 
If these don't solve your issue, please follow the last step on the [troubleshooting-document] to contact the support team.

## Credentials Encryption ##

Please visit [Encryption Guidelines] to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.
If you want to use password encryption, please send arguments as connectionProperties. You will have to fill in the encrypted Password and Encryption Key fields in the config but you will also have to give an empty "" value to the password field and the encrypted password will be automatically picked up.

## Extensions Workbench ##
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually
 deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench ]

## Version 
|Product | Version | 
| ----- | ----- | 
| Extension Version|  1.0.0 | 
| Controller Compatability | 3.7+ |
| Last Updated | March 3, 2018 | 

**List of Changes can be found in the [Changelog.md]**


[How to use the Extensions WorkBench ]: https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130
[Changelog.md]: https://github.com/Appdynamics/jmx-monitoring-extension/blob/1.0.0/Changelog.md
[Encryption Guidelines]: https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397
[troubleshooting-document]: https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695
[AppDynamics Exchange]: https://www.appdynamics.com/community/exchange/extension/jmx-monitoring-extension/
[GitHub]: https://github.com/Appdynamics/jmx-monitoring-extension/
