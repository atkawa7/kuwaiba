/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.inventory.communications;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;

/**
 * Manage the thread pool and create communications stub callable methods
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CommunicationsStubTask {    
    private static CommunicationsStubTask instance;
    private static ExecutorService fixedThreadPool;
    
    private CommunicationsStubTask() {
    }
    
    public static CommunicationsStubTask getInstance() {
        return instance == null ? instance = new CommunicationsStubTask() : instance;
    }
    
    public static ExecutorService getFixedThreadPool() {
        return fixedThreadPool == null ? fixedThreadPool = Executors.newFixedThreadPool(10) : fixedThreadPool;
    }
    
    public Callable<List<LocalObjectLight>> getObjectChildrenCallable(final String oid, final String className) {
        return new Callable<List<LocalObjectLight>>() {

            @Override
            public List<LocalObjectLight> call() throws Exception {
                return CommunicationsStub.getInstance().getObjectChildrenAsync(oid, className);
            }
        };
    }
    
    public Callable<LocalObject> getObjectInfoCallable(final String objectClass, final String oid) {
        return new Callable<LocalObject>() {

            @Override
            public LocalObject call() throws Exception {
                return CommunicationsStub.getInstance().getObjectInfoAsync(objectClass, oid);
            }
        };
    }
    
    public Callable<HashMap<String, LocalObjectLight[]>> getSpecialAttributesCallable(final String objectClass, final String objectId) {
        return new Callable<HashMap<String, LocalObjectLight[]>>() {
            @Override
            public HashMap<String, LocalObjectLight[]> call() throws Exception {
                return CommunicationsStub.getInstance().getSpecialAttributesAsync(objectClass, objectId);
            }
        };
    }
}
