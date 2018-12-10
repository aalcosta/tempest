(function($) {

    /**
     * requestObject = {
     *     method  - Optional (Default: 'GET')
     *     url     - Mandatory
     *     params  - Optional (Default: {})
     *     headers - Optional (Default: {})
     * }
     */

	invocationCache = {

        dataValidity: 30*60*1000, // 30 min

        isAvailable: typeof(Storage) !== "undefined",

		invalidate: function() {
			if(invocationCache.isAvailable) {
				localStorage.clear();
		    }
		},

        load: function (requestObject) {
		    requestObject = _setDefaults(requestObject);

            defer = $.Deferred();
            _invoke(requestObject).done(function(data) {
    			if(invocationCache.isAvailable) {
    			    _setItem(requestObject, data);
                }
                defer.resolve(data);
            });
            return defer.promise();
        },

		get: function(requestObject) {
    	    requestObject = _setDefaults(requestObject);

	        var defer = $.Deferred();
			if(!invocationCache.isAvailable) {
			    _invoke(requestObject).done(function(data) { defer.resolve(data); });
			    return defer.promise();
			}

			var cacheKey = JSON.stringify(requestObject);
			var cacheEntry = JSON.parse(localStorage.getItem(cacheKey));
       		if (!cacheEntry) {
       		    invocationCache.load(requestObject).done(function(data) {
                    defer.resolve(data);
       		    });
       		} else {
		        defer.resolve(cacheEntry.data);
		        if (cacheEntry.validity < new Date().getTime()) {
		            invocationCache.load(requestObject);
                }
		    }
			return defer.promise();
		}

	}

    function _invoke(requestObject) {
        return $.ajax({
            method: requestObject.method,
            url : requestObject.url,
            data: requestObject.params,
            headers: requestObject.headers,
            dataType: 'json'
        });
    }

    function _setDefaults(requestObject) {
        requestObject.method = requestObject.method || 'GET';
        requestObject.params = requestObject.params || {};
        requestObject.headers = requestObject.headers || {};
        return requestObject;
    }

    function _setItem(requestObject, cachedData) {
        var cacheKey = JSON.stringify(requestObject);
        var cacheEntry = JSON.stringify({
            data: cachedData,
            validity: new Date().getTime() + invocationCache.dataValidity
        });
        localStorage.setItem(cacheKey, cacheEntry);
    }

})(jQuery);