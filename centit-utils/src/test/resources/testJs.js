function runOpt(bizModel) {
    var resultList = [];
    for(var i=0;i<5;i++){
      var splitObj = {};
      splitObj.a = "hello" + i;
      splitObj.b = "world" + i;
      resultList.push(splitObj);
    }
    return resultList;
}
