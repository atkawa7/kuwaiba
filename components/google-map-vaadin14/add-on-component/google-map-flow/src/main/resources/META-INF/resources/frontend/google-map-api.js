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

/**
 * Class that load the Maps Java Script API
 */
class MapApi {
    /**
     * 
     * @param {string} key 
     */
    constructor(key) {
        /** 
         * The string contains your application API key. See https://developers.google.com/maps/documentation/javascript/get-api-key
         * @type {string} 
         * @protected
         */
        this._key = key;
        if (!window._mapApi) {
            /** @type {string} */
            this.callback = '_mapApi.ready';
            window._mapApi = this;
            window._mapApi.ready = this.ready.bind(this);
        }
    }
    /**
     * Gets your application API key
     * @return {string}
     */
    get key() {
        return this._key;
    }
    /**
     * Sets your application API key
     * @param {string} key
     * @return {void}
     */
    set key(key) {
        this._key = key;
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
                    script.src = 'https://maps.googleapis.com/maps/api/js?key=' + this.key + '&callback=' + this.callback;
                    script.async = true;
                    script.defer = true;
                    document.body.append(script);
                } else {
                    this.resolve();
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