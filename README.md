# JMX Monitoring Extension

## Use Case

The JMX Monitoring extension collects metrics from a JMX based messaging server and uploads them to the AppDynamics Metric Browser. 

## Prerequisites 
1. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.

2. JMX must be enabled in your JMX based server for this extension to gather metrics. Please make sure you have all the permissions before deploying the extension.

3. The extension needs to be able to connect to the JMX based server in order to be collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.

## Installation

1. To build from source, clone this repository and run 'mvn clean install'. This will produce a JMXMonitor-VERSION.zip in the target directory. 
2. Unzip the file JMXMonitor-[version].zip into `<MACHINE_AGENT_HOME>/monitors/`.
3. In the newly created directory "JMXMonitor", edit the config.yml configuring the parameters (See Configuration section below).
4. Make sure you place it in the right directory on your computer. 
5. Restart the machineagent
6. If you're monitoring multiple JMX instances, follow the above steps for every JMX instance that you want to monitor.

**NOTE:** Please place the extension in the **"monitors"** directory of your Machine Agent installation directory. Do not place the extension in the "extensions" directory of your Machine Agent installation directory.

## Configuration

Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](https://jsonformatter.org/yaml-validator)

1. Configure the JMX connection parameters by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/JMXMonitor/`. 

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
      #encryptedPassword: ""
```
* displayName: This will be the name of your server that you would like to see on the metric browser.
* host: This is the HostURL that is used with a port to create a connection with the JMX Server.
* serviceUrl: This is the full URL with host and port that is used to establish a connection. 

**You should either use HOST AND PORT or just the SERVICEURL in order to establish a connection.**

* username: List the username, if any, that is needed to establish a connection.
* password: List the password associated with the username that is needed to establish a connection.
* encryptedPassword: In case you would like to use an encrypted password, use this field.

 3. Configure the encyptionKey for encryptionPasswords(only if password encryption required).
    For example,
 ```
    #Encryption key for Encrypted password.
    encryptionKey: "axcdde43535hdhdgfiniyy576"
 ```
 4. Configure the numberOfThreads
    For example,
    If number of servers that need to be monitored is 3, then number of threads required is 5 * 3 = 15
 ```
    numberOfThreads: 15
 ```  

* encryptionKey: If you use an encryptedPassword, please provide the key here as well in order for the system to decrypt your password.

**You should either use the Normal PASSWORD or the encryptedPassword and encryptionKey in order to establish a connection. Please read below to find more information on Password Encryption.**

5. 
The metricPrefix of the extension has to be configured as [specified here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695#Configuring%20an%20Extension). Please make sure that the right metricPrefix is chosen based on your machine agent deployment, otherwise this could lead to metrics not being visible in the controller.
Configure the "tier" under which the metrics need to be reported. This can be done by changing the value of `<TIER NAME OR TIER ID>` in
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
        - name: "StorePercentUsage"
          alias: "Store Percent Usage"

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
        - name: "AverageEnqueueTime"
          alias: "Average Enqueue Time"
          clusterRollUpType: "AVERAGE"
          timeRollUpType: "SUM"
          aggregationType: "SUM"
```

3. This extension can  be used to get values from **composite objects**. 
In order to do so, you have to list the metric name as is and then specify the path with a **"|"** followed my the composite attribute.
In this example we see that HeapMemoryUsage is a composite object that has 4 values associated with it. 
Now in order to monitor them, you list the property and then in the alias name, add the **"|"** followed by the attribute name in order to get all of the attributes associated with HeapMemoryUsage under one folder in the metric browser.

```
  - objectName: "java.lang:type=Memory"
    mbeanKeys: ["type"]
    metrics:
      include:
        - name: "HeapMemoryUsage.committed"
          alias: "Heap Memory Usage|Committed"
        - name: "HeapMemoryUsage.used"
          alias: "Heap Memory Usage|Used"
```

4. This extension can be used to get values from Map Objects as well. 
To do so, list the metrics you would like to retrieve from the map in the following manner. 
 ```
  - objectName: "java.lang:type=Memory"
    mbeanKeys: ["type"]
    metrics:
      include:
         # Map Metric Level 1
         - name: "MapOfString.key1"
           alias: "Map 1|Key 1"
         - name: "MapOfString.key2"
           alias: "Map 1|Key 2"
 
         # Map Metric Level 2
         - name: "MapOfString.map2.key2"
           alias: "Map 1|Map 2|Key 2"
 
         # Map Metric Level 3
         - name: "MapOfString.map2.map3.key32"
           alias: "Map 1|Map 2|Map 3|Key 32"
           multiplier: "20"
           delta: false
           aggregationType: "OBSERVATION"
           timeRollUpType: "AVERAGE"
           clusterRollUpType: "INDIVIDUAL"
           convert : {
             "ENDANGERED" : "1",
             "NODE-SAFE" : "2",
             "MACHINE-SAFE" : "3"
           }
```
    
5. This extension can be used to get data from List Objects as well. 
    To do so, the metric in the list should be separated with a separator such as a ":" and should be in a key value pair form.
    If your metric is not in the form listed about, the extension will not be able to extract that data. 

``` 
 - objectName: "java.lang:type=Memory"
    mbeanKeys: ["type"]
    metrics:
      include:
        # List Metrics Can be set in the following ways:
        - name: "listOfString.metric one"
          alias: "listOfString|metric one"
        - name: "listOfString.metric two"
          alias: "listOfString|metric two"
        - name: "listOfString.metric three" 
          alias: "listOfString|metric three"
```    

## metricPathReplacements
Please visit [this](https://community.appdynamics.com/t5/Knowledge-Base/Metric-Path-CharSequence-Replacements-in-Extensions/ta-p/35412) page to get detailed instructions on configuring Metric Path Character sequence replacements in Extensions.

## Credentials Encryption ##

Please visit [Encryption Guidelines](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.
If you want to use password encryption, please send arguments as connectionProperties. You will have to fill in the encrypted Password and Encryption Key fields in the config but you will also have to give an empty "" value to the password field and the encrypted password will be automatically picked up.

## Extensions Workbench ##
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually
 deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench ](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting ##

Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. 

## Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://www.appdynamics.com/community/exchange/jmx-monitoring-extension/).

## Version 
|Product | Version | 
| ----- | ----- | 
| Extension Version|  1.2.1 | 
| Last Updated | Jan 8 , 2021 | 
| Changelist | [Changelog](https://github.com/Appdynamics/jmx-monitoring-extension/blob/master/Changelog.md) |

**Note**: While extensions are maintained and supported by customers under the open-source licensing model, they interact with agents and Controllers that are subject to [AppDynamics’ maintenance and support policy](https://docs.appdynamics.com/latest/en/product-and-release-announcements/maintenance-support-for-software-versions). Some extensions have been tested with AppDynamics 4.5.13+ artifacts, but you are strongly recommended against using versions that are no longer supported.
