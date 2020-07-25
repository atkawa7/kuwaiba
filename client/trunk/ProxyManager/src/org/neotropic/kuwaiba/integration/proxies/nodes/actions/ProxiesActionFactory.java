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

package org.neotropic.kuwaiba.integration.proxies.nodes.actions;

import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * A factory that provides single instances of the available actions in this module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ProxiesActionFactory {
    /**
     * The add configuration variable action
     */
    private static AddProxyAction addProxyAction;
    /**
     * The delete configuration variable action
     */
    private static DeleteProxyAction deleteProxyAction;
    /**
     * The add configuration variables pool action
     */
    private static AddProxyPoolAction addProxyPoolAction;
    /**
     * The deletes configuration variables pool action
     */
    private static DeleteProxyPoolAction deleteProxyPoolAction;
    /**
     * Relates a proxy to an existing project.
     */
    private static RelateProxyToProjectAction relateProxyToProjectAction;
    /**
     * Releases a proxy from an existing project.
     */
    private static ReleaseProxyFromProjectAction releaseProxyFromProject;
    
    /**
     * Returns a singleton instance of AddProxyAction.
     * @return The singleton instance.
     */
    public static GenericInventoryAction getAddProxyAction() {
        return addProxyAction == null ? addProxyAction = new AddProxyAction() : addProxyAction;
    }
    
    /**
     * Returns a singleton instance of DeleteProxyAction.
     * @return The singleton instance.
     */
    public static GenericInventoryAction getDeleteProxyAction() {
        return deleteProxyAction == null ? deleteProxyAction = new DeleteProxyAction() : deleteProxyAction;
    }
    
    /**
     * Returns a singleton instance of AddProxyPoolAction.
     * @return The singleton instance.
     */
    public static GenericInventoryAction getAddProxyPoolAction() {
        return addProxyPoolAction == null ? addProxyPoolAction = new AddProxyPoolAction() : addProxyPoolAction;
    }
    
    /**
     * Returns a singleton instance of DeleteProxyPoolAction.
     * @return The singleton instance
     */
    public static GenericInventoryAction getDeleteProxyPoolAction() {
        return deleteProxyPoolAction == null ? deleteProxyPoolAction = new DeleteProxyPoolAction() : deleteProxyPoolAction;
    }
    
    public static GenericInventoryAction getRelateProxyToProjectAction() {
        return relateProxyToProjectAction == null ? relateProxyToProjectAction = new RelateProxyToProjectAction() : relateProxyToProjectAction;
    }
    
    public static GenericInventoryAction getReleaseProxyFromProject() {
        return releaseProxyFromProject == null ? releaseProxyFromProject = new ReleaseProxyFromProjectAction() : releaseProxyFromProject;
    }
}
