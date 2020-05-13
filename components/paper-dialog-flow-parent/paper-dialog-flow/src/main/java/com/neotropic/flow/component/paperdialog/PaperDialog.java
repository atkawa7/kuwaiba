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
package com.neotropic.flow.component.paperdialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * <paper-dialog>
 *  <h2></h2>
 *  <!--Content-->
 *  <div class="buttons"></div>
 * </paper-dialog>
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("paper-dialog")
@JsModule("@polymer/paper-dialog/paper-dialog.js")
@NpmPackage(value = "@polymer/paper-dialog", version = "^3.0.1")
public class PaperDialog extends Component implements HasComponents, HasSize {
    public PaperDialog() {
    }
    public void dialogConfirm(Component component) {
        component.getElement().setAttribute(Attribute.DIALOG_CONFIRM.attribute(), true);
    }
    public void dialogDismiss(Component component) {
        component.getElement().setAttribute(Attribute.DIALOG_DISMISS.attribute(), true);
    }
    public void open() {
        getElement().executeJs("this.open()"); //NOI18N
    }
    public void positionTarget(Component positionTarget) {
        getElement().executeJs("this.positionTarget = $0", positionTarget); //NOI18N
    }
    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        getElement().setProperty(Property.HORIZONTAL_ALIGN, horizontalAlign.align());
    }
    public void setModal(boolean modal) {
        getElement().setProperty(Property.MODAL, modal);
    }
    public void setNoOverlap(boolean noOverlap) {
        getElement().setProperty(Property.NO_OVERLAP, noOverlap);
    }
    public void setVerticalAlign(VerticalAlign verticalAlign) {
        getElement().setProperty(Property.VERTICAL_ALIGN, verticalAlign.align());
    }
    public void setMargin(boolean margin) {
        if (!margin)
            getElement().getStyle().set(Attribute.MARGIN.attribute(), "0");
    }
    public class Property {
        public static final String HORIZONTAL_ALIGN = "horizontalAlign"; //NOI18N
        public static final String MODAL = "modal"; //NOI18N
        public static final String NO_OVERLAP = "noOverlap"; //NOI18N
        public static final String VERTICAL_ALIGN = "verticalAlign"; //NOI18N
    }
    public enum HorizontalAlign {
        LEFT("left"); //NOI18N
        private final String align;
        private HorizontalAlign(String align) {
            this.align = align;
        }
        public String align() {
            return align;
        }
    }
    public enum VerticalAlign {
        TOP("top"); //NOI18N
        private final String align;
        private VerticalAlign(String align) {
            this.align = align;
        }
        public String align() {
            return align;
        }
    }
    private enum Attribute {
        DIALOG_DISMISS("dialog-dismiss"), //NOI18N
        DIALOG_CONFIRM("dialog-confirm"), //NOI18N
        MARGIN("margin"); //NOI18N
        private final String attribute;
        private Attribute(String attribute) {
            this.attribute = attribute;
        }
        public String attribute() {
            return attribute;
        }
    }
    public enum Clazz {
        BUTTONS("buttons"); //NOI18N
        private final String clazz;
        private Clazz(String clazz) {
            this.clazz = clazz;
        }
        public String clazz() {
            return clazz;
        }
    }
}
