echo Start copy frontend files... 

cp \
google-map-api.js \
google-map-constants.js \
google-map-marker.js \
google-map-polyline.js \
google-map.js \
../google-map-flow-root/google-map-flow/src/main/resources/META-INF/resources/frontend/

cp \
demo/marker.png \
demo/star.png \
../google-map-flow-root/google-map-flow-demo/src/main/resources/META-INF/resources/

echo End copy frontend files