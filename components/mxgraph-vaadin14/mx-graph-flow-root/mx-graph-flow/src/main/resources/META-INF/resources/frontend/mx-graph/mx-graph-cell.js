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

import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { mxGraphApi, mxClient, mxUtils, mxGraph, mxRubberband, mxEvent } from './mx-graph-api.js';
/**
 * `my-element`
 * my-element
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 */
class MxGraphCell extends PolymerElement {
  static get template() {
    return html`
      <style>
        :host {
          display: block;
        }
      </style>
      
    `;
  }

  static get properties() {
    return {
      prop1: {
        type: String,
        value: 'mx-graph',
      },
      cell: {
        type: Object,
        notify: true
      },

      graph: {
        type: Object,
        observer: '_graphChanged'
      }
    };
  }

//  _attachDom(dom) { this.appendChild(dom); }

  ready() {
    super.ready();
  }

  initMxGraph() {


  }

  attached() {
    // If element is added back to DOM, put it back on the map.
    if (this.cell) {
     // this.cell.setMap(this.map);
    }
              console.log("ATTACHED");

  }

  _graphChanged() {
    console.log("FUNCTION _graphChanged");
    // Marker will be rebuilt, so disconnect existing one from old map and listeners.

    if (this.graph && this.graph instanceof mxGraph) {
      this._graphReady();
    }


  }

  _graphReady() {
    console.log("FUNCTION _graphReady");
    var parent = this.graph.getDefaultParent();
    this.graph.getModel().beginUpdate();
    try {
      this.cell = this.graph.insertVertex(parent, null, 'Hello,', 20, 20, 80, 30);
    }
    finally {
      // Updates the display
    this.graph.getModel().endUpdate();
    }
    //this.setupDragHandler_();
    //this._forwardEvent('click');

  }

  fireClickEdge() {
    this.dispatchEvent(new CustomEvent('click-edge', { detail: { kicked: true } }));
  }


}

window.customElements.define('mx-graph-cell', MxGraphCell);
