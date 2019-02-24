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
package org.inventory.customization.classhierarchy.importdb;

import java.util.HashMap;
import java.util.List;
import javax.swing.SwingWorker;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;

/**
 * This class work as a thread, store a metadata list inside database, also
 * count the progress of this process
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class CreateClassesTask extends SwingWorker<Void, Void> {

    private final List<LocalClassMetadata> roots;
    private final HashMap<String, String> errors;

    /**
     * default constructor
     *
     * @param roots
     */
    public CreateClassesTask(List<LocalClassMetadata> roots) {
        this.roots = roots;
        this.errors = new HashMap<>();
    }

    /**
     * Main thread, it will be execute in background Process to save into
     * database
     *
     * @return
     * @throws Exception
     */
    @Override
    protected Void doInBackground() throws Exception {
        CommunicationsStub stub = CommunicationsStub.getInstance();

        //variables to show progress       
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
            if (newClass == -1) {
                getErrors().put(root.getClassName(), stub.getError());
            }

            totalBytesRead++;
            percentCompleted = (int) (totalBytesRead * 100 / fileSize);
            setProgress(percentCompleted);
        }
        return null;
    }

    /**
     * @return the errors
     */
    public HashMap<String, String> getErrors() {
        return errors;
    }

}
