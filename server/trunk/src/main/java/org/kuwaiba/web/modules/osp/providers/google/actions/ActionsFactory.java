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
 */
package org.kuwaiba.web.modules.osp.providers.google.actions;

import org.kuwaiba.apis.web.gui.actions.AbstractAction;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ActionsFactory {
    private static NewObjectAction newObjectAction;
    private static DeleteMarkerNodeAction deleteMarkerNodeAction;
    private static DeletePolygonAction deletePolygonAction;
    private static DeletePhysicalConnectionAction deletePhysicalConnectionAction;
    private static ConnectLinksAction connectLinksAction;
    private static ShowObjectIdAction showObjectIdAction;
    private static MeasureConnectionDistanceAction measureConnectionDistanceAction;
    
    public static NewObjectAction createNewObjectAction() {
        if (newObjectAction == null)
            newObjectAction = new NewObjectAction();
        return newObjectAction;
    }
    
    public static DeleteMarkerNodeAction createDeleteMarkerNodeAction() {
        if (deleteMarkerNodeAction == null)
            deleteMarkerNodeAction = new DeleteMarkerNodeAction();
        return deleteMarkerNodeAction;
    }
    
    public static AbstractAction createDeletePolygonAction() {
        if (deletePolygonAction == null)
            deletePolygonAction = new DeletePolygonAction();
        return deletePolygonAction;            
    }
        
    public static DeletePhysicalConnectionAction createDeletePhysicalConnectionAction(String caption) {
        if (deletePhysicalConnectionAction == null)
            deletePhysicalConnectionAction = new DeletePhysicalConnectionAction();
        deletePhysicalConnectionAction.setCaption(caption);
        return deletePhysicalConnectionAction;
    }
        
    public static ConnectLinksAction createConnectLinksAction() {
        if (connectLinksAction == null)
            connectLinksAction = new ConnectLinksAction();
        return connectLinksAction;
    }
    
    public static ShowObjectIdAction createShowObjectIdAction() {
        if (showObjectIdAction == null)
            showObjectIdAction = new ShowObjectIdAction();
        return showObjectIdAction;
    }
        
    public static MeasureConnectionDistanceAction createMeasureConnectionDistanceAction() {
        if (measureConnectionDistanceAction == null)
            measureConnectionDistanceAction = new MeasureConnectionDistanceAction();
        return measureConnectionDistanceAction;
    }
}
