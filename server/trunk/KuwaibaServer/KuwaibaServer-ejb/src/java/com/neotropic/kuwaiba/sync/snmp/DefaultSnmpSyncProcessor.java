/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neotropic.kuwaiba.sync.snmp;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

/**
 * Contains the logic that finds the differences between the polled device and the element in the inventory
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSnmpSyncProcessor implements ItemProcessor {
    @Inject
    private JobContext jobContext;
    
    @Override
    public Object processItem(Object item) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
