/*
 *  Copyright 2010 - 2013 Neotropic SAS <contact@neotropic.co>
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

package org.inventory.sync;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.communications.CommunicationsStub;

/**
 * This class provides the business logic to the associated component
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SyncService implements ActionListener{

    private SyncTopComponent stc;
    private String fileName;
    
    private byte[] logResults;
    private String logFileName;
    private byte[] wrongLinesResults;
    private String wrongLinesFileName;
    
    private static final String LOGS = "kuwaiba_load_data.log_";
    private static final String ERRORS = "kuwaiba_load_data.errors_";

    public SyncService(SyncTopComponent stc) {
        this.stc = stc;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
    }
    
    public void loadFile(byte[] file){
        CommunicationsStub com = CommunicationsStub.getInstance();
        //fileName = com.loadDataFromFile(choosenFile);
    }
    
    public void downloadErrors(){
        logFileName = ERRORS+fileName;
        CommunicationsStub com = CommunicationsStub.getInstance();
        //wrongLinesResults = com.downloadErrors(fileName);
    }    
    
    public void downloadLog(){
        wrongLinesFileName = LOGS+fileName;
        CommunicationsStub com = CommunicationsStub.getInstance();
        //logResults = com.downloadLog(fileName);
    }

    public byte[] getLogResults() {
        return logResults;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public byte[] getWrongLinesResults() {
        return wrongLinesResults;
    }

    public String getWrongLinesFileName() {
        return wrongLinesFileName;
    }
    
    
}
