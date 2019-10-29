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
import {mxGraphApi, mxClient, mxUtils, mxGraph, mxRubberband, mxEvent, mxEdgeHandler} from './mx-graph-api.js';

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
      style="overflow:hidden;width:[[width]];height:[[height]];background:url([[grid]])">
      </div>
      <slot></slot>
    `;
  }
  
  static get properties() {
    return {
        // mxGraph object 
      graph: {
        type: Object,
         value: null
      } ,
      //array of polymer objects of type mx-graph-cell
      cells: {
        type: Array,
        value: function() { return []; }   
      },
      // background image path
      grid: {
        type: String
      } ,
      width: {
        type: String,
        value: '400px',
      },
      height: {
        type: String,
        value: '400px',
      }
    }
  }

  constructor() {
    super();
  
  }

  _attachDom(dom) { this.appendChild(dom); }

  connectedCallback() {
    super.connectedCallback();
    // …
    console.log("CONECTEDCALLBACK")
    new mxGraphApi().load().then(() => {this.initMxGraph()})
  }


  ready() {
    super.ready();   
  }

 //called then the mxGraph library has been loaded and initialize the grap object
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
          //this.graph.setConnectable(true);
          this.graph.setAllowDanglingEdges(false);


          //enable adding and removing control points. 
          mxEdgeHandler.prototype.addEnabled = true;
	  mxEdgeHandler.prototype.removeEnabled = true;
          
          
          var _this = this;
          // here add handles for general click events in the graph
          this.graph.addListener(mxEvent.CLICK, function (sender, evt) {
              var cell = evt.getProperty('cell');
              console.log("CLICK")
              console.log(evt)

              if (cell != null && _this.graph.getModel().isEdge(cell)) {

                var cellObject = _this.getCellObjectById(cell.id);
                cellObject.fireClickEdge();
                console.log("CLICK on EDGE")					
              } 
          });

          // Called when any cell is moved
          this.graph.addListener(mxEvent.CELLS_MOVED, function (sender, evt) {
            var cellsMoved = evt.getProperty('cells');
            var dx = evt.getProperty('dx');
            var dy = evt.getProperty('dy');
            console.log("CELLS_MOVED")
            console.log(evt)

            if(cellsMoved) {

              cellsMoved.forEach(function(cellMoved) {
                if (_this.graph.getModel().isVertex(cellMoved)) {

                   var cellObject = _this.getCellObjectById(cellMoved.id);
               
                   if (cellObject) {
                      
                      cellObject.x += dx; 
                      cellObject.y += dy;                    
                      
                    }
      
                  console.log("VERTEX WITH ID " + cellMoved.id + " MOVED")	
                  
                } else if (_this.graph.getModel().isEdge(cellMoved)) {
                    
                    var cellObject = _this.getCellObjectById(cellMoved.id);
                    
                    if (cellObject) {
                
                    cellObject.points = JSON.stringify(cellMoved.geometry.points);
                
                    }
                }
              });

            }           
        });

        //allow editing in edges labels
        mxGraph.prototype.isCellEditable = function(	cell	){
          return true;
        }
        //Handler for labelChanged events fired when some label was edited.
        var labelChanged = mxGraph.prototype.labelChanged;
            mxGraph.prototype.labelChanged = function (cell, value, evt) {
                labelChanged.apply(this, arguments);
                // if the cell is an edge label
                if (_this.graph.getModel().isEdge(cell.parent)) {
                    
                    var cellObject = _this.getCellObjectById(cell.parent.id);
                    
                    if (cellObject) {

                       if(cell.id == cellObject.cellSourceLabel.id) {
                          cellObject.sourceLabel = value; 
                       }
                       if(cell.id == cellObject.cellTargetLabel.id) {
                          cellObject.targetLabel = value; 
                       }

                    }

                } else {  // the cell is a vertex or an edge
                    var cellObject = _this.getCellObjectById(cell.id);

                    if (cellObject) {

                        cellObject.label = value;

                    }

                }
                console.log("LABELCHANGED");
//                console.log(cell);
//                console.log(value);
            }

        // The method  isAddPointEvent is overwritten, to perform some previous action by adding a control point
        mxEdgeHandler.prototype.isAddPointEvent = function (evt) {
          console.log("ADD CONTROL POINT EVENT")
//          console.log(evt)
          if (evt.shiftKey) {
            return true
          }
          return false;
        }
         // The method  isRemovePointEvent is overwritten, to perform some previous action by removing a control point
        mxEdgeHandler.prototype.isRemovePointEvent = function (evt) {
          console.log("REMOVE CONTROL POINT EVENT")
//          console.log(evt)
          if (evt.shiftKey) {
            return true
          }
          return false;
        }
        // The method  isRemovePointEvent is overwritten, to update the points in the respective PolymerElement object.
        var mxChangePoints = mxEdgeHandler.prototype.changePoints;
        mxEdgeHandler.prototype.changePoints = function( edge, points, clone){
          console.log("CHANGEPOINTS EVENT")
          console.log(edge)
          console.log(points)
          console.log(clone)
          if(edge && _this.graph.getModel().isEdge(edge)) {
            
             var cellObject = _this.getCellObjectById(edge.id);
               
                if (cellObject) {
                
                cellObject.points = JSON.stringify(points);
                
              }

          }
          mxChangePoints.apply(this,arguments);
        }

        // The method  isRemovePointEvent is addPointAt, to update the points in the respective PolymerElement object, when a point is added,

        var addPointAt = mxEdgeHandler.prototype.addPointAt;
          mxEdgeHandler.prototype.addPointAt = function (state, x, y) {
            addPointAt.apply(this, arguments);

            var cell = state.cell;
            console.log("addPointAt EVENT")
//            console.log(state)
//            console.log(x)
//            console.log(y)

            if (cell) {

               var cellObject = _this.getCellObjectById(cell.id);
               
                if (cellObject) {

                  cellObject.points = JSON.stringify(cell.geometry.points);
                  
                }

            }
          }

          // The method  removePoint is addPointAt, to update the points in the respective PolymerElement object, when a point is removed,

          var removePoint = mxEdgeHandler.prototype.removePoint;
          mxEdgeHandler.prototype.removePoint = function (state, index) {
            removePoint.apply(this, arguments);
            
            var cell = state.cell;
            console.log("removePoint EVENT")
//            console.log(state)
//            console.log(index)
       
            if (cell) {

              var cellObject = _this.getCellObjectById(cell.id);
                if (cellObject) {

                  cellObject.points = JSON.stringify(cell.geometry.points);
                  
                }

            }
          }
        
//        console.log("adding Observer")
          this._cellObserver = new MutationObserver(this.addCell.bind(this));
          this._cellObserver.observe(this, { childList: true});
        }
  };
  
   // Get the polymer object that represents the cell with the porvided idCell. 
    getCellObjectById(idCell) {
        var cell;
        this.cells.forEach(function (cellObject) {
            if (cellObject.cell.id == idCell) {

                cell = cellObject;
                             
            }

        },this);
        return cell;
    };
  
  // fired when some children tag is added.
  addCell(mutations){
    console.log("addcell Method")
   
    mutations.forEach(function(mutation) {
    console.log("MUTATION TYPE" + mutation.type);
    var node  = mutation.addedNodes[0];
    if(node) {
        if (node.localName === "mx-graph-cell") {
              node.parent = this;   // add reference to the parent PolymerObject
              node.graph = this.graph; // add the mxGraph object
              this.push('cells', node);            

         }
     }
    }, this); 
     
}

  fireClickEdge(){
    this.dispatchEvent(new CustomEvent('click-edge', {detail: {kicked: true}}));
  }

  
}

window.customElements.define('mx-graph', MxGraph);
