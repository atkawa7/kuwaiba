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
public class SyncService implements ActionListener, Runnable{

    private String fileName;
    private byte[] logResults;
    private int fileType;
    private int commitSize;
    private byte[] file;
            
    
    public SyncService(byte[] file, int commitSize, int fileType) {
        fileName = "";
        this.file = file;
        this.commitSize = commitSize;
        this.fileType = fileType;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
    }
    
    public boolean loadFile(){
        if(!fileName.isEmpty())
            return true;
        else
            return false;
    }
   
    public void downloadLog(){
        CommunicationsStub com = CommunicationsStub.getInstance();
        logResults = com.downloadLog(fileName);
    }

    public byte[] getLogFile() {
        return logResults;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void run() {
        CommunicationsStub com = CommunicationsStub.getInstance();
        fileName =  com.loadDataFromFile(file, commitSize, fileType);
    }
 
}