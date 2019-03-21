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

import com.neotropic.kuwaiba.sync.connectors.ssh.mpls.entities.MPLSLink;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
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
            MPLSLink currentMplsTransportLink = null;
            
            for (String line : lines) {
                String[] lineTokens = line.trim().split("\\s+");
                //check here if is necesary add the DOWN interfaces
                if (lineTokens.length == 2){
                    state = ParsingState.READING_SERVICE_NAME;
                    serviceName = lineTokens[1];
                    serviceCustomerAccronym = lineTokens[0];
                }//TODO the VFIs
                else if(lineTokens.length == 6 && lineTokens[0].equals("UP") && lineTokens[2].equals("UP") && lineTokens[5].equals("UP") && state == ParsingState.READING_SERVICE_NAME){
                    state = ParsingState.READING_INTERFACES;
                    currentMplsTransportLink = new MPLSLink(SyncUtil.normalizePortName(lineTokens[1]), lineTokens[4], lineTokens[3], serviceName, serviceCustomerAccronym);
                    mplsTransportLinks.add(currentMplsTransportLink);
                    serviceName = ""; serviceCustomerAccronym = "";
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
         * after the header
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
