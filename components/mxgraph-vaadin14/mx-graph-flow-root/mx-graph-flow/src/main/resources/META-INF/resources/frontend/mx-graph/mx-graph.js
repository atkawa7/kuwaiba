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
import {mxGraphApi, mxClient, mxUtils, mxGraph, mxRubberband, mxEvent} from './mx-graph-api.js';
import {  } from "./mx-graph-cell";
/**
 * `my-element`
 * my-element
 *
 * @customElement
 * @polymer
 * @demo demo/index.html
 */
class MxGraph extends PolymerElement {
  static get template() {
    return html`
      <style>
        :host {
          display: block;
        }
      </style>
      <h2>Hello [[prop1]]!</h2>
      <div id="graphContainer" 
      style="overflow:hidden;width:321px;height:241px;background:url('editors/images/grid.gif')">
      </div>
    `;
  }
  
  static get properties() {
    return {
      prop1: {
        type: String,
        value: 'mx-graph',
      },

      graph: {
        type: Object,
         value: null
      } ,

      cells: {
        type: Array,
        value: function() { return []; }   
      }
    };
  }

  _attachDom(dom) { this.appendChild(dom); }

  ready() {
    super.ready();
    new mxGraphApi().load().then(() => {this.initMxGraph()})
    console.log("adding Observer")
    this._cellObserver = new MutationObserver(this.addCell.bind(this));
    this._cellObserver.observe(this, { childList: true});
  }

  initMxGraph() {
        // Checks if the browser is supported
        //console.log('sadasd' + mxClient);
        if (!mxClient.isBrowserSupported())
        {
          // Displays an error message if the browser is not supported.
          mxUtils.error('Browser is not supported!', 200, false);
        }
        else
        {
          // Disables the built-in context menu
          mxEvent.disableContextMenu(this.$.graphContainer);
          
          // Creates the graph inside the given container
          this.graph = new mxGraph(this.$.graphContainer);
          // Enables rubberband selection
          new mxRubberband(this.graph);
          this.graph.setConnectable(true);
          
          // Gets the default parent for inserting new cells. This
          // is normally the first child of the root (ie. layer 0).
       
                  
          // Adds cells to the model in a single step
          
          
          var _this = this
          this.graph.addListener(mxEvent.CLICK, function (sender, evt) {
					var cell = evt.getProperty('cell');
					console.log("CLICK")
					console.log(evt)

					if (cell != null && _this.graph.getModel().isEdge(cell)) {
                                                _this.fireClickEdge();
						console.log("CLICK on EDGE")
						/*
						graph.getModel().beginUpdate();
						var sourceLabel = new mxCell('LABEL PRUEBA', new mxGeometry(-0.7, 0, 0, 0), 'resizable=0;editable=1;labelBackgroundColor=white;fontSize=10;strokeColor=#69b630;strokeWidth=3;');
						sourceLabel.geometry.relative = true;
						sourceLabel.setConnectable(false);
						sourceLabel.vertex = true;
						cell.insert(sourceLabel);
						graph.getModel().endUpdate();*/
						
					}
				});
        }
  }
  
  addCell(mutations){
    console.log("addcell Method")
   
    mutations.forEach(function(mutation) {
    console.log("MUTATION TYPE" + mutation.type);
    var node  = mutation.addedNodes[0];
    if(node) {
        if (node.localName === "mx-graph-cell") {
              node.graph = this.graph;
              this.push('cells', node); 
              
//                  this.push('markers', {name: "marker "+this.markers.length});

         }
     }
    }, this); 
     
}

  fireClickEdge(){
    this.dispatchEvent(new CustomEvent('click-edge', {detail: {kicked: true}}));
  }
}

window.customElements.define('mx-graph', MxGraph);
