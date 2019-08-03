/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.sync.connectors.ssh.mpls.parsers;

import com.neotropic.kuwaiba.sync.connectors.ssh.mpls.entities.MPLSLinkNew;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * A parser for the output of the command "sh l2vpn xconnect" in the Cisco ASR9001 router series
 * Tue Feb 19 13:59:50.579 UTC
 * Legend: ST = State, UP = Up, DN = Down, AD = Admin Down, UR = Unresolved,
 *        SB = Standby, SR = Standby Ready, (PP) = Partially Programmed
 *
 * XConnect                   Segment 1                       Segment 2                
 * Group      Name       ST   Description            ST       Description            ST    
 * ------------------------   -----------------------------   -----------------------------
 * YYY        XXX-YYY-FFF-ELINE-20M-001
 *                       UP   gi0/0/0/5.792          UP       111.55.40.88   1151   UP    
 * ----------------------------------------------------------------------------------------
 * ZZZ        CCC-NEOTROPIC
 *                      UP   gi0/0/0/3.58           UP       111.33.44.55    900    UP    
 * ----------------------------------------------------------------------------------------
 * ...
 * ..
 * .
 * 
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MplsSyncASR9001Parser {
    /**
     * Parses the raw input
     * @param input The raw input that corresponds to the output of the command
     * @return The list of bridge domains in the given router (and inside, the related interfaces -VFI, service instances and BDI-)
     */
    public List<AbstractDataEntity> parseVcIds(String input) {
        List<AbstractDataEntity> mplsTransportLinks = new ArrayList<>();
        if(input != null){
            String[] lines = input.split("\n");
            ParsingState state = ParsingState.START;
            String serviceName = "", serviceCustomerAccronym = "";
            MPLSLinkNew currentMplsTransportLink = null;
            boolean isTest = false;
            for (String line : lines) {
                String[] lineTokens = line.trim().split("\\s+");
                 //check here if is necesary add the DOWN interfaces
                if (lineTokens.length == 1 && lineTokens[0].matches(".*[a-zA-Z]+.*") && state == ParsingState.START){
                    state = ParsingState.READING_CUSTOMER_NAME;
                    serviceCustomerAccronym = lineTokens[0];
                }//TODO the VFIs
                else if (lineTokens.length == 1 && state == ParsingState.READING_CUSTOMER_NAME){
                    state = ParsingState.READING_SERVICE_NAME;
                    if(lineTokens[0].toLowerCase().contains("test"))
                        isTest = true;
                    serviceName = lineTokens[0];
                }//TODO the VFIs
                //check here if is necesary add the DOWN interfaces
                else if (lineTokens.length == 2 && state == ParsingState.START){
                    state = ParsingState.READING_SERVICE_NAME;
                    if(lineTokens[0].toLowerCase().contains("test"))
                        isTest = true;
                    serviceName = lineTokens[1];
                    serviceCustomerAccronym = lineTokens[0];
                }//TODO the VFIs
                else if(lineTokens.length == 6 && lineTokens[0].equals("UP") && lineTokens[2].equals("UP") && lineTokens[5].equals("UP") && state == ParsingState.READING_SERVICE_NAME){
                    state = ParsingState.READING_INTERFACES;
                    currentMplsTransportLink = new MPLSLinkNew();
                    if(lineTokens[1].contains(".")){
                        currentMplsTransportLink.setLocalPhysicalInterface(lineTokens[1].split("\\.")[0]);
                        currentMplsTransportLink.setLocalVirtualInterface(lineTokens[1].split("\\.")[1]);
                    }
                    else if(lineTokens[1].contains(":")){
                        currentMplsTransportLink.setLocalPhysicalInterface(lineTokens[1].split(":")[0]);
                        currentMplsTransportLink.setLocalVirtualInterface(lineTokens[1].split(":")[1]);
                    }
                    currentMplsTransportLink.setVcidA(lineTokens[4]); //vcid
                    currentMplsTransportLink.setServiceName(serviceName);
                    currentMplsTransportLink.setDestinyIPAddress(lineTokens[3]);
                    currentMplsTransportLink.setServiceCustomerGroup(serviceCustomerAccronym);
                    
                    if(!isTest)
                        mplsTransportLinks.add(currentMplsTransportLink);
                    serviceName = ""; serviceCustomerAccronym = "";
                    state = ParsingState.START;
                }       
            }//end for
            state = ParsingState.END;
        }
        return mplsTransportLinks;
    }
    
    /**
     * The possible states of the parsing process
     */
    private enum ParsingState {
        /**
         * The default state
         */
        START, 
        /**
         * after the header the customer name could be so long an take a whole line
         */
        READING_CUSTOMER_NAME,
        /**
         * after the header, the customer name could be so long an take a whole line
         */
        READING_SERVICE_NAME,
        /**
         * after the service name
         */
        READING_INTERFACES,
         /**
         * the end of the list of the interfaces associated to a MPLSLink
         */
        END
    }
}