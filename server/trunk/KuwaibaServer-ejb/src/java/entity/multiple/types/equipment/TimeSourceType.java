/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package entity.multiple.types.equipment;

import entity.multiple.GenericType;
import javax.persistence.Entity;

/**
 * A time source type such as simple NTP server (a single computer), GPS-based time source or atomic clock
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class TimeSourceType extends GenericType {
    /**
     * As seen in http://www.symmetricom.com/products/gps-solutions/telecom-primary-reference-sources/
     */
    protected Boolean isPrimaryReferenceSource = false;
    /**
     * Depending on whether a time source depends on another (i.e. GPS powered TS, which depends on the GPS signal)
     * or it's independent (an atomic clock) this attribute should be set
     */
    protected Boolean isDependent = false;
    /**
     * As seen in http://www.symmetricom.com/resources/glossary/network-timing-faq/#ntp14
     */
    protected Integer stratum;

    public Boolean isDependent() {
        return isDependent;
    }

    public void setAsDependent(Boolean isDependent) {
        this.isDependent = isDependent;
    }

    public Boolean isPrimaryReferenceSource() {
        return isPrimaryReferenceSource;
    }

    public void setAsPrimaryReferenceSource(Boolean isPrimaryReferenceSource) {
        this.isPrimaryReferenceSource = isPrimaryReferenceSource;
    }

    public Integer getStratum() {
        return stratum;
    }

    public void setStratum(Integer stratum) {
        this.stratum = stratum;
    }
}
