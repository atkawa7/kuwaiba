
class mxGraphApi {
    
    load() {
        if (!this.promise) {
            this.promise = new Promise(resolve => {
                this.resolve = resolve;
                const script = document.createElement('script');
                script.src = './MXGRAPH/mxClient.min.js';
                script.type = 'text/javascript';
                script.async = true;
                script.addEventListener('load', () => {
                    this.ready();
                });
                document.body.append(script);
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