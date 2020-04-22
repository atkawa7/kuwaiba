/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.vaadin.component.demo.services;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class CountryService {
    public class Country {
        public String name;
        public String abbreviation;
        
        public Country(String name, String abbreviation) {
            this.name = name;
            this.abbreviation = abbreviation;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getAbbreviation() {
            return abbreviation;
        }
        
        public void setAbbreviation(String abbreviation) {
            this.abbreviation = abbreviation;
        }
    }
    public List<Country> getCountries() {
        List<Country> countries = new ArrayList();
        countries.add(new Country("Argentina", "ARG"));
        countries.add(new Country("Bolivia", "BOL"));
        countries.add(new Country("Brazil", "BRA"));
        countries.add(new Country("Chile", "CHL"));
        countries.add(new Country("Colombia", "COL"));
        countries.add(new Country("Ecuador", "ECU"));
        countries.add(new Country("Falkland Islands", "FLK"));
        countries.add(new Country("French Guiana", "GUF"));
        countries.add(new Country("Guyana", "GUY"));
        countries.add(new Country("Paraguay", "PRY"));
        countries.add(new Country("Peru", "PER"));
        countries.add(new Country("Suriname", "SUR"));
        countries.add(new Country("Uruguay", "URY"));
        countries.add(new Country("Venezuela", "VEN"));
        countries.add(new Country("Anguilla", "AIA"));
        countries.add(new Country("Aruba", "ABW"));
        countries.add(new Country("Bonaire, Sint Eustatius and Saba", "BES"));
        countries.add(new Country("Cuba", "CUB"));
        countries.add(new Country("Curaçao", "CUW"));
        countries.add(new Country("Dominica", "DMA"));
        countries.add(new Country("Dominican Republic", "DOM"));
        countries.add(new Country("El Salvador", "SLV"));
        countries.add(new Country("Greenland", "GRL"));
        countries.add(new Country("Grenada", "GRD"));
        countries.add(new Country("Guadeloupe", "GLP"));
        countries.add(new Country("Guatemala", "GTM"));        
        countries.add(new Country("Haiti", "HTI"));
        countries.add(new Country("Honduras", "HND"));
        countries.add(new Country("Jamaica", "JAM"));
        countries.add(new Country("Martinique", "MTQ"));
        countries.add(new Country("Mexico", "MEX"));
        countries.add(new Country("Montserrat", "MSR"));
        countries.add(new Country("Netherlands Antilles", "ANT"));
        countries.add(new Country("Nicaragua", "NIC"));
        countries.add(new Country("Panama", "PAN"));
        countries.add(new Country("Puerto Rico", "PRI"));
        countries.add(new Country("Saint Barthelemy", "BLM"));
        countries.add(new Country("Saint Kitts and Nevis", "KNA"));
        countries.add(new Country("Saint Lucia", "LCA"));
        countries.add(new Country("Saint Martin", "MAF"));
        countries.add(new Country("Saint Pierre and Miquelon", "SPM"));
        countries.add(new Country("Saint Vincent and the Grenadines", "VCT"));
        countries.add(new Country("Sint Maarten (Netherlands)", "SXM"));
        countries.add(new Country("Trinidad and Tobago", "TTO"));
        countries.add(new Country("Turks and Caicos Islands", "TCA"));
        countries.add(new Country("United States Minor Outlying Islands", "UMI"));
        countries.add(new Country("United States of America", "USA"));
        countries.add(new Country("United States Virgin Islands", "VIR"));
        return countries;
    }
}
