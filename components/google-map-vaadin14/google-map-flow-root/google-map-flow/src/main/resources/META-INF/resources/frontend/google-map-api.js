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
import * as Constants from './google-map-constants.js';
/**
 * Class to load the Maps Java Script API
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class MapApi {
    /**
     * 
     * @param {string} apiKey 
     * @param {string} clientId
     * @param {string} libraries libraries=drawing,geometry,places,visualization
     */
    constructor(apiKey, clientId, libraries) {
        if (!apiKey) {
            console.warn('google-maps must include an API key');
        }
        /** 
         * The string contains your application API key. See https://developers.google.com/maps/documentation/javascript/get-api-key
         * @type {string} 
         * @protected
         */
        this._apiKey = apiKey;
        /**
         * Specifies a client Id
         * @type {string}
         * @protected
         */
        this._clientId = clientId;
        /**
         * The string contains the additional libraries
         * @type {string}
         * @protected
         */
        this._libraries = libraries;

        this._paramApiKey = '';
        this._paramClientId = '';
        this._paramLibraries = '';

        if (this._apiKey && !this._clientId) {
            this._paramApiKey = 'key=' + this._apiKey;             
        }
        if (this._clientId) {
            this._paramClientId = 'client=' + this._clientId;
        }
        if (this._libraries) {            
            this._paramLibraries = '&libraries=' + this._libraries;
        }

        if (!window._mapApi) {
            /** @type {string} */
            this.callback = '_mapApi.ready';
            this._paramCallback = '&callback=' + this.callback;
            window._mapApi = this;
            window._mapApi.ready = this.ready.bind(this);
        }
    }
    /**
     * Loads the Maps JavaScript API
     * when the api is ready, it will call this.ready()
     * @return
     */
    load() {
        if (!this.promise) {
            this.promise = new Promise(resolve => {
                this.resolve = resolve;

                if (typeof window.google === 'undefined') {
                    const script = document.createElement('script');
                    script.src = 'https://maps.googleapis.com/maps/api/js?' + this._paramApiKey + this._paramClientId + '&v=' + Constants.googleMapsJavaScriptAPIVersion + this._paramCallback + this._paramLibraries;
                    script.async = true;
                    script.defer = true;
                    document.body.append(script);
                } else {
                    this.ready();
                }
            });
        }
        return this.promise;
    }
    /**
     * 
     * @return {void}
     */
    ready() {
        if (this.resolve) {
            this.resolve();
        }
    }
}

export { MapApi };