export var mxClient;
export var mxUtils;
export var mxGraph;
export var mxRubberband;
export var mxEvent;

class mxGraphApi {
    
    load() {
        if (!this.promise) {
            this.promise = new Promise(resolve => {
                this.resolve = resolve;

                import('./mx-graph-build.js')
                .then((module) => {
                    mxClient = module.mxClient;
                    mxUtils = module.mxUtils;
                    mxGraph = module.mxGraph;
                    mxRubberband = module.mxRubberband;
                    mxEvent = module.mxEvent;
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