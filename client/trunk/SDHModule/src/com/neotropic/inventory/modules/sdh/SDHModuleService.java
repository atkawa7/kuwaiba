/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.sdh;

import com.neotropic.inventory.modules.sdh.scene.SDHModuleScene;
import org.inventory.communications.core.views.LocalObjectViewLight;

/**
 * The service associated to this module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SDHModuleService {
    private LocalObjectViewLight view;
    private SDHModuleScene scene;

    public SDHModuleService(SDHModuleScene scene) {
        this.scene = scene;
    }
    
    public void setView(LocalObjectViewLight view) {
        this.view = view;
    }
    
    public LocalObjectViewLight getView() {
        return view;
    }
    
    public void saveCurrentView() {
    }
    
    public void openView(LocalObjectViewLight aView) {
    }
    
    public void deleteView(LocalObjectViewLight aView) {
    }
}
