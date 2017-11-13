# Configuring to use Data Hub Input Flows
When configured to use the data hub framework input flows and transforms, the following assumptions are made. The other sections detail how to setup golden gate initial export and load as well as continuous replication for change data capture.

## Assumptions and adapter behavior
1) No PK updates
2) Inserts, updates and deletes only
3) No checks for duplicate-record - an insert will just overwrite the existing
4) Delete of a document that is not there is not an error

# Setup and start extract for the “ipas” database changes
## On Source system:
Create <GG install dir>/dirprm/IPAS.prm
```
EXTRACT IPAS
DBOPTIONS HOST localhost, CONNECTIONPORT 3306
SOURCEDB ipas, USERID root, PASSWORD <root password>
RMTTRAIL ./dirdat/IP, FORMAT RELEASE 12.2
RMTHOST mlgg2, MGRPORT 7809
TRANLOGOPTIONS ALTLOGDEST /var/lib/mysql/mlgg1-bin.index
TABLE ipas.*;
```

via ggsci
```
> ADD EXTRACT IPAS, TRANLOG, BEGIN NOW
> ADD RMTTRAIL ./dirdat/IP, EXTRACT IPAS
> START EXTRACT IPAS
```

# Setup replicat for the “ipas” database changes
## On target system:

Extract the `ogg-marklogic-adapter` archive to <GG install dir>.

Edit <GG install dir>/ogg-marklogic-adapter/dirprm/ipas.props
1) Set `gg.handler.marklogic.host` to the MarkLogic host
1) Set `gg.classpath` to `<GG install dir>/ogg-marklogic-adapter/target/lib/*`

Create <GG install dir>/dirprm/IPAS.prm
```
REPLICAT IPAS
HANDLECOLLISIONS
TARGETDB LIBFILE libggjava.so SET property=ogg-marklogic-adapter/dirprm/ipas.props
REPORTCOUNT EVERY 1 MINUTES, RATE
GROUPTRANSOPS 10000
MAP ipas.*, TARGET ipas.*;
```
via ggsci
```
> ADD REPLICAT IPAS, exttrail dirdat/IP
```

# Setup and run initial extract
## On Source system:

Create <GG install dir>/dirprm/IPASINIT.prm
```
SOURCEISTABLE
DBOPTIONS HOST localhost, CONNECTIONPORT 3306
SOURCEDB ipas, USERID root, PASSWORD <root password>
RMTHOST mlgg2, MGRPORT 7809
RMTFILE ./dirdat/initld, FORMAT RELEASE 12.2, MEGABYTES 2, PURGE
TABLE ipas.*;
```
add via ggsci
```
> add extract ipasinit, sourceistable
```
run
```
./extract pf dirprm/IPASINIT.prm rf dirrpt/ipastinit.rpt
```

This should create a remote trail file on the target system with all of the data

# Setup and run initial load
## On the target system:

create <GG install dir>/dirprm/IPASLOAD.prm
```
SPECIALRUN
ASSUMETARGETDEFS
HANDLECOLLISIONS
TARGETDB LIBFILE libggjava.so SET property=marklogic/dirprm/ipas.props
EXTFILE ./dirdat/initld
MAP ipas.*, TARGET ipas.*;
END RUNTIME
```
add via ggsci
```
> add replicat ipasload, extfile ./dirdat/initld
```
run
```
./replicat pf dirprm/IPASLOAD.prm rf dirrpt/ipasinit.rpt
```
# Run replicat to load any changes from during the initial load
## On target system:

via ggsci
```
> START IPAS
```

# Restart replicat for normal operation
Remove the HANDLECOLLISIONS parameter
## On the target system:

edit <GG install dir>/dirprm/IPASLOAD.prm and remove this line
```
HANDLECOLLISIONS
```
via ggsci
```
> STOP IPAS
> START IPAS
```

# Logs
```
ggserr.log
dirrpt/IPAS.rpt
```
