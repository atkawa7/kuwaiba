SNMP with JBATCH is an implementation to understand the use of an SNMP Manager
and batch jobs to get information of agents using the SNMP protocol.

The application is in the http://localhost:8080/snmp_batch-war
you give the information to connect with the agent and in the log of server
you see the result. For test purposes there are a delay of five (5) seconds 
in the process of the request, to prove multiple request from a client.

Additional information

 Add the snmp4j-2.5.6.jar file to the SNMP manager created using SNMP4J
 https://oosnmp.net/dist/release/org/snmp4j/snmp4j/2.5.6/snmp4j-2.5.6-distribution.zip

 To simulate Agents you can use the next tools:
 - Verify the manager output and fix the host like an agent use http://net-snmp.sourceforge.net/
 - Simulate agents use http://snmpsim.sourceforge.net/ 
 - Translate snmp walks to the numeric OID format used in snmpsim use https://github.com/murrant/librenms-snmpsim
 - See the tree structure of a MIB file use https://www.mibble.org/

References
    http://www.snmp4j.org/
    https://docs.oracle.com/javaee/7/tutorial/batch-processing.htm#GKJIQ6

