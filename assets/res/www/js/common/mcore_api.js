/**
 * @파일명 : mcore_api.js
 * @생성일 : 2020.03.24
 * @작성자 : 김동창
 */

// Navigator 관련 API
var MNavigator = {
	os : function(os) {
		if (arguments.length == 1) {
			return M.navigator.os(os);
		} else {
			return M.navigator.os();
		}
	},
	device : function(os, version) {
		if (arguments.length == 2) {
			return M.navigator.device(os, version);
		} else if (arguments.length == 1) {
			return M.navigator.device(os);
		} else if (arguments.length == 0) {
			return M.navigator.device();
		}
	},
	browser : function(name, version) {
		if (arguments.length == 2) {
			return M.navigator.browser(name, version);
		} else if (arguments.length == 1) {
			return M.navigator.browser(name);
		} else if (arguments.length == 0) {
			return M.navigator.browser();
		}
	}
};


// PAGE 관련 API
var MPage = {
	info : function(keyPath) {
		return M.page.info(keyPath);
	},
	defer : function(enabled, time) {
		if (enabled) {
			M.page.defer(enabled, time);
		} else {
			M.page.defer(enabled);
		}
	},
	html : function(options) {
		// 화면 이동시 공통 처리
		var _options = options;
		M.page.html(_options);
	},
	back : function(options) {
		var _options = options;
		M.page.back(_options);
	},
	remove : function(url) {
		M.page.remove(url);
	},
	replace : function(options) {
		var _options = options;
		M.page.replace(_options);
	},
	activity : function(options) {
		var _options = options;
		if (MInfo.device().os.name != "pc") {
			M.page.activity(_options);
		} else {
			MPopup.alert("PC 에뮬레이터에서는 웹뷰 기능을 제공하지 않습니다.");
		}
	}
};


// DATA 관련 API
var MData = {
	param : function(key, value) {
		if (arguments.length == 0) {
			return M.data.param() || {};
		} else if (arguments.length == 1) {
			return M.data.param(key);
		} else if (arguments.length == 2) {
			M.data.param(key, value);
		}
	},
	removeParam : function(key) {
		if (arguments.length == 0) {
			M.data.removeParam();
		} else if (arguments.length == 1) {
			M.data.removeParam(key);
		}
	},
	global : function(key, value) {
		if (arguments.length == 0) {
			return M.data.global() || {};
		} else if (arguments.length == 1) {
			return M.data.global(key);
		} else if (arguments.length == 2) {
			M.data.global(key, value);
		}
	},
	removeGlobal : function(key) {
		if (arguments.length == 0) {
			M.data.removeGlobal();
		} else if (arguments.length == 1) {
			M.data.removeGlobal(key);
		}
	},
	encStorage : function(key, value) {
		if (arguments.length == 1) {
			var _getKey = key;
			if (MInfo.device().os.name != 'pc') {
				_getKey = Base64.encode(key);
				var returnVal = exWNDecryptString(M.data.storage(_getKey));
				try {
					if (typeof JSON.parse(returnVal) == "object") {
						return JSON.parse(returnVal);
					} else {
						return returnVal;
					}
				} catch (e) {
					return returnVal;
				}
			} else {
				return M.data.storage(_getKey);
			}
		} else if (arguments.length == 2) {
			var _setKey = key;
			var _setValue = value;
			if (MInfo.device().os.name != 'pc') {
				_setKey = Base64.encode(key);
				if (typeof value == "object") {
					value = JSON.stringify(value);
				}
				_setValue = exWNEncryptString(value);
			}
			M.data.storage(_setKey, _setValue);
		}
	},
	encRemoveStorage : function(key) {
		if (arguments.length == 1) {
			var _getKey = key;
			if (MInfo.device().os.name != 'pc') {
				_getKey = Base64.encode(key);
			}
			M.data.removeStorage(_getKey);
		}
	},
	storage : function(key, value) {
		if (arguments.length == 1) {
			return M.data.storage(key);
		} else if (arguments.length == 2) {
			M.data.storage(key, value);
		}
	},
	removeStorage : function(key) {
		M.data.removeStorage(key);
	}
};


// AES 128
var MSec = {
	encrypt : function(value) {
		var encObj = M.sec.encrypt(value);
		return encObj.status == "SUCCESS" ? encObj.result : "";
	},
	decrypt : function(value) {
		var decObj = M.sec.decrypt(value);
		return decObj.status == "SUCCESS" ? decObj.result : "";
	}
};


var MInfo = {
	version : function() {
		return M.info.version();
	},
	memory : function() {
		return M.info.memory();
	},
	device : function(keyPath) {
		if (arguments.length == 0) {
			return M.info.device();
		} else if (arguments.length == 1) {
			return M.info.device(keyPath);
		}
	},
	stack : function(index) {
		if (arguments.length == 0) {
			return M.info.stack();
		} else if (arguments.length == 1) {
			return M.info.stack(index);
		}
	}
};


var MApps = {
	browser : function(url, encoding) {
		M.apps.browser(url, encoding || "UTF-8");
	},
	remove : function(packageName) {
		if (MInfo.device.toLowerCase() == "android") {
			M.apps.remove(packageName);
		} else {
			MPopup.alert("지원하지 않는 OS 입니다.");
		}
	},
	info : function(packageNm) {
		return M.apps.info(packageNm);
	},
	open : function(packageNm, params) {
		if (arguments.length == 1) {
			M.apps.open(packageNm);
		} else {
			M.apps.open(packageNm, params);
		}
	},
	store : function(packageNm) {
		M.apps.store(packageNm);
	}
};


var MSys = {
	call : function(telNum) {
		M.sys.call(telNum);
	}
};



/**
 * 
 * Addon Api
 * 
 */
var MMedia = {
	camera : function(options) {
		M.media.camera({
			path : options.path || "/",
			mediaType : options.mediaType || "PHOTO",
			saveAlbum : options.saveAlbum || false,
			callback : function(status, result, option) {
				if (options.callback) {
					options.callback(status, result, option);
				}
			}
		});
	},
	album : function(options) {
		if (!options.path) {
			MPopup.alert("이미지 경로를 입력해주세요.");
			return;
		}
		M.media.album({
			path : options.path,
			onfinish : function(status, result, option) {
				if (options.callback) {
					options.callback(status, result, option);
				}
			}
		});
	},
	info : function(options) {
		M.media.info({
			path : options.path || "/",
			mediaType : options.mediaType || "PHOTO",
			callback : function(status, result) {
				if (options.callback) {
					options.callback(status, result);
				}
			}
		});
	},
	picker : function(options) {
		M.media.picker({
			mode : options.mode || "SINGLE",
			media : options.media || "PHOTO",
			path : options.path || null,
			column : options.column || 3,
			callback : function(status, result) {
				if (options.callback) {
					options.callback(status, result);
				}
			}
		});
	},
	optimize : function(options) {
		M.media.optimize({
			source : options.source,
			destination : options.destination,
			overwrite : options.overwrite || true,
			quality : options.quality || "0.5",
			format : options.format || "JPG",
			callback : function(result) {
				if (options.callback) {
					options.callback(result);
				}
			}
		});
	},
	crop : function(options) {
		M.media.crop({
			source : options.source,
			destination : options.destination,
			dimension : {
				width : options.width,
				height : options.height
			},
			optimize : options.optimize || 0.8,
			format : options.format || "JPG",
			callback : function(result) {
				if (options.callback) {
					options.callback(result);
				}
			}
		});
	}
};

var MPlugin = {
	QR : function(options){
		M.plugin('qr').scan({
	        flash: false,
	        fadeToggle: true,
	        orientation: 'LANDSCAPE',
	        onscan: function( result ) {
	        	if ( result.status === 'NS' ) {
                    console.log('This QRCode Plugin is not supported');
                }
                else if ( result.status === 'CANCEL' ) {
                	console.log('Scanning is canceled');
                }
                else if ( result.status !== 'SUCCESS' ) {
                	if ( result.message ) {
                		M.pop.alert( result.status + ":" + result.message );
					}
					else {
						console.log( 'This QRCode Plugin is failed' );
					}
				}
				else {
					if ( result.text ) {
						console.log( result.format + " :: " + result.text );
						if (options.callback) {
							options.callback(result);
						}
					}
					else {
						console.log( 'QRCode data is not scanning' );
					}
				}
	        }
		});
	}
}


var MNet = {
	httpSend : function(options) {
		$('.full_loding').hasClass('none') ? $('.full_loding').removeClass('none') : ''
		var serverTarget = '';
		CONFIG.IS_DEV == true ? serverTarget = "STORE_DEV" : serverTarget = "STORE_REAL";

		M.net.http.send({
//			server : "STORE_DEV",	//STORE_LOCAL,
			server : serverTarget,
			path : options.path,
			timeout : options.timeout || 30000, // 추후 기본 값 변경
			data : options.sendData || {},
			indicator : {
				show : options.indicator || false
			},
			success : function(receivedData, setting) {
				if (options.callback) {
					options.callback(receivedData, setting);
//					!$('.full_loding').hasClass('none') ? $('.full_loding').addClass('none') : ''
				}
			},
			error : function(errorCode, errorMessage, setting) {
				// 추후 공통처리가 필요한 경우 해당 부분에 처리
				if (options.errCallback) {
					options.errCallback(errorCode, errorMessage);
//					!$('.full_loding').hasClass('none') ? $('.full_loding').addClass('none') : ''
				}
			}
		});
	},
	upload : function(options) {
		M.net.http.upload({
			url : options.path,
			headers : {}, // "content-type" :
							// "multipart/form-data;charset=utf-8","User-Agent"
							// : "Tridentr"
			params : options.sendData,
			body : options.files || [],
			encoding : 'UTF-8',
			timeout : options.timeout || 20000, // 추후 기본 값 변경
			indicator : {
				show : options.indicator || true
			},
			finish : function(statusCd, header, body, status, errMsg, setting) {
				if (statusCd == -1) {
					MPopup.alert('네트워크 연결 시 문제가 발생하였습니다. 잠시 후 다시 시도해 주십시오.');
					return false;
				}
				var code = receivedData.head.resultCode;
				if (code == "200") {
					options.callback(body);
				}
			},
			progress : function(total, current) {
			}
		});
	}
};

var MPopup = {
	alert : function(options) {
		if (typeof options == "string") {
			M.pop.alert({
				title : "알림",
				message : options,
				buttons : [ "확인" ],
				callback : function(idx) {
					if (options.callback) {
						options.callback(idx);
					}
				}
			});
		} else {
			M.pop.alert({
				title : "알림",
				message : options.message || "",
				buttons : [ "확인" ],
				callback : function(idx) {
					if (options.callback) {
						options.callback(idx);
					}
				}
			});
		}
	},
	confirm : function(options) {
		M.pop.alert({
			title : options.title || "알림",
			message : options.message || "",
			buttons : options.buttons || [ "확인", "취소" ],
			callback : function(idx) {
				if (options.callback) {
					options.callback(idx);
				}
			}
		});
	},
	date : function(options, callback) {
		M.pop.date({
			type : options.type || "YMD",
			initDate : options.initDate || moment().format("YYYYMMDD"),
			startDate : options.startDate || "19000101",
			endDate : options.endDate || "21001231"
		}, function(result, setting) {
			if (callback) {
				callback(result, setting);
			}
		});
	},
	listSingle : function(options, callback) {
		if (!options.list) {
			this.alert("목록을 세팅해주세요.");
			return;
		}
		M.pop.list({
			mode : "SINGLE",
			title : options.title || "알림",
			buttons : options.buttons || [ "취소", "확인" ],
			list : options.list || [],
			selected : options.selected || 0
		}, function(buttonIdx, rowInfo, setting) {
			if (callback) {
				callback(buttonIdx, rowInfo, setting);
			}
		})
	},
	listMulti : function(options, callback) {
		if (!options.list) {
			this.alert("목록을 세팅해주세요.");
			return;
		}
		M.pop.list({
			mode : "MULTI",
			title : options.title || "알림",
			buttons : options.buttons || [ "취소", "확인" ],
			list : options.list || [],
			selected : options.selected || [ 0 ]
		}, function(buttonIdx, rowInfo, setting) {
			if (callback) {
				callback(buttonIdx, rowInfo, setting);
			}
		});
	},
	instance : function(param) {
		if (typeof param == "object") {
			M.pop.instance({
				message : param.message,
				time : param.time || "SHORT"
			});
		} else {
			M.pop.instance({
				message : param
			});
		}
	}
};


/**
 * Pattern 간편인증
 */
var ptCert = {
	/*
	 * 종합 패턴인증
	 * Pattern 인증 제품에 대한 3rd Party연동 기능을 제공한다
	 * M.plugin("3rd_iOS_pattern_basic").lock
	 * @param {object} object 설정 정보
	 * @param {string} object.type 패턴 타입을 잠금으로 설정
	 * @param {function} object.callback 결과 콜백 함수
	 */
	lock : function(callbackFunc){
		if(MNavigator.device("ios")){
			M.plugin("3rd_iOS_pattern_basic").lock({
				type : "lock",
				callback : function(status, result){
					callbackFunc(status, result);
				}
			});
		}else{
			M.execute("exWNPatternLockActivity", {
				type : "lock",
				callback : M.response.on( function(status, result){
					console.log(status);
					console.log(result);
					callbackFunc(status, result);
				}).toString()
			});
		}
	},

	reset : function(callbackFunc){
		if(MNavigator.device("ios")){
			M.plugin("3rd_iOS_pattern_basic").lock({
				type: 'reset',
				callback: function(status, result){
					callbackFunc(status, result);
				}
			});
		}else{
			M.execute("exWNPatternLockActivity", {
				type: "reset",
				callback: M.response.on( function( status, result ) {
					// 패턴 재설정 및 인증 성공
					console.log("result message : " + result.message);
					callbackFunc(status, result);
				}).toString()
			});
		}
	}
}


/**
 * 지문 간편인증
 */
var bioCert = {
	auth : function(callbackFunc){
		if(MNavigator.device("ios")){
			M.plugin('3rd_iOS_fingerprint_basic').auth({
				callback: function(status, result) {
					callbackFunc(status, result);
				}
			});
		}else{
			M.plugin('3rd_fingerprint_basic').auth({
				callback: function(status, result) {
					callbackFunc(status, result);
				}
			});
		}
	},

	check : function(callbackFunc){
		if(arguments.length == 0){
			if(MNavigator.device("ios")){
				return M.plugin('3rd_iOS_fingerprint_basic').check();
			}else{
				return M.plugin('3rd_fingerprint_basic').check();
			}
		}else{
			if(MNavigator.device("ios")){
				M.plugin('3rd_iOS_fingerprint_basic').check({
					callback:function(status,result){
						callbackFunc(status, result);
					}
				});
			}else{
				M.plugin('3rd_fingerprint_basic').check({
					callback:function(status,result){
						callbackFunc(status, result);
					}
				});
			}
		}
	},

	moveSetting : function(){
		if(MNavigator.device("ios")){
			M.plugin('3rd_iOS_fingerprint_basic').moveSetting();
		}else{
			M.plugin('3rd_fingerprint_basic').moveSetting();
		}
	},
}


/**
 * 핀번호 인증
 */
var pinCert = {
	register : function(callbackFunc){
		if(MNavigator.device("ios")){
			var params = {};
			params.length = '4';
			params.activity = 'SamplePassCodeViewController';

			M.plugin('3rd_iOS_pin_basic').register({
				param: params,
				callback:function(status, result) {
					callbackFunc(status, result);
				}
			});
		}else{
			M.execute("exWNPinLockActivity", {
				length : 4,
				type: 'REGISTER',
				callback: M.response.on( function( status, result ) {
					console.log(status);
					console.log(result);
					callbackFunc(status, result);
				}).toString()
			});
		}
	},

	remove : function(callbackFunc){
		if(MNavigator.device("ios")){
			M.plugin('3rd_iOS_pin_basic').remove({
				callback:function(status, result) {
					callbackFunc(status, result);
				}
			});
		}else{
			M.plugin('3rd_pin_basic').remove({
				callback:function(status, result) {
					callbackFunc(status, result);
				}
			});
		}
	},

	get : function(callbackFunc){
		if(MNavigator.device("ios")){
			M.plugin('3rd_iOS_pin_basic').get({
				callback:function(status, result) {
					callbackFunc(status, result);
				}
			});
		}else{
			M.plugin('3rd_pin_basic').get({
				callback:function(status, result) {
					callbackFunc(status, result);
				}
			});
		}
	},

	auth : function(callbackFunc){
		if(MNavigator.device("ios")){
			var params = {};
			params.length = '4';
			params.activity = 'SamplePassCodeViewController';

			M.plugin('3rd_iOS_pin_basic').auth({
				param: params,
				callback:function(status, result) {
					callbackFunc(status, result);
				}
			});
		}else{
			M.execute("exWNPinLockActivity", {
				length : 4,
				type: 'AUTH',
				callback: M.response.on( function( status, result ) {
					console.log(status);
					console.log(result);
					callbackFunc(status, result);
				}).toString()
			});
		}
	}
}

/**
 * 모피어스 화면 상태 이벤트 공통 영역
 * 
 */
M.onReady(function(event) {
	if (typeof MStatus.onReady == "function") {
		commUI();

		MStatus.onReady(event);
	}
});
M.onHide(function(event) {
	if (typeof MStatus.onHide == "function") {
		// M.onHide 공통 처리
		MStatus.onHide(event);
	}
});
M.onRestore(function(event) {
	if (typeof MStatus.onRestore == "function") {
		// M.onRestore 공통 처리
		MStatus.onRestore(event);
	}
});
M.onBack(function(event) {
	if(MInfo.stack().length == 1){
		MPopup.confirm({
			message : "앱을 종료하시겠습니까?",
			callback : function(idx){
				if(idx == 0){
					M.sys.exit();
				}
			},
		});
	}
	else{
		if(typeof MStatus.onBack == "function"){
			// M.onBack 공통 처리
			MStatus.onBack();
		}else{
			MPage.back();
		}
	}
});
M.onDestroy(function(event) {
	if (typeof MStatus.onDestroy == "function") {
		// M.onDestroy 공통 처리
		MStatus.onDestroy(event);
	}
});
M.onPause(function(event) {
	if (typeof MStatus.onPause == "function") {
		// M.onPause 공통처리
		MStatus.onPause(event);
	}
});
M.onResume(function(event) {
	if (typeof MStatus.onResume == "function") {
		// M.onResume 공통처리
		MStatus.onResume(event);
	}
});