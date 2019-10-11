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
    };
  }

  _attachDom(dom) { this.appendChild(dom); }

  ready() {
    super.ready();
    new mxGraphApi().load().then(() => {this.initMxGraph()})
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
          var graph = new mxGraph(this.$.graphContainer);
          // Enables rubberband selection
          new mxRubberband(graph);
          
          // Gets the default parent for inserting new cells. This
          // is normally the first child of the root (ie. layer 0).
          var parent = graph.getDefaultParent();
                  
          // Adds cells to the model in a single step
          graph.getModel().beginUpdate();
          try
          {
            var v1 = graph.insertVertex(parent, null, 'Hello,', 20, 20, 80, 30);
            var v2 = graph.insertVertex(parent, null, 'World!', 200, 150, 80, 30);
            var e1 = graph.insertEdge(parent, null, '', v1, v2);
          }
          finally
          {
            // Updates the display
            graph.getModel().endUpdate();
          }
          
          var _this = this
          graph.addListener(mxEvent.CLICK, function (sender, evt) {
					var cell = evt.getProperty('cell');
					console.log("CLICK")
					console.log(evt)

					if (cell != null && graph.getModel().isEdge(cell)) {
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
  
  fireClickEdge(){
    this.dispatchEvent(new CustomEvent('click-edge', {detail: {kicked: true}}));
  }
}

window.customElements.define('mx-graph', MxGraph);
