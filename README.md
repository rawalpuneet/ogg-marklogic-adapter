# What is GoldenGate?
GoldenGate is a Change Data Capture (CDC) system that can keep different databases in running in separate enviornments in-sync. It works on the pricipal of tracing transaction log activity. Any database that supports transaction logs can be used as source database to GoldenGate. 

# What is MarkLogic GoldenGate Adapter
GoldenGate supports adapters that developer write custom target databases. MarkLogic is a document database that can store variety of content types. GG Adapter for MarkLogic can accept events like data INSERT,UPDATE, TRUNCATE, DELETE in relational database and send it to MarkLogic for processing. MarkLogic treats each record as a document and takes the required action. 
![MarkLogic Adapter Architecture](/docs/architecture.jpg)

## Use Cases
MarkLogic is often used as a datahub for the information available in different environments, databases and formats. Adapter interface can be used to bring data from all the sources and also keep datahub upto date with all the latest changes.

## Configuration
Adapter is currently cerfified for following versions:

S.no| Software | Version
----|----------|--------
1.  |MarkLogic | 8.x
2.  |MySQL     | 14.14
3.  |Java      | 1.8
4.  |GoldenGate| 12.2.0.1
5.  |Centos    | 7.2.1511

### Setup MySQL 

### Setup MarkLogic


### Setup GoldenGate

#### GoldenGate Extract 

#### GoldenGate Replicat

### Buik Load Initial Data
