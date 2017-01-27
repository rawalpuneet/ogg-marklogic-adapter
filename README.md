# What is GoldenGate?
GoldenGate is a Change Data Capture (CDC) system that can keep different databases in running in separate enviornments in-sync. It works on the pricipal of tracing transaction log activity. Any database that supports transaction logs can be used as source database to GoldenGate. 

# What is MarkLogic GoldenGate Adapter
GoldenGate supports adapters that developer write custom target databases. MarkLogic is a document database that can store variety of content types. GG Adapter for MarkLogic can accept events like data INSERT,UPDATE, TRUNCATE, DELETE in relational database and send it to MarkLogic for processing. MarkLogic treats each record as a document and takes the required action. 

## Use Cases

## Configuration

### Setup MySQL 

### Setup MarkLogic

### Setup GoldenGate

#### GoldenGate Extract 

#### GoldenGate Replicat

### Buik Load Initial
