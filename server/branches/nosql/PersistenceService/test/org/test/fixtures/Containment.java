/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.test.fixtures;

import java.util.HashMap;

/**
 * Containment Hierarchy
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Containment {
    public HashMap<String, String[]> containmentHierarchy = new HashMap<String, String[]>();

    public Containment() {
        containmentHierarchy = new HashMap<String, String[]>();
        containmentHierarchy.put("Continent", new String[]{"Country"});
        containmentHierarchy.put("Country", new String[]{"City", "State"});
        containmentHierarchy.put("City", new String[]{"Building", "Warehouse","Shelter", "Facility", "Tower", "Pole"});
        containmentHierarchy.put("Building", new String[]{"Room", "Floor", "Tower"});
        containmentHierarchy.put("Floor", new String[]{"Room"});
        containmentHierarchy.put("Facility", new String[]{"Building", "Warehouse", "Shelter", "Tower"});
        containmentHierarchy.put("Room", new String[]{"GenericBox","Workstation","Server","Printer", "AirConditioning"});
        containmentHierarchy.put("Lot", new String[]{"Shelter","Tower","OutdoorsCabinet"});
        containmentHierarchy.put("Rack", new String[]{"GenericNetworkElement", 
                                    "GenericPhysicalElement","Server", "TimeSource",
                                    "GenericDataLinkElement", "KVMSwitch", "FuseHolder"});
        containmentHierarchy.put("OutdoorsCabinet", new String[]{"GenericNetworkElement",
                                    "GenericPhysicalElement","GenericDataLinkElement", "FuseHolder"});
        containmentHierarchy.put("FuseHolder", new String[]{"Fuse"});
        containmentHierarchy.put("ACDistributionBoard", new String[]{"Breaker","Fuse"});
        containmentHierarchy.put("DCDistributionBoard", new String[]{"Breaker","Fuse"});
        containmentHierarchy.put("Breaker", new String[]{"PowerPort"});
        containmentHierarchy.put("Fuse", new String[]{"PowerPort"});
        containmentHierarchy.put("IPBoard", new String[]{"GenericCommunicationsPort"});
        containmentHierarchy.put("DWDMBoard", new String[]{"GenericCommunicationsPort"});
        containmentHierarchy.put("SDHBoard", new String[]{"GenericCommunicationsPort"});
        containmentHierarchy.put("RadioBoard", new String[]{"GenericCommunicationsPort"});
        containmentHierarchy.put("HybridBoard", new String[]{"GenericCommunicationsPort"});
        containmentHierarchy.put("ONTBoard", new String[]{"OpticalPort"});
        containmentHierarchy.put("DSLAMBoard", new String[]{"GenericCommunicationsPort"});
        containmentHierarchy.put("OLTBoard", new String[]{"OpticalPort"});
        containmentHierarchy.put("ATMBoard", new String[]{"GenericCommunicationsPort"});
        containmentHierarchy.put("Workstation", new String[]{"GenericComputerPart", "PeripheralPort", "PowerPort"});
        containmentHierarchy.put("Server", new String[]{"GenericComputerPart", "PeripheralPort", "PowerPort"});
        containmentHierarchy.put("DDF", new String[]{"ElectricalPort"});
        containmentHierarchy.put("ODF", new String[]{"OpticalPort"});
        containmentHierarchy.put("Tower", new String[]{"GenericAntenna"});
        containmentHierarchy.put("Slot", new String[]{"GenericBoard"});
        containmentHierarchy.put("Router", new String[]{"Slot", "GenericPort"});
        containmentHierarchy.put("SDHMux", new String[]{"Slot", "GenericPort"});
        containmentHierarchy.put("DWDMMux", new String[]{"Slot", "PowerPort", "OpticalPort"});
        containmentHierarchy.put("RadioShelf", new String[]{"Slot", "GenericPort"});
        containmentHierarchy.put("ATMMux", new String[]{"Slot", "GenericPort"});
        containmentHierarchy.put("DSLAM", new String[]{"Slot", "GenericPort"});
        containmentHierarchy.put("OpticalLineTerminal", new String[]{"Slot", "PowerPort", "OpticalPort"});
        containmentHierarchy.put("OpticalNetworkTerminal", new String[]{"Slot", "PowerPort", "OpticalPort"});
        containmentHierarchy.put("ParabolicAntenna", new String[]{"VirtualPort"});
        containmentHierarchy.put("HornAntenna", new String[]{"VirtualPort"});
        containmentHierarchy.put("DipoleAntenna", new String[]{"VirtualPort"});
    }


}
