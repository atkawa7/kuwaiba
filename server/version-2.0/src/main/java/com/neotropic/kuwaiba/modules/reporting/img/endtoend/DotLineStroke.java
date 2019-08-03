/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package com.neotropic.kuwaiba.modules.reporting.img.endtoend;

import java.awt.BasicStroke;

/**
 * A stroke to represent a dot line
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class DotLineStroke extends BasicStroke {
    
    public DotLineStroke(int width) {
        super(width, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_BEVEL, 0, new float[] { 4, 5 }, 0);
    }    
}
