/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.api;

import com.neotropic.flow.component.mxgraph.MxConstants;

/**
 * Set of constants used in the Outside Plant Module
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MapConstants {
    /**
     * XML Outside Plant View attribute lat
     */
    public static final String ATTR_LAT = "lat"; //NOI18N
    /**
     * XML Outside Plant View attribute lon
     */
    public static final String ATTR_LON = "lon"; //NOI18N
    /**
     * View Edge property controlPoints
     */
    public static final String PROPERTY_CONTROL_POINTS = "controlPoints"; //NOI18N
    /**
     * View node/edge property editable
     */
    public static final String PROPERTY_CELL_EDITABLE = MxConstants.STYLE_EDITABLE;
    /**
     * View node/edge property movable
     */
    public static final String PROPERTY_CELL_MOVABLE = MxConstants.STYLE_MOVABLE;
    
    public static final String CELL_ID = "cellId";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String POINTS = "points";
    public static final String FROM_CLIENT_ADD_NODE = "fromClientAddNode";
}
