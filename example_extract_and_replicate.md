# Assumptions and adapter behavior
1) No PK updates
2) Inserts, updates and deletes only
3) No checks for duplicate-record - an insert will just overwrite the existing
4) Delete of a document that is not there is not an error

# Setup and start extract for the “ipas” database changes
## On Source system:
create dirprm/IPAS.prm
```
EXTRACT IPAS
DBOPTIONS HOST localhost, CONNECTIONPORT 3306
SOURCEDB ipas, USERID root, PASSWORD root
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

## Setup replicat for the “ipas” database changes
On target system:
create dirprm/IPAS.prm
```
REPLICAT IPAS
HANDLECOLLISIONS
TARGETDB LIBFILE libggjava.so SET property=marklogic/dirprm/ipas.props
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

create dirprm/IPASINIT.prm
```
SOURCEISTABLE
DBOPTIONS HOST localhost, CONNECTIONPORT 3306
SOURCEDB ipas, USERID root, PASSWORD root
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

create dirprm/IPASLOAD.prm
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

edit dirprm/IPASLOAD.prm and remove this line
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