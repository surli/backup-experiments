(function() {
  var countdown, num;

  countdown = (function() {
    var i, results;
    results = [];
    for (num = i = 10; i >= 1; num = --i) {
      results.push(num);
    }
    return results;
  })();

}).call(this);