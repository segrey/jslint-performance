/*global params:true*/

function dumpObj(obj) {
    "use strict";
    var str = "", key;
    for (key in obj) {
        str += "{" + key + "->" + obj[key] + "}";
    }
    return obj.toString() + " of type " + (typeof obj) + " with properties " + str;
}

function runLinter(linter) {
    "use strict";
    var options = {};
    var _options = String(params[0]);
    var _source = String(params[1]);
    var optionsArray = _options.split(',');


    for (var i = 0; i < optionsArray.length; ++i) {
        var o = optionsArray[i];
        var index = o.indexOf(':');
        var key = o.substr(0, index);
        var valueString = o.substr(index + 1);
        var value = "true".equals(valueString) ? true:"false".equals(valueString) ? false:valueString.replace('^',',');

        if ("predef".equals(key)) {
            var predefArray = value.split(',');
            var objectLiteralValue = {};

            for(var j = 0; j < predefArray.length; ++j) {
                var predefName = predefArray[j];
                while(predefName.charAt(0) == ' ') predefName = predefName.substring(1);
                var index2 = predefName.indexOf(':');
                objectLiteralValue[predefName.substr(0, index2 != -1 ? index2:predefName.length)] = index2 != -1 ? "true".equals(predefName.substr(index2 + 1)):false;
            }
            value = objectLiteralValue;
        }
        options[key] = value;
    }
    linter(_source, options);

    var result = "";
    for (var i = 0; i < linter.errors.length; ++i) {
        var m = linter.errors[i];
        if (m == null) break;
        if (result.length > 0) result += "\n";
        result += m.line + "^^^" + m.character + "^^^" + m.reason;
    }

    return result;
}
