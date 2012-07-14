/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.test.fixtures;

import java.util.HashMap;

/**
 * Default list types
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ListTypes {

    public HashMap<String, String[]> listTypes;
    public ListTypes() {
        listTypes = new HashMap<String, String[]>();
        listTypes.put("StructuralState", new String[]{"Good", "Needs repairs", "Poor"});
        listTypes.put("OperationalState", new String[]{"Operative", "Damaged", "Being repaired", "Being trasported"});
        listTypes.put("OperatingSystem", new String[]{"Windows XP", "Windows Vista", "Windows 7", "Windows 8", "Windows 2000 Server",
                            "Windows Server 2003", "Windows Server 2008", "Windows SBS 2003", "Windows SBS 2008","Windows SBS 2011",
                            "Windows HPC Server 2008", "Windows EBS 2008", "Windows Server 2012","RHEL 2.1 AS","RHEL 2.1 ES",
                            "RHEL 3", "RHEL 4","RHEL 5","RHEL 6", "SLES 9", "SLES 10", "SLES 11", "Solaris 8", "Solaris 9", "Solaris 10", "Solaris 11",
                            "HP UX 11.00", "HP UX 11.04", "HP UX 11.10", "HP UX 11.11", "HP UX 11.20", "HP UX 11.22", "HP UX 11.23", "HP UX 11.31",
                            "AIX 5L 5.1", "AIX 5L 5.2", "AIX 5L 5.3", "AIX 6.1", "AIX 7.1",
                            "MacOSX 10.0", "MacOSX 10.1", "MacOSX 10.2", "MacOSX 10.3", "MacOSX 10.4", "MacOSX 10.5", "MacOSX 10.6", "MacOSX 10.7", "MacOSX 10.8"});
        listTypes.put("VirtualizationSoftware", new String[]{"VirtualBox 3.x", "VirtualBox 4.x",
                      "VMWare ESX Server 4.x", "VMWare ESX Server 5.x", "VMWare Server 1.x", "VMWare Server 2.x",
                      "Hyper-V", "KVM", "Oracle VM Server", "Xen"});
        listTypes.put("NetworkService", new String[]{"DNS", "DHCP", "LDAP", "Time Server",
                      "Radius", "Web", "FTP", "SSH", "Telnet", "Database", "NFS", "Samba", "Streaming", "Routing"});
        listTypes.put("ServerSidePlatform", new String[]{"PHP 3.x", "PHP 4.x", "PHP 5.x", "ASP 2.0", "ASP 3.0", ".NET 3.5", ".NET 4",
                                                        "Java EE 5", "Java EE 6", "Ruby", "Python", "PERL", "CGI"});
        listTypes.put("Database", new String[]{"MySQL 4.x", "MySQL 5.x", "PostgreSQL 8.x", "PostgreSQL 9.x", "SQL Server 2005", "SQL Server 2008", "SQL Server 2012",
                                               "Sybase ASE 14.x", "Sybase ASE 15.x", "Oracle 8", "Oracle 8i", "Oracle 9i", "Oracle 10g", "Oracle 11g", "Neo4J 1.x",
                                                "Firebird 1.x", "Firebird 2.x"});
        listTypes.put("SoftwareType", new String[]{"Operating System", "Application Server", "Development Tool", "Database Management System",
                                                    "Office Desktop Application", "Network Management Application", "Web Application", "Another Service"});
        listTypes.put("WireContainerType", new String[]{"Conduit", "Cable Tray", "Trunking"});
        listTypes.put("WirelessContainerType", new String[]{"Microwave Link", "Waveguide"});
        listTypes.put("TowerType", new String[]{"Mast", "Metalic Structure"});
        listTypes.put("ElectricalLinkType", new String[]{"Coaxial RG-58", "Coaxial RG-59", "Twisted CAT-5", "Twisted CAT-6", "Twisted CAT-7"});
        listTypes.put("OpticalLinkType", new String[]{"Monomode", "Multimode Graded Index", "Multimode Step Index"});
        listTypes.put("WirelessLinkType", new String[]{"Channel"});
        listTypes.put("HardDiskType", new String[]{"IDE", "SATA", "SCSI", "Solid State Disk", "USB"});
        listTypes.put("PrinterType", new String[]{"Dot-matrix", "Toner-based", "Liquid ink-jet", "Solid ink", "Inkless", "Dye-sublimation"});
        listTypes.put("MouseType", new String[]{"Optical USB", "Trackball USB", "Serial RS-232","Serial PS/2", "Wireless"});
        listTypes.put("ComputerMonitorType", new String[]{"CRT", "Flat"});
        listTypes.put("KeyboardType", new String[]{"USB", "Serial PS/2", "Wireless"});
        listTypes.put("CommunicationsPortType", new String[]{"BNC", "TNC", "DE-9", "RJ-11", "RJ-14", "RJ-21", "RJ-25", "RJ-45","RJ-48", "RJ-61", "V35", //Electrical interfaces
                                                            "ADT-UNI", "DMI", "E-2000/LSH", "EC", "F-3000", "FC", "FiberGate", "FSMA", "LC", "MIC", "MPO/MTP", "MT", "MT-RJ", "SC", "SMC"});
        listTypes.put("TimeSourceType", new String[]{"Stratum 0", "Stratum 1", "Stratum 2", "Stratum 3"});
        listTypes.put("IPAddressType", new String[]{"IPv$", "IPv6"});
        listTypes.put("PowerPortType", new String[]{"NEMA 5-15R", "NEMA 5-15P"});
        listTypes.put("RackType", new String[]{"Enclosed", "Open Frame", "2Post", "Portable", "Wall Mounted"});
        listTypes.put("AntennaType", new String[]{"Yagi-Uda", "Log-periodic", "Horn","Helical", "Sector", "Discone", "Monopole", "Loop", "Wire Grid","Dish"});
        listTypes.put("PolarizationType", new String[]{"Vertical", "Horizontal", "Elliptical", "Circular"});
        listTypes.put("RateType", new String[]{"Kbps","Mbps", "Gbps"});
        listTypes.put("PeripheralPortType", new String[]{"USB", "PS/2", "DB-25", "DE-9", "USB 2", "USB 3", "Firewire", "SATA", "HDMI", "VGA (DE-15)", "DVI"});
        listTypes.put("FacilityType", new String[]{"Campus", "Building", "House"});
        listTypes.put("EnvironmentalCondition", new String[]{"Indoors Normal", "Indoors Humidity", "Indoors Direct Sunlight", "Indoors No Thermal Control","Outdoors"});
        listTypes.put("EquipmentVendor", new String[]{"Alcatel-Lucent", "APC", "Cisco", "Juniper","D-Link", "Dell", "HP", "IBM", "Oracle", "Extreme Networks", "Huawei", "ZTE", "Siemens", "Nortel", "ECI",
                                                     "Ericsson", "Nokia-Siemens Networks", "Harris", "Motorola", "Arris", "NEC", "Fujitsu", "Tellabs", "Lenovo", "Sony", "Avaya", "JDSU", "Symmetricon"});
        listTypes.put("SoftwareVendor", new String[]{"Microsoft", "Oracle", "Red Hat", "Adobe","Sun Microsystems", "Apple", "SAP", "JD Edwards", "Enterprise DB",  "Neo Technologies", "HP",
                                                    "VMWare"});
        listTypes.put("LocationOwner", new String[]{"Owner 1","Owner 2", "Owner 3", "Owner 4", "Owner 5"});
        listTypes.put("Employee", new String[]{"Employee 1","Employee 2", "Employee 3", "Employee 4", "Employee 5"});


    }

}
