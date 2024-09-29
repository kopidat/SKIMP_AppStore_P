/**
 * SKIMP-FR-017-2.js
 * @ 간편로그인 설정
 * 2021.05.31
 */

var bioCertRstCd = "";
var fromWhere = "";

var pageInit = function(){
	pageEvent();

	easyLoginUseCheck();
	bioReChk();
};

var pageEvent = function(){

	//간편로그인 사용여부 토글
	$(document).on('click', '.login-simple input[type=checkbox]', function(){
		if($(this).is(':checked')) {
			$('.simple-auth').show();

			//상태가 on 이 되어도 간편인증 데이터가 없으면 상태값 N
			if(!StringUtil.isEmpty(MData.storage('PatternLoginData')) || !StringUtil.isEmpty(MData.storage('PinLoginData'))  || !StringUtil.isEmpty(MData.storage('BioLoginData'))){
				MData.storage("useEasyLogin", 'Y');
				easyLoginUseCheck();
			}else{
				MData.storage("useEasyLogin", 'N');
			}
		} else {
			$('.simple-auth').hide();
			MData.storage("useEasyLogin", 'N');
//			MData.removeStorage('lastLoginType');
		}
	});


	//간편로그인 설정
	$(document).on('click', '.simple-auth input', function(){
		var thisId = $(this).attr('id');
		
		callSetSimpleLogin(thisId); //간편 로그인 인증 호출
	});
	
	
	//패턴 재설정
	$(document).on('click', '#btnChangePattern', function(){
		MPopup.confirm({
			message : "패턴 인증 정보를 재설정 하시겠습니까?",
			callback : function(idx){
				if(idx == 0){
	        		fromWhere = 'setPattern';

					ptCert.reset((state, result) => {
						if(result.message == "SUCCESS" || result.result == "SUCCESS"){
							MData.storage('lastLoginType', 'PATTERN');
							MData.storage('PatternLoginData', 'Y');
							MData.storage("useEasyLogin", 'Y');
							setScreen();
							MPopup.alert("패턴 인증 재설정이 완료되었습니다.");
						}else{
							MPopup.alert("패턴 인증 재설정이 미인증 되었습니다. 기존 저장된 데이터는 삭제됩니다.");
							 MData.storage('lastLoginType', '');
							 MData.storage('PatternLoginData', '');
							$('#btnChangePattern').parent().parent('ul').addClass('none');
							//라디오 버튼 전체 해제
							$('.simple-auth input').prop('checked', false);
							resetScreen();
							setScreen();
						}
					});
				}
			}
		});
	});
	
	
	//PIN 재설정
	$(document).on('click', '#btnChangePIN', function(){
		MPopup.confirm({
			message : "핀번호 인증 정보를 재설정 하시겠습니까?",
			callback : function(idx){
				if(idx == 0){
	        		fromWhere = 'setPin';

					pinCert.register((status, result) => {
						if(result.message == "SUCCESS" || result.result == "SUCCESS"){ 
							MData.storage('lastLoginType', 'PIN');
							MData.storage('PinLoginData', 'Y');
							MData.storage("useEasyLogin", 'Y');
							setScreen();
							MPopup.alert("핀번호 인증 재설정이 완료되었습니다.");
						}else{
							MPopup.alert("핀번호 인증 재설정이 미인증 되었습니다. 기존 저장된 데이터는 삭제됩니다.");
							 MData.storage('lastLoginType', '');
							 MData.storage('PinLoginData', '');
							$('#btnChangePIN').parent().parent('ul').addClass('none');
							//라디오 버튼 전체 해제
							$('.simple-auth input').prop('checked', false);
							resetScreen();
							setScreen();
						}
					});
				}
			}
		});
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

//		if(thisId != "btnSetting"){
			MPage.html({
				url : thisPageUrl,
				animation : "NONE",
			})
//		}
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

			if (fromWhere == 'setBIO') {
				//지문인증 등록 후 재확인
				bioRegRechk();
				fromWhere = '';
				fromBack = "fromBack";
			} else if (fromWhere == 'setPattern' || fromWhere == 'setPin') {
				fromWhere = '';
				fromBack = "fromBack";

			} else {
				fromBack = "fromBack";
				mdmInstallChk();
			}			
		}
}