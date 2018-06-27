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
package com.neotropic.vaadin.lienzo.client;

import com.vaadin.shared.AbstractComponentState;

/**
 * 
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public class LienzoComponentState extends AbstractComponentState {
    // State is directly readable in the client after it is set in server
    
    public boolean enableConnectionTool = false;    
    
    public double labelsFontSize = 10;
    public double labelsPaddingTop = 16;
    public double labelsPaddingLeft = 16;
    // Scene background
    public String backgroundUrl = null;
    public double backgroundY = 0;
    public double backgroundX = 0;
}