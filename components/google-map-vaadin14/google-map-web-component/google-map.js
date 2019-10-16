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
import {MapApi} from './google-map-api.js';
/**
 * `google-map`
 * &lt;google-map&gt; is a web component displays a map using Maps JavaScript API
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 * @extends PolymerElement
 * @summary Custom Element for Google Map
 */
class GoogleMap extends PolymerElement {
  
  static get is() {
    return 'google-map';
  }  
  
  static get template() {
    return html`
      <style>
        /* Always set the map height explicitly to define the size of the div
        * element that contains the map. */
        #map {
          height: 100%;
        }       
      </style>
      <div id="map"></div>
    `;
  }

  static get properties() {
    return {
      /**
       * The string contains your application API key. See https://developers.google.com/maps/documentation/javascript/get-api-key
       */
      apiKey: String
    };
  }
  /**
   * Display a map
   * @return {void}
   */
  initMap() {    
    const map = new google.maps.Map(this.shadowRoot.querySelector('#map'), {
      center: {lat: -34.397, lng: 150.644},
      zoom: 8
    });
  }
  /**
   * Loads the Maps JavaScript API
   * when the api is ready, it will call this.initMap()
   * @override
   * @return {void}
   */
  ready() {
    super.ready();
    const mapApi = new MapApi(this.apiKey);
    mapApi.load().then(() => {this.initMap()})
  }
}

window.customElements.define(GoogleMap.is, GoogleMap);
