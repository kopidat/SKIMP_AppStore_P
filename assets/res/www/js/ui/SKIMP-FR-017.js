/**
 * SKIMP-FR-017.js
 * @ 설정
 * 2021.05.24
 */
var pageInit = function(){
	$('#version').html(M.info.app().app.version);

	pageEvent();
};

var pageEvent = function(){

	//간편로그인 설정
	$(document).on('click', '#btnEasyLogin', function(){
		MPage.html({
			url : "SKIMP-FR-017-2.html",
		})
	});

	//로그아웃
	$(document).on('click', '#btnLogout', function(){
		MPopup.confirm({
			message : "로그아웃 하시겠습니까?",
			callback : function(idx){
				if(idx == 0){
					logOut();
				}
			},
		})
	});

	//알림함 이동
	$(document).on('click', '#alertMenu', function(){
		MPage.html({
			url : "SKIMP-FR-018.html",
		})
	});

	//footer 페이지 이동
	$(document).on('click', '#btnFooter button', function(){
		var thisId = $(this).attr('id');
		var thisPageUrl = "";

		if(thisId == "btnGroup"){
			thisPageUrl = "SKIMP-FR-007.html";
		}else if(thisId == "btnUpdate"){
			thisPageUrl = "SKIMP-FR-013.html";
		}else if(thisId == "btnNotice"){
			thisPageUrl = "SKIMP-FR-015.html";
		}else if(thisId == "btnSetting"){
			thisPageUrl = "SKIMP-FR-017.html";
		}

		if(thisId != "btnSetting"){
			MPage.html({
				url : thisPageUrl,
				animation : "NONE",
			})
		}
	});

};



var logOut = function(){
	MNet.httpSend({
		path : "skimp/common/api/SKIMP-0003",
		sendData : {
			token : MData.storage('encData').token,
		},
		callback : function(rst, setting){
			console.log(rst);
//			MPage.html({
//				url : "SKIMP-FR-003.html",
//				action : "CLEAR_TOP",
//			})
			M.sys.exit();
		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
		}
	});
};



var MStatus = {
		onReady : function(){
			pageInit();
		},

//		onBack : function(){
//
//		},

		onRestore : function(){

		},

		onHide : function(){

		},

		onDestroy : function(){

		},

		onPause : function(){

		},

		onResume : function(){
			console.log("onResume");
			fromBack = "fromBack";
			loginAuth();
		}
}