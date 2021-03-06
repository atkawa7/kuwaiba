/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Named;
import javax.servlet.http.Part;
import org.kuwaiba.beans.ToolsBeanRemote;
import org.kuwaiba.web.misc.JsfUtil;
import org.kuwaiba.util.patches.GenericPatch;
/**
 * Controls how all the actions in the administration console are executed
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Named("Tools")
@SessionScoped
public class ToolsController implements Serializable {
    
    private Part dataModelFile;
    /**
     * The patchesIds to be executed
     */
    private String[] patchesIds;
    
    private DataModel dataModelPatches;
    
    private List<GenericPatch> patches;
    
    @EJB
    private ToolsBeanRemote tbr;
    
    public String prepareResetDatabase() {
        return "ResetDatabase"; //NOI18N
    }
    
    public String prepareApplyPatches(){
        return "ApplyPatches"; //NOI18N
    }
    
    public String resetDatabase () {
        if (dataModelFile == null || dataModelFile.getSize() == 0)
            JsfUtil.addErrorMessage("The data model file seems to be empty or corrupted");
        else {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (InputStream dataModelFileInputStream = dataModelFile.getInputStream()) {
                    while (true) {
                        int aByte = dataModelFileInputStream.read();
                        if (aByte == -1)
                            break;
                        baos.write(aByte);
                    }
                }
                
                tbr.loadDataModel(baos.toByteArray());
                JsfUtil.addSuccessMessage("A new schema has been created. The database can be used now.");
            } catch (Exception e) {
                JsfUtil.addErrorMessage("An unexpected error occurred while resetting the database: " + e.getMessage());
            }
        }
        return "index";
    }
    
    public String createResetAdminAccount() {
        try {
            tbr.resetAdmin();
            JsfUtil.addSuccessMessage("Password for user \"admin\" has been set to \"kuwaiba\"");
        } catch (Exception e) {
            JsfUtil.addErrorMessage("An unexpected error occurred while resetting admin password: " + e.getMessage());
        }
        return "index";
    }
    
    public String applyPatches() {
        try {
            
            if (patchesIds == null || patchesIds.length == 0) 
                JsfUtil.addSuccessMessage(String.format("No patches were selected."));
            else {                
                List<GenericPatch> patchesToApply = new ArrayList();
                
                for (GenericPatch patch : patches) {
                    for (String patchesId : patchesIds) {
                        if (patch.getId().equals(patchesId)) {
                            patchesToApply.add(patch);
                            break;
                        }
                    }
                }
                String[] executedPatchesMessages = tbr.executePatches(patchesToApply);

                for (int i = 0; i < executedPatchesMessages.length; i++) {
                    if (executedPatchesMessages[i] == null)
                        JsfUtil.addSuccessMessage(String.format("Patch %s applied sucessfully", i + 1));
                    else
                        JsfUtil.addErrorMessage(String.format("Patch %s exit with error: %s", i + 1, executedPatchesMessages[i]));
                }
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage("An unexpected error occurred while applying the patches: " + e.getMessage());
        }
        return "index";
    }

    public Part getDataModelFile() {
        return dataModelFile;
    }

    public void setDataModelFile(Part dataModelFile) {
        this.dataModelFile = dataModelFile;
    }

    public String[] getPatchesIds() {
        return patchesIds;
    }

    public void setPatchesIds(String[] patchesIds) {
        this.patchesIds = patchesIds;
    }
    
    public DataModel getPatches() {
        if (patches == null)
            patches = tbr.getPatches();
        return dataModelPatches == null ? dataModelPatches = new ListDataModel(patches) : dataModelPatches;
    }
}
