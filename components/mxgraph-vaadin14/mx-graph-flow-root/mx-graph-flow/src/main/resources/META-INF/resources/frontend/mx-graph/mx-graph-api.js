

export var mxClient;
export var mxUtils;
export var mxGraph;
export var mxRubberband;
export var mxEvent;
export var mxCell;
export var mxPoint;
export var mxGeometry;
export var mxEdgeHandler;


class mxGraphApi {
    
    load() {
        if (!this.promise) {
            this.promise = new Promise(resolve => {
                this.resolve = resolve;

                import('./mx-graph-build.min.js')
                .then((module) => {
                    mxClient = module.mxClient;
                    mxUtils = module.mxUtils;
                    mxGraph = module.mxGraph;
                    mxRubberband = module.mxRubberband;
                    mxEvent = module.mxEvent;
                    mxCell = module.mxCell;
                    mxPoint = module.mxPoint;
                    mxGeometry = module.mxGeometry;
                    mxEdgeHandler = module.mxEdgeHandler;
                    this.resolve();
                });
            });
        }
        return this.promise;
    }
            
    ready() {
        if (this.resolve) {
            this.resolve();
        }
    }
}

export { mxGraphApi };