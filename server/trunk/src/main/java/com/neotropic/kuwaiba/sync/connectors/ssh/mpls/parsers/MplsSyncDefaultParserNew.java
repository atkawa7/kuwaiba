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
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for the output of the command "show l2vpn service all" 
 * in the Cisco ASR920, ASR1002, ASR1006, ME3600 router series
 * 
 Interface          Group       Encapsulation                   Prio  St  XC St
  ---------          -----       -------------                   ----  --  -----
VPWS name: HUAWEI-BRAZIL-LAPAZ, State: UP
  pw100002                       156.56.42.4:410(MPLS)         0     UP  UP   
  Fa0/1/1                        Fa0/1/1:10(Eth VLAN)            0     UP  UP   
VPWS name: HUAWEI-BRAZIL-LAPAZ, State: UP
  pw100003                       156.65.43.3:420(MPLS)         0     UP  UP   
  Fa0/1/2                        Fa0/1/2:10(Eth VLAN)            0     UP  UP   
 *  ...
 *  ...
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MplsSyncDefaultParserNew {
    //regex for ip addresses
    private final Pattern ptn = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    /**
     * Parses the raw input
     * @param input The raw input that corresponds to the output of the command
     * @return The list of bridge domains in the given router (and inside, the related interfaces -VFI, service instances and BDI-)
     */
    public List<AbstractDataEntity> parse(String input) {
                List<AbstractDataEntity> mplsTransportLinks = new ArrayList<>();
        if(input != null){
            String[] lines = input.split("\n");
            ParsingState state = ParsingState.START;
            MPLSLinkNew mplsLink = new MPLSLinkNew();
            for (String line : lines) {
                String[] lineTokens;
                //Processing data first we check that the state is up
                if(line.toLowerCase().contains("state")){ //NOI18N
                    lineTokens = line.trim().split(",");
                    if(lineTokens.length >= 2 && lineTokens[1].contains(":") && lineTokens[1].split(":")[1].toLowerCase().contains("up")){
                        state = ParsingState.READING_SERVICE_NAME;
                        if(lineTokens[0].contains(":")){ 
                            //we get the technology or service type from the first line token VPWS or VPLS
                            mplsLink.setServiceType(lineTokens[0].split(":")[0].split("\\s")[0].trim());
                            //we get the service name from the: "something : servicename"
                            mplsLink.setServiceName(lineTokens[0].split(":")[1].trim());
                        }
                    }
                }//end service name
                if (line.split("\\s{2,}").length >= 5){ //reading interfaces
                    lineTokens = line.trim().split("\\s{2,}"); 
                    int cursor = lineTokens.length == 6 ? 1 : 0; //some times there are 6 columns instead of 5 because of group column, so we need to move the column cursor one space

                    if(lineTokens[1 + cursor].contains(":") //the ip address could be in column 2 or 3 
                            && lineTokens[3 + cursor].trim().toLowerCase().equals("up")  && lineTokens[4 + cursor].trim().toLowerCase().equals("up")){
                        
                        if(state != ParsingState.READING_EXTERNALEQUIPMENT)
                            state = ParsingState.READING_INTERFACES;
                        //we must chek if is the ip address line or is the interface line
                        //ip address: something xx.x.x.x:yyyy(somethig)  .... more things states, etc....
                        //interface: interface   real_interface_to_connect ... more things states, etc....
                        Matcher mtch = ptn.matcher(lineTokens[1 + cursor].split(":")[0]);
                        if(mtch.find()){ //it is possible that there is no interface only two ip addresses
                            if(mplsLink.getDestinyIPAddress() == null){
                                //PW to PW
                                //VPWS name: AFR-EP2P-FTW-TEV-128k-001, State: UP
                                //  pw713                          185.35.140.3:713(MPLS)          0     UP  UP <-- reading this line
                                //  pw714                          41.223.133.74:714(MPLS)         0     UP  UP 
                                //to logical o physical interface
                                //VPWS name: AFR-EP2P-FTW-TEV-128k-001, State: UP
                                // pw100003                       185.35.140.3:2420(MPLS)         0     UP  UP  <-- reading this line
                                // Fa0/1/2                        Fa0/1/2:10(Eth VLAN)            0     UP  UP 
                                
                                //this if is for vfis when it comes in second line 
                                if(state == ParsingState.READING_EXTERNALEQUIPMENT){
                                    if(lineTokens[0].toLowerCase().contains("pw"))
                                        mplsLink.setPseudowireB(lineTokens[0]);
                                    mplsLink.setDestinyIPAddressB(lineTokens[1 + cursor].split(":")[0]); //we get the ip address
                                    mplsLink.setVcidB(lineTokens[1 + cursor].split(":")[1].substring(0, lineTokens[1 + cursor].split(":")[1].indexOf("(")));//we get vcid after : and before (
                                }
                                else{
                                    if(lineTokens[0].toLowerCase().contains("pw"))//NOI18N
                                        mplsLink.setPseudowireA(lineTokens[0]); //we get the local pesudowire need it if both are pws
                                    mplsLink.setDestinyIPAddress(lineTokens[1 + cursor].split(":")[0]); //we get the ip address
                                    mplsLink.setVcidA(lineTokens[1 + cursor].split(":")[1].substring(0, lineTokens[1 + cursor].split(":")[1].indexOf("(")));//we get vcid after : and before (
                                }
                                
                                state = (mplsLink.getLocalPhysicalInterface() != null || mplsLink.getLocalVirtualInterface() != null || state == ParsingState.READING_EXTERNALEQUIPMENT) ? ParsingState.END : ParsingState.READING_PWS_LINE;
                            }
                            else{
                                //VPWS name: AFR-EP2P-FTW-TEV-128k-001, State: UP
                                //  pw713                          185.35.140.3:713(MPLS)          0     UP  UP   
                                //  pw714                          41.223.133.74:714(MPLS)         0     UP  UP <-- reading this line
                                if(lineTokens[0].toLowerCase().contains("pw"))//NOI18N
                                    mplsLink.setPseudowireB(lineTokens[0]);
                                    
                                mplsLink.setVcidB(lineTokens[1 + cursor].split(":")[1].substring(0, lineTokens[1 + cursor].split(":")[1].indexOf("(")));
                                mplsLink.setDestinyIPAddressB(lineTokens[1 + cursor].split(":")[0]);
                               
                                state = mplsLink.getDestinyIPAddress() != null ? ParsingState.END : ParsingState.READING_PWS_LINE_B;
                            } 
                        }//end if is an ip
                        //the second linke token is a interface so we extract the service instand after the :
                        else{//it should be an interface
                            //to logical o physical interface
                            //VPWS name: AFR-EP2P-FTW-TEV-128k-001, State: UP
                            // pw100003                       185.35.140.3:2420(MPLS)         0     UP  UP 
                            // Fa0/1/2                        Fa0/1/2:10(Eth VLAN)            0     UP  UP <-- reading this line
                            state = mplsLink.getDestinyIPAddress() != null ? ParsingState.END : ParsingState.READING_SPECIFIC_INTERFACE;
                            //there are some cases like Te2/1/0.411:411
                            if(lineTokens[1 + cursor].split(":")[0].contains("."))
                                mplsLink.setLocalPhysicalInterface(SyncUtil.normalizePortName(lineTokens[1 + cursor].split("\\.")[0]));
                            else
                                mplsLink.setLocalPhysicalInterface(SyncUtil.normalizePortName(lineTokens[1 + cursor].split(":")[0]));
                            mplsLink.setLocalVirtualInterface(lineTokens[1 + cursor].split(":")[1].substring(0, lineTokens[1 + cursor].split(":")[1].indexOf("("))); //<- this is where the mpls link is really e=connected
                        }
                    }//end if normal interfaces
                    //VPLS name: CPE-MGMT-NTP-FIBER, State: UP
                    //pw100039                       CPE-MGMT-NTP-FIBER(VFI)         0     UP  UP  <-- reading this line, we only check that the service name and VFI has the same name
                    //pw100040           core_pw     197.255.193.20:3903(MPLS)       0     UP  UP  
                    else if(mplsLink.getServiceName() != null && lineTokens[1].toLowerCase().contains("vfi")){
                        if(mplsLink.getDestinyIPAddress() != null){
                            //mplsLink.setDestinyIPAddressB(mplsLink.getDestinyIPAddress());
                            mplsLink.setPseudowireB(lineTokens[0]);
                            state = ParsingState.END;
                        }else{
                            mplsLink.setPseudowireA(lineTokens[0]);
                            state = ParsingState.READING_EXTERNALEQUIPMENT;
                        }
                        mplsLink.setVfiName(SyncUtil.normalizeVfiName(lineTokens[1]));
                    }
                }//end if enough line tokens
                
                //saving mplslink
                if(state == ParsingState.END)
                {
                    mplsTransportLinks.add(mplsLink);
                    mplsLink = new MPLSLinkNew();
                    state = ParsingState.START;
                }
                //end saving mplslink
            }//end for
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
         * after the first line when the state is checked
         */
        READING_SERVICE_NAME,
        /**
         * after the first line when the state is checked
         */
        READING_INTERFACES,
        /**
         * checking the line where the pw and the ip address is 
         */
        READING_PWS_LINE,
        /**
         * checking the second line where the pw and the ip address is 
         */
        READING_PWS_LINE_B,
        /**
         * checking the line after a pw with only the service name, and now we are reading an ip an vcid, it is presumed as an ExternalEquipment
         */
        READING_EXTERNALEQUIPMENT,
        /**
         * checking the line where the specific output interface is
         */
        READING_SPECIFIC_INTERFACE,
        /**
         * the end of the list of the interfaces associated to a MPLSLinkNew
         */
        END
    }
}
