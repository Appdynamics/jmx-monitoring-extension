# jmx-monitoring-extension

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
 with your server.

