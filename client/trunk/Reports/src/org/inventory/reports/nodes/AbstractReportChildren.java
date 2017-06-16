/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package org.inventory.reports.nodes;

import java.util.Collections;
import org.inventory.communications.core.LocalReportLight;
import org.openide.nodes.Children;

/**
 * Root of all Children classes dealing with report nodes. The idea behind this class is that the behavior of all children classes to be similar.
 * If you want to update the list of children, just call the addNotify method, for example, and each implementation will do it in its own way
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractReportChildren extends Children.Keys<LocalReportLight> {
    @Override
    public abstract void addNotify();
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    } 
}