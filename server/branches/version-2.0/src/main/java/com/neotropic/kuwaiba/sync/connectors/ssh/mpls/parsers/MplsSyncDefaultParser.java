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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for the output of the command "sh mpls l2transport vc" 
 * in the Cisco ASR920, ASR1002, ASR1006, ME3600 router series
 * 
 * Local intf     Local circuit              Dest address    VC ID      Status
 *  -------------  -------------------------- --------------- ---------- ----------
 *  pw1052         99.255.93.20 1052        111.33.140.3    1051       UP        
 *  pw3904         99.255.93.20 3904        111.33.140.8    417        UP   
 *  ...
 *  ...
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MplsSyncDefaultParser {
    private final Pattern ptn = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    /**
     * Parses the raw input
     * @param input The raw input that corresponds to the output of the command
     * @return The list of bridge domains in the given router (and inside, the related interfaces -VFI, service instances and BDI-)
     */
    public List<AbstractDataEntity> parseVcs(String input) {
        List<AbstractDataEntity> mplsTransportLinks = new ArrayList<>();
        if(input != null){
            String[] lines = input.split("\n");
            ParsingState state = ParsingState.START;
            for (String line : lines) {
                String[] lineTokens = line.trim().split("\\s{2,}");
                
                //check here if is necesary add the DOWN interfaces
                if (lineTokens.length >= 5){ //NOI18N
                    state = ParsingState.READING_INTERFACES;
                    
                    if(lineTokens.length == 5 && !lineTokens[0].toLowerCase().startsWith("vfi") && lineTokens[4].equals("UP")){ 

                        Matcher mtch = ptn.matcher(lineTokens[1].split("\\s")[0]);
                        String localIp = null;
                        if(mtch.find())
                            localIp = lineTokens[1].split("\\s")[0];

                        mplsTransportLinks.add(new MPLSLink(SyncUtil.normalizePortName(lineTokens[0]), localIp, lineTokens[3], lineTokens[2]));
                    }
                }//TODO the VFIs
            }//end for
            state = ParsingState.END;
            
        }
        return mplsTransportLinks;
    }
    
    /**
     * Parses the raw input of a VCid detail
     * @param input The raw input that corresponds to the output of the command
     * @param entry entry of mpls to check its details
     * @return The list of bridge domains in the given router (and inside, the related interfaces -VFI, service instances and BDI-)
     */
    public MPLSLink parseVcDetails(String input, MPLSLink entry) {
        if(input != null){
            String[] lines = input.split("\n");
            ParsingState state = ParsingState.START;
           
            for (String line : lines) {
                String[] lineTokens = line.trim().split("\\s+");
                //check here if is necesary add the DOWN interfaces
                if (state == ParsingState.START && line.toLowerCase().contains("local interface:")){ //NOI18N
                    //example line: Local interface: pwxxx xxx.xxx.xxx.xx [vcid] up
                    
                    state = ParsingState.READING_LOCAL_INTERFACE_VCID_DETAIL;
                    
                    Matcher mtch = ptn.matcher( lineTokens[3].split("\\s")[0]);
                    String localIp = null;
                    if(mtch.find())
                        localIp = lineTokens[3];
                    entry.setLocalInterfaceIpDetail(localIp);

                }
                if (state == ParsingState.READING_LOCAL_INTERFACE_VCID_DETAIL && line.toLowerCase().contains("destination address:") && lineTokens.length == 9  &&  lineTokens[8].toLowerCase().equals("up")){ //NOI18N
                    //example line: Destination address: xxx.xxx.xxx.xx, VC ID: xxxx, VC status: up
                    //Whe two pseudowires are connected the vcid should replace with the one in the details not the got from the general list
                    
                    state = ParsingState.READING_OUTPUT_INTERFACE_VCID_DETAIL;
                    entry.setVcId(lineTokens[5].replace(",", ""));
                    entry.setDestinationIpDetail(lineTokens[2].replace(",", ""));
                }
                if (state == ParsingState.READING_OUTPUT_INTERFACE_VCID_DETAIL && line.toLowerCase().contains("output interface:")){ //NOI18N
                    //Output interface: gix/x/x.yyy, imposed label stack {0 24}
                    
                    entry.setOutputInterface(SyncUtil.normalizePortName(lineTokens[2].replace(",", "")));
                }
            }//end for
            state = ParsingState.END;
        }
        else{
            entry.setDestinationIpDetail(null);
            entry.setOutputInterface(null);
            entry.setLocalInterfaceDetail(null);
            entry.setLocalInterfaceIpDetail(null);
        }
            
        return entry;
    }
    
    /**
     * Parses the raw input of pseudowires related
     * @param mplsLinks The raw input that corresponds to the output of the command
     * @return The list of bridge domains in the given router (and inside, the related interfaces -VFI, service instances and BDI-)
     */
    public List<AbstractDataEntity> findRelatedPseudowires(List<AbstractDataEntity> mplsLinks){
        for (AbstractDataEntity pw : mplsLinks) {
            if(((MPLSLink)pw).getLocalInterface().toLowerCase().contains("pw")){
                String pwNumericPart = ((MPLSLink)pw).getLocalInterface().replace("pw", "").replace("\\s", "");
                for (AbstractDataEntity vc : mplsLinks) {
                    if(((MPLSLink)vc).getVcId().equals(pwNumericPart)){
                        ((MPLSLink)pw).setLocalInterfaceDetail(((MPLSLink)vc).getLocalInterface());
                        break;
                    }
                }
            }
        }//end for
        return mplsLinks;
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
         * after the headers
         */
        READING_INTERFACES,
        /**
         * In vcid detail looking for the local interface
         */
        READING_LOCAL_INTERFACE_VCID_DETAIL,
        /**
         * In vcid detail looking for the output interface
         */
        READING_VCID_DETAIL,
        /**
         * In vcid detail looking for the output interface
         */
        READING_OUTPUT_INTERFACE_VCID_DETAIL,
        /**
         * the end of the list of the interfaces associated to a MPLSLink
         */
        END
    }
}
