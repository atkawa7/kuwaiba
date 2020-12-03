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
 * Utility functions to polygons and polylines.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
class GeometryPoly {
  /**
   * Calculate whether the given point exist inside the specified path.
   * @param {Object} point - {lat: 0, lng: 0}
   * @param {Object[]} paths - [.. ,[.. ,{lat: 0, lng: 0}]]
   * @returns {boolean} True if the given point exist inside the specified path.
   */
  containsLocation(point, paths) {
    const polygon = new google.maps.Polygon({paths: paths});

    return google.maps.geometry.poly.containsLocation(
      new google.maps.LatLng(point.lat, point.lng),
      polygon
    );
  }
  /**
   * Calculate whether the given points exist inside the specified path.
   * @param {Object} points - {id: {lat: 0, lng: 0}, ..}
   * @param {Object[]} paths - [.. ,[.. ,{lat: 0, lng: 0}]]
   * @returns {Object} {id: true | false, ..}
   */
  containsLocations(points, paths) {
    var contains = {};
    for (const point in points) {
      contains[point] = this.containsLocation(points[point], paths);
    }
    return contains;
  }
  /**
   * Calculate whether the given point exist inside the specified path.
   * @param {Object} point - {lat: 0, lng: 0}
   * @param {Object[]} paths - [.. ,[.. ,{lat: 0, lng: 0}]]
   * @param {boolean} isPolyline - True if the path is of a polyline or false if is of a polygon
   * @param {number} tolerance - 
   * @returns {boolean} True if the given point exist inside the specified path.
   */
  isLocationOnEdge(point, paths, isPolyline, tolerance) {
    const poly = null;
    if (isPolyline)
      poly = new google.maps.Polyline({path: paths[0]});
    else
      poly = new google.maps.Polygon({paths: paths});

    return google.maps.geometry.poly.isLocationOnEdge(
      new google.maps.LatLng(point.lat, point.lng),
      poly,
      tolerance
    );
  }
}

export { GeometryPoly };