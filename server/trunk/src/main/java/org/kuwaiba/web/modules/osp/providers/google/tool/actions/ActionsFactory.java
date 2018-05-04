/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.providers.google.tool.actions;

/**
 * Actions factory for Outside Plant Tooled Component
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ActionsFactory {
    private static NewOspViewAction newOspViewAction;
    private static OpenOspViewAction openOspViewAction;
    private static SaveOspViewAction saveOspViewAction;
    private static DeleteOspViewAction deleteOspViewAction;
    private static FilterByAction filterByAction;
    
    public static NewOspViewAction createNewOspViewAction(String caption, String resourceId) {
        if (newOspViewAction == null)
            newOspViewAction = new NewOspViewAction(caption, resourceId);
        return newOspViewAction;
    }
    
    public static OpenOspViewAction createOpenOspViewAction(String caption, String resourceId) {
        if (openOspViewAction == null)
            openOspViewAction = new OpenOspViewAction(caption, resourceId);
        return openOspViewAction;
    }
    
    public static SaveOspViewAction createSaveOspViewAction(String caption, String resourceId) {
        if (saveOspViewAction == null)
            saveOspViewAction = new SaveOspViewAction(caption, resourceId);
        return saveOspViewAction;
    }
    
    public static DeleteOspViewAction createDeleteOspViewAction(String caption, String resourceId) {
        if (deleteOspViewAction == null)
            deleteOspViewAction = new DeleteOspViewAction(caption, resourceId);
        return deleteOspViewAction;
    }
    
    public static FilterByAction createFilterByAction(String caption, String resourceId) {
        if (filterByAction == null)
            filterByAction = new FilterByAction(caption, resourceId);
        return filterByAction;
    }
}
