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

/**
 * `my-element`
 * my-element
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 */
class MxGraphPoint extends PolymerElement {
  static get template() {
    return html`<p>point</p>`;
  }

  static get properties() {
    return {
      uuid: {
        type: String,
        value: null
      },
      point: {
        type: Object,
        notify: true
      },

      cell: {
        type: Object,
        observer: '_cellChanged'
      },
      x: {
        type: Number,
        value: 0
      },
      y: {
        type: Number,
        value: 0
      },
      parent: {
        type : Object,
        value : null
      }
    };
  }

  _attachDom(dom) { this.appendChild(dom); }

  constructor() {
    super();
    console.log("Constructor: MxGraphPoint")
  }

  ready() {
    super.ready();
    console.log("Ready: MxGraphPoint")
  }

  initMxGraph() {


  }

  attached() {
    // If element is added back to DOM, put it back on the map.
    if (this.point) {
     // this.cell.setMap(this.graph);
    }
              console.log("ATTACHED");

  }

  _cellChanged() {
    console.log("FUNCTION _cellChanged");
    // Marker will be rebuilt, so disconnect existing one from old map and listeners.

    if (this.cell && this.cell instanceof mxCell) {
      this._cellReady();
    }


  }

  _cellReady() {
    console.log("FUNCTION _cellReady");
    this.parent.graph.getModel().beginUpdate();
    try {
      if (this.cell) {
        console.log("CREATING POINT");
        this.point = new mxPoint(this.x, this.y);
        this.point.uuid = "idprueba";
        var geometryOfEdge =  this.parent.graph.getModel().getGeometry(this.cell);
        geometryOfEdge = geometryOfEdge.clone();
        if (!geometryOfEdge.points ) {
          geometryOfEdge.points = [];
        } 
         geometryOfEdge.points.push(this.point);
         geometryOfEdge.relative = false;
         this.parent.graph.getModel().setGeometry(this.cell, geometryOfEdge);
      }    
     
    }
    finally {
      // Updates the display
    this.parent.graph.getModel().endUpdate();
    }
    //this.setupDragHandler_();
    //this._forwardEvent('click');

  }



}

window.customElements.define('mx-graph-point', MxGraphPoint);
