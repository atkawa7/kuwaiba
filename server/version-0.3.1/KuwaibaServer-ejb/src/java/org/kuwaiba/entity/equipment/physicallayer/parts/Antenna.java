/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.kuwaiba.entity.equipment.physicallayer.parts;

import org.kuwaiba.entity.multiple.companies.CorporateCustomer;
import org.kuwaiba.entity.multiple.types.parts.AntennaType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Represents an antenna. For satellite antennas use SatelliteAntenna
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Antenna extends GenericPart {
    @ManyToOne
    protected AntennaType type;
    protected Float gain;
    /**
     * Operation frequency
     */
    protected String band;
    /**
     * Which segment is the antenna located on
     */
    protected Integer segment;
    /**
     * Which edge is the antenna located on
     */
    protected Integer edge;
    /**
     *In degrees
     */
    protected Float orientation;
    @ManyToOne
    protected CorporateCustomer owner; //In case the antenna belongs to a customer

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public Integer getEdge() {
        return edge;
    }

    public void setEdge(Integer edge) {
        this.edge = edge;
    }

    public Float getGain() {
        return gain;
    }

    public void setGain(Float gain) {
        this.gain = gain;
    }

    public Float getOrientation() {
        return orientation;
    }

    public void setOrientation(Float orientation) {
        this.orientation = orientation;
    }

    public CorporateCustomer getOwner() {
        return owner;
    }

    public void setOwner(CorporateCustomer owner) {
        this.owner = owner;
    }

    public Integer getSegment() {
        return segment;
    }

    public void setSegment(Integer segment) {
        this.segment = segment;
    }

    public AntennaType getType() {
        return type;
    }

    public void setType(AntennaType type) {
        this.type = type;
    }

}
