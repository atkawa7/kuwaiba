/*
 * Copyright (c) 2019 Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>} - initial API and implementation and/or initial documentation
 */
package org.inventory.core.services.api.importdb;

import java.util.List;
import javax.swing.SwingWorker;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class CreateClassesTask extends SwingWorker<Void, Void> {

    private List<LocalClassMetadata> roots;

    public CreateClassesTask(List<LocalClassMetadata> roots) {
        this.roots = roots;
    }

    @Override
    protected Void doInBackground() throws Exception {
        CommunicationsStub stub = CommunicationsStub.getInstance();
        //varibales to show progress       
        long totalBytesRead = 0;
        int percentCompleted = 0;
        long fileSize = roots.size() - 2;
        
        for (LocalClassMetadata root : roots) {
            long newClass = stub.createClassMetadata(
                    root.getClassName(),
                    root.getDisplayName(),
                    root.getDescription(),
                    root.getParentName(),
                    root.isCustom(),
                    root.isCountable(),
                    root.getColor().getRGB(),
                    root.isAbstract(),
                    root.isInDesign()
            );
            
            totalBytesRead ++;
            percentCompleted = (int) (totalBytesRead * 100 / fileSize);
            setProgress(percentCompleted);
            
        }
        return null;
    }

}
