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
package com.neotropic.inventory.modules.cpe.nodes.actions;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CpeManagerActionFactory {
    private static CreateEvlanRootPoolAction createEvlanRootPoolAction;
    private static DeleteEvlanRootPoolAction deleteEvlanRootPoolAction;
    private static CreateEvlanAction createEvlanAction;
    private static DeleteEvlanAction deleteEvlanAction;
    
    public static CreateEvlanRootPoolAction getCretateEvlanRootPoolAction() {
        createEvlanRootPoolAction = createEvlanRootPoolAction != null ? createEvlanRootPoolAction : new CreateEvlanRootPoolAction();
        return createEvlanRootPoolAction;
    }
    
    public static DeleteEvlanRootPoolAction getDeleteEvlanRootPoolAction() {
        deleteEvlanRootPoolAction = deleteEvlanRootPoolAction != null ? deleteEvlanRootPoolAction : new DeleteEvlanRootPoolAction();
        return deleteEvlanRootPoolAction;
    }
    
    public static CreateEvlanAction getCreateEvlanAction() {
        createEvlanAction = createEvlanAction != null ? createEvlanAction : new CreateEvlanAction();
        return createEvlanAction;
    }
    
    public static DeleteEvlanAction getDeleteEvlanAction() {
        deleteEvlanAction = deleteEvlanAction != null ? deleteEvlanAction : new DeleteEvlanAction();
        return deleteEvlanAction;
    }
}
