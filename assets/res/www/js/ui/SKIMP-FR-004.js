/**
 * SKIMP-FR-004.js
 * @ 간편인증
 * 2021.05.21
 */

var bioCertRstCd = "";

var pageInit = function(){

	//생체인증 사용가능 여부 확인
	bioDoubleChk();

	pageEvent();
};

var pageEvent = function(){
	
	
	//간편로그인 설정
	$(document).on('click', '.simple-cert button', function(){
		var thisId = $(this).attr('id');
		
		setSimpleLogin(thisId); //간편 로그인 인증 호출
	});
	
	
	//건너뛰기 버튼
	$(document).on('click', '#btnNext', function(){
		login();
	});
};


var login = function(){
	//최초로그인 여부 저장
	MData.storage('firstLogin') == "N" ? "" : MData.storage('firstLogin', 'N');

	MPage.html({
		url : "SKIMP-FR-007.html",
		action : "CLEAR_TOP",
	})
}



//디바이스 저장 파라미터 정의 : lastLoginType - 마지막 로그인 타입 / useEasyLogin - 간편인증 사용여부 / LoginData - 각각의 간편인증 데이터
var setSimpleLogin = function(thisId) {
	if (thisId == MData.storage('lastLoginType')) {
		return;
	} else {
		//A.생체인증일 때
		if(thisId== "BIO"){
			//생체인증 사용가능 여부 확인 (402는 생체인증 가능한 디바이스)
			if(bioCertRstCd == "402"){
				M.pop.alert({
			        message: '디바이스에 등록된 생체인증 정보가 없습니다.\n디바이스 설정에서 등록해 주세요.',
			        buttons: ['확인', '취소'],
			        callback: function(idx) {
			        	if (idx == 0) {
			        		//생체인증 등록으로 앱 background 전환
			        		fromWhere = 'setBIO';
			        		
			        		//등록된 생체인증 정보가 없을 때, 설정 페이지로 이동 --> 다시 앱으로 돌아올 때, bioRegDoubleChk() //지문인증 등록 후 재확인 요청 함수
			    			if(M.navigator.device("ios")) {     // ios일 경우
			    		        var result = M.plugin('3rd_iOS_fingerprint_basic').moveSetting();
			    		        console.log('result : ' + JSON.stringify(result));
			    		    } else if (M.navigator.device("android")) {     // android일 경우
			    		        var result = M.plugin('3rd_fingerprint_basic').moveSetting();
			    		        console.log('result : ' + JSON.stringify(result));
			    		    }

			        	}
			        }
				});
			}else{
				bioRegDoubleChk();
			}
		}

		//B.패턴인증일 때	
		else if(thisId == "Pattern"){
			//패턴인증 사용가능 여부 확인
			if(StringUtil.isEmpty(MData.storage("PatternLoginData"))){
				MPopup.confirm({
					message : "등록된 패턴 인증 정보가 없습니다. 패턴 인증을 등록하시겠습니까?",
					callback : function(idx){
						if(idx == 0){
							//패턴인증 등록으로 앱 background 전환
		        			fromWhere = 'setPattern';

		        			//패턴인증 등록 호출 (reset)
							ptCert.reset((state, result) => {
								//인증 성공일 때
								if(result.result == "SUCCESS"){
									//마지막 로그인 방법 표시
									MData.storage('lastLoginType', 'PATTERN');
									//패턴인증 데이터 있음으로 등록
									MData.storage('PatternLoginData', 'Y');
									//간편 로그인 사용함으로 등록
									MData.storage("useEasyLogin", 'Y');
//									MPopup.alert("패턴 인증이 설정되었습니다.");
									
									login();
								}else{
									MPopup.alert("패턴 설정이 미인증 되었습니다.");
								}
							});
						}
					}
				});
			}else{
				//마지막 로그인 방법 표시
				MData.storage('lastLoginType', 'PATTERN');
			}
		}

		//C.핀번호인증일 때	
		else if(thisId == "PIN"){
			//핀번호인증 사용가능 여부 확인
			if(StringUtil.isEmpty(MData.storage("PinLoginData"))){
				MPopup.confirm({
					message : "등록된 핀번호 인증 정보가 없습니다. 핀번호 인증을 등록하시겠습니까?",
					callback : function(idx){
						if(idx == 0){
							//핀번호인증 등록으로 앱 background 전환
			        		fromWhere = 'setPin';

			        		//핀번호인증 등록 호출 (register)
							pinCert.register((status, result) => {
								//핀번호인증 성공일 때
								if(result.result == "SUCCESS"){
									//핀번호인증 변경 버튼 보이기
									$('#btnChangePIN').parent().parent('ul').removeClass('none');
									//마지막 로그인 방법 표시
									MData.storage('lastLoginType', 'PIN');
									//핀번호인증 데이터 있음으로 등록
									MData.storage('PinLoginData', 'Y');
									MData.storage("useEasyLogin", 'Y');
//									MPopup.alert("핀번호 인증이 설정되었습니다.");
									login();

								}else{
									MPopup.alert("핀번호 설정이 미인증 되었습니다.");
								}
							});
						}
					}
				});
			}else{
				//마지막 로그인 방법 표시
				MData.storage('lastLoginType', 'PIN');
			}
		}
	}
}


//생체인증 사용가능 여부 확인
var bioDoubleChk = function(){
	bioCert.check((status, result) => {
		console.log(status);
		if(status == "SUCCESS"){
			if(result.result != "SUCCESS"){
				//402 리턴 값일 때, 생체인증 사용 가능한 기기라고 판단
				bioCertRstCd = result.result_code;
			}
		}
	});
}


//지문인증 등록 후 재확인
var bioRegDoubleChk = function(){
	bioCert.auth((state, result) => {
		//인증 성공일 때
		if(result.result == "SUCCESS"){
			//마지막 로그인 방법 표시
			MData.storage('lastLoginType', 'BIO');
			//생체인증 데이터 있음으로 등록
			MData.storage('BioLoginData', 'Y');
			//간편 로그인 사용함으로 등록
			MData.storage("useEasyLogin", 'Y');
//			MPopup.alert("생체 인증이 설정되었습니다.");
			
			login();
		}else{
			//생체인증 데이터 있음으로 등록
			MData.storage('BioLoginData', '');
			MPopup.alert("생체 인증등록이 미인증 되었습니다.");
			
		}
	});
}

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
			mdmInstallChk();
		}
}