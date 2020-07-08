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
import {mxGraphApiLoader} from './mx-graph-api-loader.js';
/**
 * `my-element`
 * my-element
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 */
class MxGraphApi extends PolymerElement {
    static get template() {
        return html``;
    }

    static get properties() {
        return {
            
        }
    }

    constructor() {
        super();
    }

    _attachDom(dom) {
        this.appendChild(dom);
    }

    connectedCallback() {
        super.connectedCallback();
        // …
        console.log("CONECTEDCALLBACK")
    }

    ready() {
        super.ready();
        console.log("READY")
        new mxGraphApiLoader().load().then(() => {
            this.onMxGraphLoad();
        });
    }

    //called then the mxGraph library has been loaded and initialize the grap object
    onMxGraphLoad() {
        window.mxgraphLoaded = true;
        console.log("MX GRAPH LIB LOADED")
    }
    

}

window.customElements.define('mx-graph-api', MxGraphApi);
