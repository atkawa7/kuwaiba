/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neotropic.kuwaiba.sync.snmp;

import java.io.Serializable;
import java.util.List;
import javax.batch.api.chunk.ItemWriter;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

/**
 * Executes the actions after having analyzed the differences between the information in the SNMP agents and the information in Kuwaiba. These actions 
 * were defined in the ItemProcessor
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSnmpSyncWriter implements ItemWriter {
    @Inject
    private JobContext jobContext;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeItems(List<Object> items) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
