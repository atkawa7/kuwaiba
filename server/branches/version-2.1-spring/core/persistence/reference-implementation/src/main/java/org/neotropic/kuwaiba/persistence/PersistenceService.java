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
package org.neotropic.kuwaiba.persistence;

import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.persistence.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service manages the life cycle of the connection to the database and the rest of 
 * services that connect to the database.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class PersistenceService {
    /**
     * General purpose configuration variables.
     */
    private Properties generalProperties;
    /**
     * The properties (call them configuration variables if you will) used in the 
     * {@link ConnectionManager}.
     */
    private Properties connectionProperties;
    /**
     * The properties (call them configuration variables if you will) used in the 
     * {@link MetadataEntityManager}.
     */
    private Properties metadataProperties;
    /**
     * The properties (call them configuration variables if you will) used in the 
     * {@link ApplicationEntityManager}.
     */
    private Properties applicationProperties;
    /**
     * The properties (call them configuration variables if you will) used in the 
     * {@link BusinessEntityManager}.
     */
    private Properties businessProperties;
    /**
     * The current state of the service (basically, it tells what is the status of the connection to the data base)
     */
    private EXECUTION_STATE state;
    /**
     * The connection manager to be initialized.
     */
    @Autowired
    private ConnectionManager connectionManager;
    /**
     * The metadata entity manager to be initialized.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * The application entity manager to be initialized.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * The business entity manager to be initialized.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    
    public Properties getMetadataProperties() {
        return metadataProperties;
    }

    public void setMetadataProperties(Properties metadataProperties) {
        this.metadataProperties = metadataProperties;
    }

    public Properties getApplicationProperties() {
        return applicationProperties;
    }

    public void setApplicationProperties(Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Properties getBusinessProperties() {
        return businessProperties;
    }

    public void setBusinessProperties(Properties businessProperties) {
        this.businessProperties = businessProperties;
    }

    public Properties getGeneralProperties() {
        return generalProperties;
    }

    public void setGeneralProperties(Properties generalProperties) {
        this.generalProperties = generalProperties;
    }

    public Properties getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }
    
    private PersistenceService() {
        state = EXECUTION_STATE.STOPPED;
    }
     
    public void start()throws IllegalStateException {
        if (state == EXECUTION_STATE.RUNNING)
            throw new IllegalStateException("Persistence Service can not be started because is already running");
        
        if (connectionProperties == null || metadataProperties == null || applicationProperties == null || 
                businessProperties == null || generalProperties == null)
            throw new IllegalStateException("Not all configuration properties have been set");
        
        if (System.getSecurityManager() == null && (boolean)generalProperties.get("enableSecurityManager")) {
            System.setSecurityManager(new SecurityManager());
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] Security Manager enabled", Calendar.getInstance().getTime()));
        }
        
        try {
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] Starting Persistence Service version %s", 
                            Calendar.getInstance().getTime(), Constants.PERSISTENCE_SERVICE_VERSION));
         
            connectionManager.setConfiguration(connectionProperties); //NOI18N
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] Establishing connection to the database...", Calendar.getInstance().getTime()));
            connectionManager.openConnection();
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] Connection established", Calendar.getInstance().getTime()));
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] %s", Calendar.getInstance().getTime(), connectionManager.getConnectionDetails()));
            
            mem.setConfiguration(metadataProperties);
            mem.initCache();
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] %s", Calendar.getInstance().getTime(), "Metadata Entity Manager initialized successfully"));
            
            
            aem.setConfiguration(applicationProperties);
            aem.initCache();
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] %s", Calendar.getInstance().getTime(), "Application Entity Manager initialized successfully"));
            
            bem.setConfiguration(businessProperties);
            bem.initCache();
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] %s", Calendar.getInstance().getTime(), "Business Entity Manager initialized successfully"));
            
            Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("[KUWAIBA] [%s] Persistence Service is up and running", Calendar.getInstance().getTime()));
            
            state = EXECUTION_STATE.RUNNING;
        } catch(Exception ex) {
            if (connectionManager != null)
                connectionManager.closeConnection();
            Logger.getLogger(PersistenceService.class.getName()).log(Level.SEVERE,
                    String.format("[KUWAIBA] [%s] Persistence Service could not be started: %s", Calendar.getInstance().getTime(), ex));
            state = EXECUTION_STATE.STOPPED;
        }
    }
    public void stop() throws IllegalStateException {
        if (state == EXECUTION_STATE.STOPPED)
            throw new IllegalStateException("Persistence Service can not be stopped because is not running");
        
        Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO,String.format("[KUWAIBA] [%s] Closing connection...", Calendar.getInstance().getTime()));
        connectionManager.closeConnection();
        Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO,String.format("[KUWAIBA] [%s] Connection closed", Calendar.getInstance().getTime()));
        state = EXECUTION_STATE.STOPPED;
    }
    public void restart(){
        stop();
        start();
    }
        
    public EXECUTION_STATE getState() {
        return state;
    }
    
    public enum EXECUTION_STATE {
        STOPPED,
        RUNNING,
        PAUSED
    }
}
