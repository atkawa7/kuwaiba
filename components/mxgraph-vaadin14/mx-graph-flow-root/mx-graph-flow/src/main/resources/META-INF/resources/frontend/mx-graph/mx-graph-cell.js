/** 
@license
Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.

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
    return html`<style>
         .cell-animated {
           animation:bounce-cell 1s infinite;
        }
         @keyframes bounce-cell {
             0%       { transform: translateY(-3px); }
	     25%, 75% {  transform: translateY(-7px); }
	     50%      {  transform: translateY(-10px); }
	     100%     { transform: translateY(0);}
    }
            </style> <slot></slot>`;
  }

  static get properties() {
    return {
      uuid: {
        type: String,
        value: null
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
      //array of polymer objects of type mx-graph-cell
      cells: {
        type: Array,
        value: function() { return []; }   
      },
      edge: {           // specify if it is an edge
        type: Boolean,
        value: false
      },
      vertex: {         
        type: Boolean,  // specify if it is a vertex
        value: false
      },
      layer: {         
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
      parent: {           //reference to the parent PolymerObject(Mxgraph, MxgraphCell)
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
        observer: 'cellLabelChanged'  // listener called when the value is changed
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
        type: String,
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
      },
      dashed: {
        type: String,
        value: '0'
      },
      curved: {
        type: String,
        value: '0'
      },
      cellParent: {
        type: String,
        value: null
      },
      cellLayer: {
          type: String,
          value : null
      },
      styleName: { // name of the style in the styleSheet
          type: String,
          value : null,
          observer: 'styleNameChanged' 
      },
      rawStyle: { // intented to assign raw styles without using the styleSheet 
               // example posible value:  'strokeColor=red;shape=ellipse' 
          type: String,
          value : null,
          observer: 'rawStyleChanged' 
      },
      fillColor: {
        type: String,
        value: null
      },
      shape: {
          type: String,
          value: 'rectangle'
      },
      verticalLabelPosition: {
          type: String,
          value : 'bottom'
      },
      labelPosition: {
          type: String,
          value : 'bottom'
      },
      movable: {         
        type: String, 
        value: '1',
        observer: 'movableChanged' 
      },
      animateOnSelect: {
          type: Boolean,
          value: false
      },
      fontSize : {
          type: Number,
          value : 10
      },
      selectable: {
          type: Boolean,
          value: true
      }
    };
  }

  _attachDom(dom) { this.appendChild(dom); }

  constructor() {
    super();
    console.log("Constructor: mxgraphCell")
//    this._pointObserver = new MutationObserver(this.tagAdded.bind(this));
//    this._pointObserver.observe(this, { childList: true});
  }

  ready() {
    super.ready();
    //console.log("Ready: mxgraphCell")
//    console.log("adding Observer")
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
    var t0 = performance.now()
    var t1;
    var parentObject;
    if (this.cellLayer)
        parentObject = this.graph.getModel().getCell(this.cellLayer);
    else if (this.cellParent) {
        parentObject = this.graph.getModel().getCell(this.cellParent);
    } else {
        parentObject = this.graph.getDefaultParent();
    }
    t1 = performance.now()
    console.log("part 1 " + (t1 - t0) + " milliseconds.")
    try {
      if (this.vertex) {  //if the cell is a vertex then create a new one
        console.log("CREATING VERTEX");
//        t0 = performance.now();
        var imageStyle =   this.image ? ';image='.concat(this.image) : '';
        this.cell = this.graph.insertVertex(parentObject, this.uuid ? this.uuid : null, 
                                            this.label, this.x, this.y, this.width, this.height,
              'verticalAlign=top' + imageStyle +
             ';fontStyle=1;labelPadding=5' +
             ';shape=' + this.shape +
             ';verticalLabelPosition=' + this.verticalLabelPosition + 
             ';labelPosition=' + this.labelPosition + 
            ';labelBackgroundColor=' + this.labelBackgroundColor +                
            ';fillColor=' + (this.fillColor ? this.fillColor : '#CCC') +                
            ';movable=' + this.movable +   
              ';strokeColor=' + this.strokeColor  + 
              ';fontSize=' + this.fontSize  + 
            ';fontColor=' + this.fontColor
            + imageStyle) ;
//    t1 = performance.now();
//    console.log("part 2 after insert vertex " + (t1 - t0) + " milliseconds.");

    
      if (this.rawStyle) {
          this.rawStyleChanged();
      }
    
      if (this.styleName) {
          this.styleNameChanged();
      }
              
      } else if (this.layer) { 
          console.log("CREATINGLAYER");
          var newLayer = new mxCell();
          newLayer.id = this.uuid;
          this.cell = this.graph.getModel().add(this.graph.getModel().getRoot(), newLayer);
      } else if (this.edge) {  //if the cell is an edge then create it.
        console.log("CREATING EDGE");
        if(this.source && this.target) {
                  
          var sourceNode = this.graph.model.getCell(this.source);
          var targetNode = this.graph.model.getCell(this.target);
                
          // create the edge and assign the reference
          this.cell = this.graph.insertEdge(parentObject, this.uuid ? this.uuid : null, this.label, sourceNode, targetNode,
          'fontStyle=1;endArrow=none;orthogonalLoop=1;labelPadding=5\
            ;perimeterSpacing=' + this.perimeterSpacing +
            ';strokeWidth=' + this.strokeWidth + 
            ';labelBackgroundColor=' + this.labelBackgroundColor +
            ';strokeColor=' + this.strokeColor  +           
            ';dashed=' + this.dashed  +           
            ';curved=' + this.curved  +           
            ';rounded=1'  +           
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
    
    t1 = performance.now()
    console.log("part 2 after insert end update " + (t1 - t0) + " milliseconds.")
    //this.graph.refresh();
    this.fireCellAdded();
    t1 = performance.now()
    console.log("Call to Create vertex took " + (t1 - t0) + " milliseconds.")
    }

  }
  
   // fired when some children tag is added.
  tagAdded(mutations){
      
    console.log("tagAdded Method")

    if (this.graph) {
        console.log("tagAdded Method GRAPH ready")
        this.updateChildren(mutations);
    } else {
        console.log("tagAdded Method NOT GRAPH ready")
        setTimeout(() => {
        
        this.updateChildren(mutations);
       
        }, 2000);  
    }    
  }

// fired when some children tag is added.
  updateChildren(mutations){
    console.log("addPoint Method")
   
    mutations.forEach(function(mutation) {
    console.log("MUTATION TYPE" + mutation.type);

     var addedNodes = mutation.addedNodes;
        addedNodes.forEach(node => {
            if (node) {
                if (node.localName === "mx-graph-cell") {
                console.log("CELL TAG ADDED " + node.uuid);
                        node.parent = this; // add reference to the parent PolymerObject
                        node.graph = this.graph; // add the mxGraph object
                        this.push('cells', node);
                }
                if (node.localName === "mx-graph-point") {
                    node.parent = this;   // add reference to the parent PolymerObject
                    node.cell = this.cell;  // add the mxGraphCell object reference
                    this.push('points', node);             

                  }
            }
        });
        var removedNodes = mutation.removedNodes;
        removedNodes.forEach(node => {
        if (node) {
            if (node.localName === "mx-graph-cell") {
                var index = this.cells.indexOf(node);
                if (index > -1) {
                    this.cells.splice(index, 1);
                }  
                if (node.cell) {
                    console.log("CELL TAG REMOVED " + node.uuid);
                    var nodes = [node.cell];
                    this.graph.removeCells(nodes , false);
                }
            };
          };
        });
     
    }, this); 
     
}

addOverlayButton(buttonId,  label, urlImage, hAlign, vAlign, offsetX, offsetY)
    {
        if (this.cell) {
	var overlay = new mxCellOverlay(new mxImage(urlImage, 16, 16), label, hAlign, vAlign);
	overlay.cursor = 'hand';
        if (!offsetX)
            offsetX = 0;
        if (!offsetY)
            offsetY = 0;
        overlay.offset = new mxPoint(offsetX, offsetY);
	overlay.addListener(mxEvent.CLICK, mxUtils.bind(this, function(sender, evt)
	{
            this.fireClickOverlayButton(buttonId);
	}));
	
	this.graph.addCellOverlay(this.cell, overlay);
    }
   };
    
setChildrenCellPosition(cellId, position) {
    if (this.cell && position >= 0 && this.cell.children.length > position) {
        var theCell;
        this.cell.children.forEach(function (children) {
           if (children.id === cellId)
           theCell = children;
        });
        if (theCell) {   
            var index = this.cell.children.indexOf(theCell);
            this.cell.children.splice(index, 1);
            this.cell.children.splice(position, 0, theCell);
        }
    }
}

updatePosition() {
    this.cell.geometry.x = this.x;
    this.cell.geometry.y = this.y;
    this.graph.refresh();
}

toggleVisibility() {
    this.graph.getModel().setVisible(this.cell, !this.graph.getModel().isVisible(this.cell));
}

cellLabelChanged() {
    if (this.graph) {
        this.graph.model.setValue(this.cell, this.label);    
        this.fireCellLabelChanged();
    }
}

// Custom Events

  fireClickCell() {
    this.dispatchEvent(new CustomEvent('click-cell', { detail: { kicked: true } }));
    console.log("click-cell fired");
  }
  
  fireRightClickCell() {
    this.dispatchEvent(new CustomEvent('right-click-cell', { detail: { kicked: true } }));
    console.log("right-click-cell fired");
  }

  fireCellPositionChanged(){
    if (this.cell) {
        this.dispatchEvent(new CustomEvent('cell-position-changed', {detail: {kicked: true}}));
        console.log("cell-position-changed fired");
    }
  }
  
  fireEdgePointsChanged() {
    if (this.cell) {
        this.dispatchEvent(new CustomEvent('edge-points-changed', {detail: {kicked: true}}));
        console.log("edge-points-changed fired");
    }
  }
  
  fireCellLabelChanged() {
    if (this.cell) {
        this.dispatchEvent(new CustomEvent('cell-label-changed', {detail: {kicked: true}}));
        console.log("Cell Label Changed fired");
    }
  }
  
  fireClickOverlayButton(buttonId) {
    this.dispatchEvent(new CustomEvent('click-overlay-button', { detail: { kicked: true, buttonId:buttonId} }));
    console.log("click-overlay-button fired");
  }
  
   //This method dispatches a custom event when the graph is loaded
  fireCellAdded() {
        this.dispatchEvent(new CustomEvent('cell-added', {detail: {kicked: true}}));
    }
  
  styleNameChanged() {
      if (this.graph && this.cell) {
          var style = this.graph.getStylesheet().getCellStyle(this.styleName, null);
          if (style) {
              var cs= new Array();
              cs[0] = this.cell;             
              this.graph.setCellStyle(this.styleName, cs);
        }
      }
  }
  
   rawStyleChanged() {
      if (this.graph && this.cell) {        
              var cs= new Array();
              cs[0] = this.cell;             
              this.graph.setCellStyle(this.cell.style + ';' + this.rawStyle, cs);       
      }
  }
  
  movableChanged() {
      if (this.graph) {
         this.graph.setCellStyles(mxConstants.STYLE_MOVABLE, this.movable, [this.cell])
      }
  }
  
  setMovable(movable) {
      if (this.graph) {
         var t0 = performance.now();
         this.graph.setCellStyles(mxConstants.STYLE_MOVABLE, movable, [this.cell]);
         var t1 = performance.now()
         console.log("Set movable took " + (t1 - t0) + " milliseconds.")
      }
  }
  
  startAnimation() {
      var state = this.graph.view.getState(this.cell);
      state.shape.node.classList.add('cell-animated');
  }
  
  stopAnimation() {
     var state = this.graph.view.getState(this.cell);
     state.shape.node.classList.remove('cell-animated'); 
  }
    
}

window.customElements.define('mx-graph-cell', MxGraphCell);
