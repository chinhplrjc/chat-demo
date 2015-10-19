(function() {
    var SubPub = {};
    window.SubPub = SubPub;

    var subcribers = {};

    SubPub.sub = function(e, cb) {
        var arr = subcribers[e];
        if (!arr) {
            arr = [];
            subcribers[e] = arr;
        }
        arr.push(cb);
    };

    SubPub.unsub = function(e, cb) {
        var arr = subcribers[e];
        if (!arr) {
            return;
        }
        subcribers[e] = _.without(arr, cb);
    }

    SubPub.pub = function(e, data) {
        var arr = subcribers[e];
        if (!arr) {
            return;
        }
        for (var i = 0; i < arr.length; i++) {
            arr[i](data);
        }
    };
})();