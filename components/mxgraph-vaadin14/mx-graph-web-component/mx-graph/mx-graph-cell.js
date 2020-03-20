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
class MxGraphCell extends PolymerElement {
  static get template() {
    return html``;
  }

  static get properties() {
    return {
      uuid: {
        type: String,
        value: '0'
      },
      cell: {           // reference to the cell that represents the Polymer Object MxGraphCell
        type: Object,
        notify: true
      },
       cellSourceLabel: {   // reference to the source cell label
        type: Object,
        notify: true
      },
       cellTargetLabel: {  // reference to the target cell label
        type: Object,
        notify: true
      },

      graph: {          //reference to the graph object container(mxGraph)
        type: Object,
        observer: '_graphChanged'    // listener called when the value is changed
      },
      edge: {           // specify if it is an edge
        type: Boolean,
        value: false
      },
      vertex: {         
        type: Boolean,  // specify if it is a vertex
        value: false
      },
      source: {       // uuid of the source vertex
        type: String,
        value: null
      },
      sourceLabel: {
        type: String,
        value: null,
        notify: true
      },
      target: {       // uuid of the target vertex
        type: String,
        value: null
      },
      targetLabel: {
        type: String,
        value: null,
        notify: true
      },
      parent: {           //reference to the parent PolymerObject(MxGraph)
        type : Object,
        value : null
      },
      points: {
        type: Array,
        value: function() { return []; },
        notify: true,
        observer: 'fireEdgePointsChanged'   // listener called when the value is changed
      },
      image: {
        type: String,
        value: null
      },
      label: {
        type: String,
        value: '',
        notify: true,
        observer: 'fireCellLabelChanged'  // listener called when the value is changed
      },
      width: {
        type: Number,
        value: 80
      },
      height: {
        type: Number,
        value: 30
      },
      x: {         // position on the x axis.
        type: Number,
        value: 0,
        notify: true,    // notifty for changes in the property
        observer: 'fireCellPositionChanged'   // listener called when the value is changed
      },
      y: {       //position on the y axis.
        type: Number,
        value: 0,
        notify: true,
        observer: 'fireCellPositionChanged'    // listener called when the value is changed
      },
      strokeWidth: {
        type: Number,
        value: 1
      },
      labelBackgroundColor: {
        type: Number,
        value: 'white'
      },
      perimeterSpacing: {
        type: Number,
        value: '1'
      },
      strokeColor: {
        type: String,
        value: 'black'
      },
      fontColor : {
        type: String,
        value: 'black'
      }
      
      
    };
  }

  _attachDom(dom) { this.appendChild(dom); }

  constructor() {
    super();
    console.log("Constructor: mxgraphCell")
  }

  ready() {
    super.ready();
    console.log("Ready: mxgraphCell")
//    console.log("adding Observer")
    //this._pointObserver = new MutationObserver(this.addPoint.bind(this));
    //this._pointObserver.observe(this, { childList: true});
  }

  initMxGraph() {


  }

  attached() {
    
    if (this.cell) {
     // this.cell.setMap(this.map);
    }
              console.log("ATTACHED");

  }
  // called when the graph property changes.
  _graphChanged() {
    console.log("FUNCTION _graphChanged");
    // Marker will be rebuilt, so disconnect existing one from old map and listeners.

    if (this.graph && this.graph instanceof mxGraph) {
      this._graphReady();
    } 


  }
 //  the cell is initialized with the initial parameters
  _graphReady() {
    console.log("FUNCTION _graphReady");
    var parent = this.graph.getDefaultParent();
    this.graph.getModel().beginUpdate();
    try {
      if (this.vertex) {  //if the cell is a vertex then...
        console.log("CREATIN VERTEX");
        var imageStyle = this.image ? ';shape=image;verticalLabelPosition=bottom;image='.concat(this.image) : '';
        this.cell = this.graph.insertVertex(parent, null, this.label, this.x, this.y, this.width, this.height,
              'verticalAlign=top' + imageStyle +
             ';fontStyle=1;labelPadding=5' +
            ';labelBackgroundColor=' + this.labelBackgroundColor +                
            ';fontColor=' + this.fontColor);
      
        

      } else if (this.edge) {  //if the cell is an edge then create it.
        console.log("CREATIN EDGE")
        if(this.source && this.target) {
                  
          var vertexs = this.parent.cells;
          var sourceNode;
          var targetNode;
          vertexs.forEach(function(node) {
            if (node.uuid == this.source) {
              sourceNode = node.cell;
            }
            if (node.uuid == this.target) {
              targetNode = node.cell;
            }
          },this);
          
          // create the edge and assign the reference
          this.cell = this.graph.insertEdge(parent, null, this.label, sourceNode, targetNode,
          'fontStyle=1;endArrow=none;orthogonalLoop=1;labelPadding=5\
            ;perimeterSpacing=' + this.perimeterSpacing +
            ';strokeWidth=' + this.strokeWidth + 
            ';labelBackgroundColor=' + this.labelBackgroundColor +
            ';strokeColor=' + this.strokeColor  +           
            ';fontColor=' + this.fontColor );

        
           // if there are control points, add them to the edge
          if(this.points && this.points.length > 0) {
            var arrayPoints = JSON.parse(this.points); 
            if (! this.cell.geometry.points ) {
              this.cell.geometry.points = [];
            } 
            arrayPoints.forEach(function(point)  {
                        
              this.cell.geometry.points.push(new mxPoint(point.x,point.y));
                     
            },this)
            

          }    

          var _this = this
 
          // if there are labels, add them to the edge
          if(this.sourceLabel) {
            this.cellSourceLabel = new mxCell(this.sourceLabel, new mxGeometry(-0.8, 0, 0, 0), 
            'resizable=0;editable=1;fontStyle=1;labelPadding=5' +
            ';labelBackgroundColor=' + this.labelBackgroundColor +                
            ';fontColor=' + this.fontColor );
                    
             this.cellSourceLabel.geometry.relative = true;
             this.cellSourceLabel.setConnectable(false);
             this.cellSourceLabel.vertex = true;
             this.cellSourceLabel.id = this.cell.id + "-source";
             this.cell.insert(this.cellSourceLabel);
          }
          if(this.targetLabel) {
            this.cellTargetLabel = new mxCell(this.targetLabel, new mxGeometry(0.8, 0, 0, 0),
             'resizable=0;editable=1;'+
            'fontStyle=1;labelPadding=5' +
            ';labelBackgroundColor=' + this.labelBackgroundColor +                
            ';fontColor=' + this.fontColor);
            this.cellTargetLabel.geometry.relative = true;
	    this.cellTargetLabel.setConnectable(false);
            this.cellTargetLabel.vertex = true;
            this.cellTargetLabel.id = this.cell.id + "-target"
	    this.cell.insert(this.cellTargetLabel);
          }
          
        }
      }
     
    }
    finally {
      // Updates the display
    this.graph.getModel().endUpdate();
    this.graph.refresh();
    }

  }

// fired when some children tag is added.
  addPoint(mutations){
    console.log("addPoint Method")
   
    mutations.forEach(function(mutation) {
    console.log("MUTATION TYPE" + mutation.type);
    var node  = mutation.addedNodes[0];
    if(node) {
        if (node.localName === "mx-graph-point") {
              node.parent = this;   // add reference to the parent PolymerObject
              node.cell = this.cell;  // add the mxGraphCell object reference
              this.push('points', node);             

         }
     }
    }, this); 
     
}

updatePosition() {
    this.cell.geometry.x = this.x;
    this.cell.geometry.y = this.y;
    this.graph.refresh();
}

// Custom Events

  fireClickEdge() {
    this.dispatchEvent(new CustomEvent('click-edge', { detail: { kicked: true } }));
    console.log("click-edge fired");
  }

  fireCellPositionChanged(){
    this.dispatchEvent(new CustomEvent('cell-position-changed', {detail: {kicked: true}}));
    console.log("cell-position-changed fired");

  }
  
  fireEdgePointsChanged() {
    this.dispatchEvent(new CustomEvent('edge-points-changed', {detail: {kicked: true}}));
    console.log("edge-points-changed fired");
  }
  
  fireCellLabelChanged() {
    this.dispatchEvent(new CustomEvent('cell-label-changed', {detail: {kicked: true}}));
    console.log("Cell Label Changed fired");
  }


}

window.customElements.define('mx-graph-cell', MxGraphCell);
