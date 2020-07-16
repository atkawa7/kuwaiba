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
class MxGraph extends PolymerElement {
    static get template() {
        return html`
      <style>
        :host {
          display: block;
        }
      </style>
      <div id="graphContainer" 
      style="overflow:[[overflow]];width:[[width]];height:[[height]];min-height:300px;background:url([[grid]]);max-width:[[maxWidth]];max-height:[[maxHeight]]">
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
            },
            //array of polymer objects of type mx-graph-cell
            cells: {
                type: Array,
                value: function () {
                    return [];
                }
            },
            // background image path
            grid: {
                type: String,
                value: ''
            },
            width: {
                type: String,
                value: '400px'
            },
            height: {
                type: String,
                value: '400px'
            },
            maxWidth: {
                type: String
            },
            maxHeight: {
                type: String
            },
            overflow: {
                type: String,
                value : 'auto'
            },
            cellsMovable: {
                type: Boolean,
                value: true
            },
            cellsEditable: {
                type: Boolean,
                value: true
            },
            stackLayout: {
                type: Object,
                value: null
            }
        }
    }

    constructor() {
        super();
        //        console.log("adding Observer")
        this._cellObserver = new MutationObserver(this.tagAdded.bind(this));
        this._cellObserver.observe(this, {childList: true});

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
        this.addEventListener('mouseover', () => {
            this.dispatchEvent(new CustomEvent('mx-graph-mouse-over'));
        });
        console.log("READY")
        if (window.mxgraphLoaded) 
            this.initMxGraph();
        else {
            new mxGraphApiLoader().load().then(() => {
                this.initMxGraph();
            });
        }
    }

    //called then the mxGraph library has been loaded and initialize the grap object
    initMxGraph() {
        // Checks if the browser is supported
        console.log("initMxGraph")
        if (!mxClient.isBrowserSupported())
        {
            // Displays an error message if the browser is not supported.
            mxUtils.error('Browser is not supported!', 200, false);
        } else
        {
            // Disables the built-in context menu
            mxEvent.disableContextMenu(this.$.graphContainer);

            //var model = new mxGraphModel(new mxCell());
            // Creates the graph inside the given container
            this.graph = new mxGraph(this.$.graphContainer);
            // Enables rubberband selection
            //new mxRubberband(this.graph);
            //this.graph.setConnectable(true);
            this.graph.setAllowDanglingEdges(false);

            mxGraph.prototype.cellsEditable = this.cellsEditable;
            mxGraph.prototype.cellsMovable = this.cellsMovable;

            //enable adding and removing control points. 
            mxEdgeHandler.prototype.addEnabled = true;
            mxEdgeHandler.prototype.removeEnabled = true;

            this.graph.getSelectionModel().setSingleSelection(true);


            var _this = this;
            // here add handles for general click events in the graph
            this.graph.addListener(mxEvent.CLICK, function (sender, evt) {
                var cell = evt.getProperty('cell');
                console.log("CLICK")
                console.log(evt)

                if (cell) {
                    var cellObject = _this.getCellObjectById(cell.id);
                    cellObject.fireClickCell();
                    console.log("CLICK on Cell")
                    
                } else {
                    _this.fireClickGraph(evt.properties.event.layerX, evt.properties.event.layerY);
                }
            });
            this.graph.addListener(mxEvent.FIRE_MOUSE_EVENT, function (sender, evt) {
   
//                console.log("MOUSE_MOVE")
//                console.log(evt)
               if (evt.properties.eventName === 'mouseMove') {
                   _this.fireMouseMoveGraph(evt.properties.event.graphX, evt.properties.event.graphY);
               }
                
            });
            // fire right click event
            this.graph.popupMenuHandler.factoryMethod = function(menu, cell, evt) {
                if (cell) {
                    var cellObject = _this.getCellObjectById(cell.id);
                    if (cell.edge || cell.vertex){
                          cellObject.fireRightClickCell(); 
                    }
                } else {
                    _this.fireRightClickGraph(evt.layerX, evt.layerY);
                }   
            }

//          detect delete key to fire delete object event
            var keyHandler = new mxKeyHandler(this.graph);
            keyHandler.bindKey(46, function (evt)
            {
                if (_this.graph.isEnabled())
                {
                    // _this.graph.removeCells(); // direct delete disables, instead event is fired
                }
                if (_this.graph.getSelectionCell()) { // just single selection is supported
                    _this.fireDeleteCellSelected();
                }
            });

            // Called when any cell is moved
            this.graph.addListener(mxEvent.CELLS_MOVED, function (sender, evt) {
                var cellsMoved = evt.getProperty('cells');
                var dx = evt.getProperty('dx');
                var dy = evt.getProperty('dy');
                console.log("CELLS_MOVED")
                console.log(evt)

                if (cellsMoved) {

                    cellsMoved.forEach(function (cellMoved) {
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

            //allow custom logic when unselect cells
            var cellUnselected = mxGraphSelectionModel.prototype.cellRemoved;
            mxGraphSelectionModel.prototype.cellRemoved = function (cell) {
                cellUnselected.apply(this, arguments);
                if (cell) {
                    _this.fireCellUnselected(cell.id, _this.graph.getModel().isVertex(cell));
                    var cellObject = _this.getCellObjectById(cell.id);
                    
                    if (cellObject && cellObject.animateOnSelect && _this.graph.getModel().isVertex(cell)) {
                        cellObject.stopAnimation();  
                    }
                }
                console.log("CELL UNSELECTED :" + cell.id + " is Vertex : " + _this.graph.getModel().isVertex(cell));
            }

            //allow custom logic when select cells
            var cellSelected = mxGraphSelectionModel.prototype.cellAdded;
            mxGraphSelectionModel.prototype.cellAdded = function (cell) {
                cellSelected.apply(this, arguments);
                if (cell) {
                    _this.fireCellSelected(cell.id, _this.graph.getModel().isVertex(cell));  
                    var cellObject = _this.getCellObjectById(cell.id);
                    
                    if (cellObject && cellObject.animateOnSelect && _this.graph.getModel().isVertex(cell)) {
                        cellObject.startAnimation();                  
                    }                  
                }
                          
                console.log("CELL SELECTED :" + cell.id + " is Vertex : " + _this.graph.getModel().isVertex(cell));
            }
            //allow custom logic when editing in edges labels
//        mxGraph.prototype.isCellEditable = function(	cell	){
//          return true;
//        }
            //Handler for labelChanged events fired when some label was edited.
            var labelChanged = mxGraph.prototype.labelChanged;
            mxGraph.prototype.labelChanged = function (cell, value, evt) {
                labelChanged.apply(this, arguments);
                // if the cell is an edge label
                if (_this.graph.getModel().isEdge(cell.parent)) {

                    var cellObject = _this.getCellObjectById(cell.parent.id);

                    if (cellObject) {

                        if (cell.id == cellObject.cellSourceLabel.id) {
                            cellObject.sourceLabel = value;
                        }
                        if (cell.id == cellObject.cellTargetLabel.id) {
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
            // The method  changePoints is overwritten, to update the points in the respective PolymerElement object.
            var mxChangePoints = mxEdgeHandler.prototype.changePoints;
            mxEdgeHandler.prototype.changePoints = function (edge, points, clone) {
                console.log("CHANGEPOINTS EVENT")
                console.log(edge)
                console.log(points)
                console.log(clone)
                if (edge && _this.graph.getModel().isEdge(edge)) {

                    var cellObject = _this.getCellObjectById(edge.id);

                    if (cellObject) {

                        cellObject.points = JSON.stringify(points);

                    }

                }
                mxChangePoints.apply(this, arguments);
            }

            // The method  addPointAt is overwritten, to update the points in the respective PolymerElement object, when a point is added,

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

            // The method  removePoint is overwritten, to update the points in the respective PolymerElement object, when a point is removed,

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


        }
        this.fireGraphLoaded();
    }
    ;
            // Get the polymer object that represents the cell with the porvided idCell. 
            getCellObjectById(idCell) {
        var cell;
        this.cells.forEach(function (cellObject) {
            if (cellObject.cell.id == idCell) {
                cell = cellObject;
            }

        }, this);
        return cell;
    }
    ;
            // fired when some children tag is added.
    waitForGraph(func, args) {

            var _this = this;
            console.log("WAITING FOR GRAPH")
            setTimeout(() => {

                func.bind(this)(args);

            }, 1000);
        

    }
            // fired when some children tag is added.
    tagAdded(mutations) {

        console.log("tagAdded Method")

        if (this.graph) {
            console.log("tagAdded Method GRAPH ready")
            this.updateChildren(mutations);
        } else {
            console.log("tagAdded Method NOT GRAPH ready")
            setTimeout(() => {

                this.updateChildren(mutations);

            }, 500);
        }

    }

    updateChildren(mutations) {

        mutations.forEach(function (mutation) {
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
                            this.graph.removeCells(nodes, false);
                        }
                    }
                    ;
                }
                ;
            });
        }, this);
    }

    executeStackLayout(cellId, horizontal, spacing, marginTop, marginRight, marginBottom, marginLeft) {
        if (this.graph) {
            if (!this.stackLayout) {
                this.stackLayout = new mxStackLayout(this.graph, horizontal, spacing);
            } else {
                this.stackLayout.horizontal = horizontal;
                this.stackLayout.spacing = spacing;
            }
            this.stackLayout.marginTop = marginTop;
            this.stackLayout.marginRight = marginRight;
            this.stackLayout.marginBottom = marginBottom;
            this.stackLayout.marginLeft = marginLeft;
            this.stackLayout.resizeParent = true;
            this.stackLayout.resizeParentMax = true;

            var cell = this.graph.model.getCell(cellId);
            if (cell)
                this.stackLayout.execute(cell);
        }  else {
            var _this = this;
            setTimeout( this.waitForGraph(() => {_this.executeStackLayout(cellId, horizontal, spacing, marginTop, marginRight, marginBottom, marginLeft);}, 2500));
        }
    }
    
    alignCells(align, cellIds, coordinate) {
        if (this.graph) {
            if (align && cellIds) {
                var cells = JSON.parse(cellIds); 
                var cellsArray = new Array();
                for (const id of cells) {
                    var cell = this.graph.model.getCell(id); 
                    cellsArray.push(cell);
                }          
                this.alignMxGraphCells(align, cellsArray, coordinate); 
           }
        } else 
            this.waitForGraph(() => {
                this.alignCells(align, cellIds, coordinate)
            });       
    }
    
    setCellsMovableStyle(align, cellIds, coordinate) {
        if (this.graph) {
            if (align && cellIds) {
                var cells = JSON.parse(cellIds); 
                var cellsArray = new Array();
                for (const id of cells) {
                    var cell = this.graph.model.getCell(id); 
                    cellsArray.push(cell);
                }          
                this.alignMxGraphCells(align, cellsArray, coordinate); 
           }
        } else 
            this.waitForGraph(() => {
                this.alignCells(align, cellIds, coordinate)
            });       
    }

    addCellStyle(name, jsonProperties) {
        if (this.graph && jsonProperties && jsonProperties.length > 0) {
            var styleProps = JSON.parse(jsonProperties);
            if (!this.isEmpty(styleProps))
                this.graph.getStylesheet().putCellStyle(name, styleProps);
        }
    }

//This method dispatches a custom event when the graph canvas is clicked (not fired on clicks in any vertex, edge, layer )
    fireClickGraph(x, y) {
        this.dispatchEvent(new CustomEvent('click-graph', {detail: {kicked: true, x:x , y:y}}));
    }
//This method dispatches a custom event when the mouse is moved over the graph
    fireMouseMoveGraph(x, y) {
        this.dispatchEvent(new CustomEvent('mouse-move-graph', {detail: {kicked: true, x:x , y:y}}));
    }
    
    //This method dispatches a custom event when right click is detected
    fireRightClickGraph(x, y) {
        this.dispatchEvent(new CustomEvent('right-click-graph', {detail: {kicked: true, x:x , y:y}}));
    }

    //This method dispatches a custom event when any edge its clicked
    fireCellSelected(cellId, isVertex) {
        this.dispatchEvent(new CustomEvent('cell-selected',
                {detail: {kicked: true, cellId: cellId, isVertex: isVertex}}));
    }

    //This method dispatches a custom event when any edge is unselected
    fireCellUnselected(cellId, isVertex) {
        this.dispatchEvent(new CustomEvent('cell-unselected',
                {detail: {kicked: true, cellId: cellId, isVertex: isVertex}}));
    }

    //This method dispatches a custom event when the delete key is press.
    fireDeleteCellSelected() {
        this.dispatchEvent(new CustomEvent('delete-cell-selected', {detail: {kicked: true}}));
    }
    
     //This method dispatches a custom event when the graph is loaded
    fireGraphLoaded() {
        this.dispatchEvent(new CustomEvent('graph-loaded', {detail: {kicked: true}}));
    }

    //this method remove all cells(vertex and edges) in the graph
    removeAllCells() {
        if (this.graph)
            this.graph.removeCells(this.graph.getChildVertices(this.graph.getDefaultParent()));
        
    }
    
    // test if the given object is null
    isEmpty(obj) {
        return Object.keys(obj).length === 0;
    }

    //this method refresh all objects in the graph
    refreshGraph() {
        if (this.graph)
            this.graph.refresh();
        else 
            this.waitForGraph(this.refreshGraph);
    }
    
    alignMxGraphCells(align, cells, param) {
        if (cells == null)
            cells = this.graph.getSelectionCells();      

        if (cells != null && cells.length > 1) {
            // Finds the required coordinate for the alignment
            if (param == null) {
                for (var i = 0; i < cells.length; i++) {
                    var state = this.graph.view.getState(cells[i]);

                    if (state != null && !this.graph.model.isEdge(cells[i])) {
                        if (param == null) {
                            if (align == mxConstants.ALIGN_CENTER) {
                                param = state.x + state.width / 2;
                                break;
                            } else if (align == mxConstants.ALIGN_RIGHT) {
                                param = state.x + state.width;
                            } else if (align == mxConstants.ALIGN_TOP) {
                                param = state.y;
                            } else if (align == mxConstants.ALIGN_MIDDLE) {
                                param = state.y + state.height / 2;
                                break;
                            } else if (align == mxConstants.ALIGN_BOTTOM) {
                                param = state.y + state.height;
                            } else {
                                param = state.x;
                            }
                        } else
                        {
                            if (align == mxConstants.ALIGN_RIGHT) {
                                param = Math.max(param, state.x + state.width);
                            } else if (align == mxConstants.ALIGN_TOP) {
                                param = Math.min(param, state.y);
                            } else if (align == mxConstants.ALIGN_BOTTOM) {
                                param = Math.max(param, state.y + state.height);
                            } else {
                                param = Math.min(param, state.x);
                            }
                        }
                    }
                }
            }
            // Aligns the cells to the coordinate
            if (param != null) {
                var s = this.graph.view.scale;

                this.graph.model.beginUpdate();
                try {
                    for (var i = 0; i < cells.length; i++) {
                        var state = this.graph.view.getState(cells[i]);

                        if (state != null) {
                            var geo = this.graph.getCellGeometry(cells[i]);

                            if (geo != null && !this.graph.model.isEdge(cells[i])) {
                                geo = geo.clone();

                                if (align == mxConstants.ALIGN_CENTER) {
                                    geo.x += (param - state.x - state.width / 2) / s;
                                } else if (align == mxConstants.ALIGN_RIGHT) {
                                    geo.x += (param - state.x - state.width) / s;
                                } else if (align == mxConstants.ALIGN_TOP) {
                                    geo.y += (param - state.y) / s;
                                } else if (align == mxConstants.ALIGN_MIDDLE) {
                                    geo.y += (param - state.y - state.height / 2) / s;
                                } else if (align == mxConstants.ALIGN_BOTTOM) {
                                    geo.y += (param - state.y - state.height) / s;
                                } else {
                                    geo.x += (param - state.x) / s;
                                }
                                this.graph.resizeCell(cells[i], geo);
                            }
                        }
                    }

                    this.graph.fireEvent(new mxEventObject(mxEvent.ALIGN_CELLS,
                            'align', align, 'cells', cells));
                } finally {
                    this.graph.model.endUpdate();
                }
            }
        }

        return cells;
    };
    

}

window.customElements.define('mx-graph', MxGraph);
