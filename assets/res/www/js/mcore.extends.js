
(function(window, undefined) {

var currentPagehtml = location.href.substring(location.href.lastIndexOf('/')+1, location.href.lastIndexOf('.'));
var thisFileName = "mcore.extends.js",

importFiles = [
	"wnInterface.extends.js",

	"libs/jquery-3.1.1.min.js",
	"libs/moment.min.js",

	"common/config.js",
	"common/mcore_api.js",
	"common/common_util.js",
	"common/common.js",
	"common/ui.js",

	"ui/"+currentPagehtml+".js",
];

M.ScriptLoader.writeScript( importFiles, M.ScriptLoader.scriptPath(thisFileName) );

})(window);