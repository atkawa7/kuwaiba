 /*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.custom.wizards.physicalconnection;

import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * Physical Connection Configuration
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@Deprecated
public class PhysicalConnectionConfiguration {
    public static final int WIZARD_TYPE_CONTAINER = 1;
    public static final int WIZARD_TYPE_LINK = 2;
    
    public static final String VALIDATOR_PHYSICAL_NODE = "physicalNode";
    public static final String VALIDATOR_PHYSICAL_ENDPOINT = "physicalEndpoint";
    
        /**
     * Generic classes
     */
    public static final String CLASS_GENERICCONNECTION="GenericConnection";

    //TODO: Gotta send this to a config file
    public static final String CLASS_WIRECONTAINER="WireContainer";
    public static final String CLASS_WIRELESSCONTAINER="WirelessContainer";

    /**
     * Physical connection classes
     */
    public static final String CLASS_ELECTRICALLINK = "ElectricalLink";
    public static final String CLASS_OPTICALLINK = "OpticalLink";
    public static final String CLASS_WIRELESSLINK = "RadioLink";
     public static final String CLASS_POWERLINK = "PowerLink";

    /**
     * Physical connection type classes
     */
    public static final String CLASS_ELECTRICALLINKTYPE = "ElectricalLinkType";
    public static final String CLASS_OPTICALLINKTYPE = "OpticalLinkType";
    public static final String CLASS_WIRELESSLINKTYPE = "WirelessLinkType";
    public static final String CLASS_POWERLINKTYPE = "PowerLinkType";

    /**
     * Physical container type classes
     */
    public static final String CLASS_WIRECONTAINERTYPE = "WireContainerType";
    public static final String CLASS_WIRELESSCONTAINERTYPE = "WirelessContainerType";
    
    private String connectionClass;
    private String connectionTypeClass;
    private long typeOid;
    private int wizardType;
    private String caption;
    private String strokeColor;
    private double strokeOpacity;
    private int strokeWeight;
    private int numChildren = 0;
    private String portType;
    
    private RemoteObjectLight endpointA;
    private RemoteObjectLight endpointB;
    
////    private PhysicalConnectionWizard physicalConnectionWizard;
    
    public PhysicalConnectionConfiguration() {
////        this.physicalConnectionWizard = physicalConnectionWizard;
        
    }
    
    public RemoteObjectLight getEndpointA() {
        return endpointA;
    }
    
    public void setEndpointA(RemoteObjectLight endpointA) {
        this.endpointA = endpointA;
    }
    
    public RemoteObjectLight getEndpointB() {
        return endpointB;
    }
        
    public void setEndpointB(RemoteObjectLight endpointB) {
        this.endpointB = endpointB;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public double getStrokeOpacity() {
        return strokeOpacity;
    }

    public void setStrokeOpacity(double strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
    }

    public int getStrokeWeight() {
        return strokeWeight;
    }

    public void setStrokeWeight(int strokeWeight) {
        this.strokeWeight = strokeWeight;
    }

    public int getWizardType() {
        return wizardType;
    }

    public void setWizardType(int wizardType) {
        this.wizardType = wizardType;
    }

    public long getTypeOid() {
        return typeOid;
    }

    public void setTypeOid(long typeOid) {
        this.typeOid = typeOid;
    }

    public String getConnectionClass() {
        return connectionClass;
    }

    public void setConnectionClass(String connectionClass) {
        this.connectionClass = connectionClass;
    }

    public String getConnectionTypeClass() {
        return connectionTypeClass;
    }

    public void setConnectionTypeClass(String connectionTypeClass) {
        this.connectionTypeClass = connectionTypeClass;
    }

    public int getNumChildren() {
        return numChildren;
    }

    public void setNumChildren(int numChildren) {
        this.numChildren = numChildren;
    }

    public String getPortType() {
        return portType;
    }

    public void setPortType(String portType) {
        this.portType = portType;
    }
    
    public void chooseWizardType(String connectionClass) {
        setStrokeOpacity(1); // User can provideded this values
        setStrokeWeight(3);  // User can provideded this values
        
        if (connectionClass != null) {            
            switch(connectionClass) {
                case "Wire Container": //NOI18N
                    setStrokeColor("red"); //NOI18N
                    setConnectionClass(PhysicalConnectionConfiguration.CLASS_WIRECONTAINER);
                    setConnectionTypeClass(PhysicalConnectionConfiguration.CLASS_WIRECONTAINERTYPE);
                    setWizardType(PhysicalConnectionConfiguration.WIZARD_TYPE_CONTAINER);
                break;
                case "Wireless Container": //NOI18N
                    setStrokeColor("blue"); //NOI18N
                    setConnectionClass(PhysicalConnectionConfiguration.CLASS_WIRELESSCONTAINER);
                    setConnectionTypeClass(PhysicalConnectionConfiguration.CLASS_WIRELESSCONTAINERTYPE);
                    setWizardType(PhysicalConnectionConfiguration.WIZARD_TYPE_CONTAINER);
                break;
                case "Electrical Link": //NOI18N
                    setStrokeColor("orange"); //NOI18N
                    setConnectionClass(PhysicalConnectionConfiguration.CLASS_ELECTRICALLINK);
                    setConnectionTypeClass(PhysicalConnectionConfiguration.CLASS_ELECTRICALLINKTYPE);
                    setWizardType(PhysicalConnectionConfiguration.WIZARD_TYPE_LINK);
                break;
                case "Optical Link": //NOI18N
                    setStrokeColor("green"); //NOI18N
                    setConnectionClass(PhysicalConnectionConfiguration.CLASS_OPTICALLINK);
                    setConnectionTypeClass(PhysicalConnectionConfiguration.CLASS_OPTICALLINKTYPE);
                    setWizardType(PhysicalConnectionConfiguration.WIZARD_TYPE_LINK);
                    break;
                case "Wireless Link": //NOI18N
                    setStrokeColor("magenta"); //NOI18N
                    setConnectionClass(PhysicalConnectionConfiguration.CLASS_WIRELESSLINK);
                    setConnectionTypeClass(PhysicalConnectionConfiguration.CLASS_WIRELESSLINKTYPE);
                    setWizardType(PhysicalConnectionConfiguration.WIZARD_TYPE_LINK);
                    break;
                case "Power Link":
                    setStrokeColor("yellow"); //NOI18N
                    setConnectionClass(PhysicalConnectionConfiguration.CLASS_POWERLINK);
                    setConnectionTypeClass(PhysicalConnectionConfiguration.CLASS_POWERLINKTYPE);
                    setWizardType(PhysicalConnectionConfiguration.WIZARD_TYPE_LINK);
                default:
                    break;
            }
        }
        else { // default configuration
            setStrokeColor("green"); //NOI18N
            setConnectionClass(PhysicalConnectionConfiguration.CLASS_OPTICALLINK);
            setConnectionTypeClass(PhysicalConnectionConfiguration.CLASS_OPTICALLINKTYPE);
            setWizardType(PhysicalConnectionConfiguration.WIZARD_TYPE_LINK);
        }
    }
}
