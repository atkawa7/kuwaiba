/** 
@license
Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

class GoogleMapPoint extends PolymerElement {
  
    static get is() {
      return 'google-map-point';
    }  
    
    static get template() {
      return html`
        <style>
            :host {
            display: none;
            }
        </style>

        <slot></slot>
      `;
    }
  
    static get properties() {
      return {
        /**
         * The initial map center latitude coordinate ranges between [-90, 90] degrees 
         */
        lat: {
          type: Number,
          value: 2.4573831
        },
        /**
         * The initial map center longitude coordinate ranges between [-180, 180] degress
         */
        lng: {
          type: Number,
          value: -76.6699746
        },

        ind: {
          type: Number,
          value: -1
        }
      };
    }
    
    /**
     * Loads the Maps JavaScript API
     * when the api is ready, it will call this.initMap()
     * @override
     * @return {void}
     */
    ready() {
      super.ready(); 
      console.log("point" + this.ind);
      this.holi = 'holi >>> <<<' + this.ind;
    }

    
  }
  
  window.customElements.define(GoogleMapPoint.is, GoogleMapPoint);