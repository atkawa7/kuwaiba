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

/**
 * Redraws node/edge on graph
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */

$0.map.setOptions({draggable: false, maxZoom: $0.map.getZoom(), minZoom: $0.map.getZoom()});
var nodes = $1;
var edges = $2;
var containsLocations = $3;
var mapZoom = $4;
var mapMinZoomForLabels = $5;
var labels = $6;
this.graph.getModel().beginUpdate();
try {
  // Updating the node geometries
  for (const cellId in nodes) {
    var cell = this.getCellObjectById(cellId).cell;
    var geo = this.graph.getCellGeometry(cell).clone();
    var point = nodes[cellId][0];
    geo.x = point.x;
    geo.y = point.y;
    this.graph.getModel().setGeometry(cell, geo);
    if (containsLocations[cellId] === true) {
      if (mapZoom >= mapMinZoomForLabels) {
        if (!this.graph.getModel().getValue(cell)) {
          this.graph.getModel().setValue(cell, labels[cellId]);
        }
      } else {
        if (this.graph.getModel().getValue(cell)) {
          this.graph.getModel().setValue(cell, null);
        }
      }
    } else {
      this.graph.getModel().setValue(cell, null);
    }
  }
  // Updating the edge geometries
  for (const cellId in edges) {
    var cell = this.getCellObjectById(cellId).cell;
    var geo = this.graph.getCellGeometry(cell).clone();
    var points = [];
    edges[cellId].forEach(point => {
      points.push(new mxPoint(point.x, point.y));
    });
    geo.points = points;
    this.graph.getModel().setGeometry(cell, geo);
  }
} finally {
  this.graph.getModel().endUpdate();
}
$0.map.setOptions({draggable: true, maxZoom: null, minZoom: null});
