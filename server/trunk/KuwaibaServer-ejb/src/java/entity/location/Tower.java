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

package entity.location;

import entity.multiple.types.parts.TowerType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * Represents a telecommunications tower
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Tower extends GenericLocation implements Serializable {
    protected Float height;
    protected Integer segments;
    protected Integer edges;
    @ManyToOne
    protected TowerType type;          //Mast, electrical, normal, etc
    protected Boolean hasSupportWires; //Support wires to help the tower or mast
                                       //to stand against the wind
    public Integer getEdges() {
        return edges;
    }

    public void setEdges(Integer edges) {
        this.edges = edges;
    }

    public Boolean getHasSupportWires() {
        return hasSupportWires;
    }

    public void setHasSupportWires(Boolean hasSupportWires) {
        this.hasSupportWires = hasSupportWires;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Integer getSegments() {
        return segments;
    }

    public void setSegments(Integer segments) {
        this.segments = segments;
    }

    public TowerType getType() {
        return type;
    }

    public void setType(TowerType type) {
        this.type = type;
    }
}
