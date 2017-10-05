/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neotropic.kuwaiba.sync.snmp;

import java.io.Serializable;
import java.util.Properties;
import javax.batch.api.chunk.ItemReader;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

/**
 * This reader will poll one by one the queued sync groups and retrieve the information declared in the polling definition and map it to a generic structure in memory 
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSnmpSyncReader implements ItemReader {
    @Inject
    private JobContext jobContext;
    
    
    @Override
    public void open(Serializable checkpoint) throws Exception {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        Properties jobParameters = jobOperator.getParameters(jobContext.getExecutionId());
        String resourceName = (String) jobParameters.get("payrollInputDataFileName");
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object readItem() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
