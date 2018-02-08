/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.communications.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Local wrapper of BusinessRule
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalBusinessRule {
    /**
     * Object of class #{appliesTo} can't be related to object of class B
     */
    public static int TYPE_RELATIONSHIP_BY_CLASS = 1;
    /**
     * Object of class #{appliesTo} can only be related to object B with this set of relationship names (defined in the rule constraints)
     */
    public static int TYPE_RELATIONSHIP_BY_RELATIONSHIP_NAME = 2;
    /**
     * Object of class #{appliesTo} can only be related to object B if ObjectA.attributeA = Z and ObjectB.attributeB = Y
     */
    public static int TYPE_RELATIONSHIP_BY_ATTRIBUTE_VALUE = 3;
    /**
     * Object of class #{appliesTo} can only be child of Object B if Object B has its attribute C set to X (as defined by a rule constraint)
     */
    public static int TYPE_STANDARD_CONTAINMENT = 5;
    /**
     * Object of class #{appliesTo} can only be special child of Object B if Object B has its attribute C set to X (as defined by a rule constraint)
     */
    public static int TYPE_SPECIAL_CONTAINMENT = 6;
    /**
     * See #{scope} for details
     */
    public static int SCOPE_GLOBAL = 1;
    /**
     * Rule id
     */
    private long id;
    /**
     * Rule name
    */
    private String name;
    /**
     * Rule description
     **/
    private String description;
    /**
     * Class this rule applies to (you will need to duplicate a rule to apply it to different classes of different class hierarchy branches). Abstract classes are allowed
     */
    private String appliesTo;
    /**
     * Rule definition may change with new versions, so we keep this value to correctly migrate instances when needed
     */
    private String version;
    /**
     * Rule type. See TYPE_* for possible values
     */
    private int type;
    /**
     * Not yet sure how this will be useful, but I'll leave it here for now. The general idea is that not all rules must be applicable globally, it may depend on some 
     */
    private int scope;
    /**
     * The list of constraints that define the rule. These constrains are the actual logic that should be checked against before to perform certain action
     */
    private List<LocalBusinessRuleConstraint> constraints;

    public LocalBusinessRule(long id, String name, String description, String appliesTo, int type, int scope, String version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.appliesTo = appliesTo;
        this.type = type;
        this.scope = scope;
        this.version = version;
        this.constraints = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<LocalBusinessRuleConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<LocalBusinessRuleConstraint> constraints) {
        this.constraints = constraints;
    }
    
    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 41 * hash + Objects.hashCode(this.name);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocalBusinessRule && ((LocalBusinessRule)obj).getId() == id;
    }
}