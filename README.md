# What is GoldenGate?
GoldenGate is a Change Data Capture (CDC) system that can keep different databases in running in separate enviornments in-sync. It works on the pricipal of tracing transaction log activity. Any database that supports transaction logs can be used as source database to GoldenGate.

# What is MarkLogic GoldenGate Adapter
GoldenGate supports adapters that developer write custom target databases. MarkLogic is a document database that can store variety of content types. GG Adapter for MarkLogic can accept events like data INSERT,UPDATE, TRUNCATE, DELETE in relational database and send it to MarkLogic for processing. MarkLogic treats each record as a document and takes the required action.
![MarkLogic Adapter Architecture](/docs/architecture.jpg)

## Use Cases
MarkLogic is often used as a datahub for the information available in different environments, databases and formats. Adapter interface can be used to bring data from all the sources and also keep datahub upto date with all the latest changes.

## Configuration
Adapter has been tested with the following versions:

S.no| Software | Version
----|----------|--------
1.  |MarkLogic | 8.x, 9.x
2.  |MySQL     | 14.14
3.  |Java      | 1.8
4.  |GoldenGate| 12.3.1
5.  |Centos    | 7.2.1511

### Setup MySQL
Goldengate requires binary logging enabled in the source database. For MySQL its reletively straight forward. Details are in the link
https://dev.mysql.com/doc/refman/5.7/en/replication-options-binary-log.html

For MySQL to work with Goldengate I had to do 2 adjustments :
1. Create symlink to mysql.sock file in /tmp/ dir. That's what GG extract process expects. You can't change my.cnf to point to /tmp/ dir. Here is command to do that:
```
ln -s /var/lib/mysql/mysql.sock /tmp/mysql.sock
```

### Setup MarkLogic
https://docs.marklogic.com/guide/installation/procedures#id_28962

### Setup GoldenGate
This adapter does not include goldengate jar files due to licencing restrictions. After installing goldengate for MySQL please copy all gg*.jar files to /lib/ directory.
Here is the generic guide of installing GoldenGate for MySQL:
https://docs.oracle.com/goldengate/1212/gg-winux/GIMYS/toc.htm

#### GoldenGate Extract
Assuming at this point that you have goldengate setup with all the environment variables. Start manager on source envionment if you have not already done so. 
Extract is the process that reads binary logs from Mysql directory and converts that to trail files. These trail files will be send over the network to target goldengate enviornment. 
Here are the steps to setup Mysql trail:

1. Create a file EXT01.prm in ggs/dirprm/ directory
```
EXTRACT EXT01
DBOPTIONS HOST centos, CONNECTIONPORT 3306
SOURCEDB test, USERID root, PASSWORD root
RMTTRAIL ./dirdat/TR
RMTHOST centos2, MGRPORT 7801
TRANLOGOPTIONS ALTLOGDEST /var/lib/mysql/centos-bin.index
TABLE SOURCEDATABASE.*;
````
- Update SOURCEDATABASE to database name in source system. 
- Update RMTHOST to remote host where extract process will run.


2. Add extract to goldengate
```
ADD EXTRACT EXT01, TRANLOG, BEGIN NOW
````
3. Check the status of extract
```
INFO EXT01
```
and it should return
```
EXTRACT    EXT01    Last Started 2017-01-26 19:54   Status STOPPED
Checkpoint Lag       00:00:00 (updated 181:01:27 ago)
VAM Read Checkpoint  2017-01-26 21:10:49.000000
  Log Number: 28
    Record Offset: 30038
```

#### GoldenGate Replicat
Replicat is responsible sending transactions target MarkLogic server. Here are the steps to get it done:
##### Setup MarkLogic Adapter
1. Download Goldengate for BigData from this url: http://www.oracle.com/technetwork/middleware/goldengate/downloads/index.html
2. Extract the package to [HOME]/ggs directory.
3. Download MarkLogic adapter source from this repo and create a directory 'marklogic' under [HOME]/ggs
4. Copy ggdbutil*.jar, gguserexitapi*.jar and ggutil*.jar to [MARKLOGIC-ADAPTER-DIR]/lib directory
5. Update pom.xml <goldengate.version>[GOLDENGATE VERSION]</goldengate.version>
6. Run command ```mvn package -DskipTests=true``` to generate target jars
7. Create replicat file RML.prm in [GOLDENGATEDIR]/dirprm/ dir
```
REPLICAT rml
-- Trail file for this example is located in "AdapterExamples/trail" directory
-- Command to add REPLICAT
-- add replicat rml, exttrail AdapterExamples/trail/tr
TARGETDB LIBFILE libggjava.so SET property=marklogic/dirprm/marklogic.props
REPORTCOUNT EVERY 1 MINUTES, RATE
GROUPTRANSOPS 10000
MAP SOURCEDATABASE.*, TARGET SOURCEDATABASE.*;
```
- Update SOURCEDATABASE to database name in source system.

6. Copy Marklogic adapter dirctory to [GOLDENGATEDIR]/marklogic
7. Update [GOLDENGATEDIR]/marklogic/dirprm/marklogic.props with marklogic connection information. 
```
gg.handlerlist=marklogic
gg.handler.marklogic.type=oracle.goldengate.delivery.handler.marklogic.MarkLogicHandler
#MarkLogic Host
gg.handler.marklogic.host=localhost
#Default port for connecting to MarkLogic
gg.handler.marklogic.port=8000
gg.handler.marklogic.database=Documents
gg.handler.marklogic.user=admin
gg.handler.marklogic.password=admin
gg.handler.marklogic.auth=digest

#TODO add XML support
#Currently json is the only format supported. XML 
gg.handler.marklogic.format=json
gg.handler.marklogic.mode=tx
goldengate.userexit.timestamp=utc
goldengate.userexit.writers=javawriter
javawriter.stats.display=TRUE
javawriter.stats.full=TRUE
gg.log=log4j
gg.log.level=DEBUG
gg.report.time=30sec
##CHANGE THE PATH BELOW TO ADAPTER JAR directory
gg.classpath=/home/joe/ggs/marklogic/target/lib
javawriter.bootoptions=-Xmx512m -Xms32m -Djava.class.path=ggjava/ggjava.jar:
```
8. Add replicat on goldengate console using command 
```ADD REPLICAT RML, exttrail dirdat/TR```
9. Check status using ```INFO RML```
10. Start manager ```start mgr```
10. Start replicat ```start replicat RML```
11. Check status of replicat ```INFO RML```
#### Starting Extract 
On the source Goldengate envionrment
1. Start manager using ```start mgr```.
2. Start extract using ```start extract EXT01```. 
#### Testing CDC
Updates in MySQL database should replicated over to MarkLogic database configured in marklogic.props file. Following REST API returns the list of records loaded/updated in MarkLogic:
http://[MLHOSTNAME]:8000/LATEST/search?database=[MLDATABASENAME]
