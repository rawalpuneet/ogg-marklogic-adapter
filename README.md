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
Goldengate requires binary logging enabled in the source database. For MySQL its reletively straight forward. Details are in the link
```https://dev.mysql.com/doc/refman/5.7/en/replication-options-binary-log.html```

For MySQL to work with Goldengate I had to do 2 adjustments :
1. Create symlink to mysql.sock file in /tmp/ dir. That's what GG extract process expects. You can't change my.cnf to point to /tmp/ dir. Here is command to do that:
```
#tmpmysql.sh
ln -s /var/lib/mysql/mysql.sock /tmp/mysql.sock
```


### Setup MarkLogic
```https://docs.marklogic.com/guide/installation/procedures#id_28962```

### Setup GoldenGate
This adapter does not include goldengate jar files due to licencing restrictions. After installing goldengate for MySQL please copy all gg*.jar files to /lib/ directory.
Here is the generic guide of installing GoldenGate for MySQL
```https://docs.oracle.com/goldengate/1212/gg-winux/GIMYS/toc.htm```

#### GoldenGate Extract

#### GoldenGate Replicat

### Buik Load Initial Data
