/**
 * SKIMP-FR-003.js
 * @ 로그인
 * 2021.05.21
 */

var failCnt = '';	//로그인 시도 횟수 카운트
var bioFailCnt = '';	//생체인증 시도 횟수 카운트
var patternFailCnt = '';	//패턴인증 시도 횟수 카운트
var pinFailCnt = '';	//핀인증 시도 횟수 카운트

var pageInit = function(){

	//로그인 5회 실패시 5분동안 로그인 사용못하도록 처리
	if(!StringUtil.isEmpty(MData.storage('timerMinute'))){
		if(timerDiffTime()){
			openPopup('layer02', setTimer());
			failCnt = 0;
		}
	}else{
		easyLoginUseCheck();
	}

	pageEvent();
	
	$('#appVersion').html('앱 버전 : ' + M.info.app().app.version);
	$('#resourceVersion').html('리소스 버전 : ' + M.info.app().resource.current_version);
	$('#appVersion, #resourceVersion').removeClass('none'	);
	
	$('#userId, #userPw').val('');
};

var pageEvent = function(){

	//아이디와 비밀번호 모두 입력시 로그인버튼 활성화
	$(document).on('keyup', '#userId, #userPw', function(e){
		//아이디 입력수 숫자, 영어만 가능
		if($(e.target).attr('id') == "userId"){
			var thisText = $(e.target).val();
			var checkId = /^[A-Za-z0-9+]*$/.test(thisText);

			if(!checkId){
				$(e.target).val( $(e.target).val().slice(0,-1));
			}
		}

		if(!StringUtil.isEmpty($('#userId').val()) && !StringUtil.isEmpty($('#userPw').val())){
			$('#btnLogin').removeAttr('disabled');
		}else{
			$('#btnLogin').attr('disabled', true);
		}
	});

	//로그인버튼
	$(document).on('click', '#btnLogin', function(){
		if(StringUtil.isEmpty($('#userId').val())){
			MPopup.alert("아이디를 입력해주세요.");
		}else if(StringUtil.isEmpty($('#userPw').val())){
			MPopup.alert("비밀번호를 입력해주세요.");
		}else{
			login();
		}
	});
};


//앱 종료시 타이머시간과 경과시간의 차이 계산
var timerDiffTime = function(){
	var endTime = Number(MData.storage('endTime'));
	var curTime = Number(moment().format('YYYYMMDDHHmmss'));
	//현재시간에서 앱종료시 남은시간과의 차이
	var diffTime = curTime - endTime;

	//로그인 5회 실패 이후 5분 경과했을 때
	if(diffTime >= 300){
		MData.removeStorage('timerMinute');
		MData.removeStorage('timerSecond');
		MData.removeStorage('endTime');

		return false;
	}else{
		var timerMinute = 0;
		var timerSecond = 0;

		//재시작까지 남은 분이 diffTime 보다 작은 경우
		if(Number(MData.storage('timerMinute')) < parseInt(diffTime/60)){
			MData.removeStorage('timerMinute');
			MData.removeStorage('timerSecond');
			MData.removeStorage('endTime');

			return false;
		}
		//재시작까지 남은 분이 diffTime 과 같고 남은 초가 diffTime 보다 작은 경우
		else if(Number(MData.storage('timerMinute')) == parseInt(diffTime/60) && Number(MData.storage('timerSecond')) < (diffTime%60)){
			MData.removeStorage('timerMinute');
			MData.removeStorage('timerSecond');
			MData.removeStorage('endTime');

			return false;
		}
		//재시작까지 남은 시간이 diffTime 보다 큰 경우
		else{
			//종료시 남은 초가 diffTime 보다 작으면 1분 내리고 60초 추가
			if(Number(MData.storage('timerSecond')) < diffTime%60){
				timerMinute = Number(MData.storage('timerMinute')) - parseInt(diffTime/60) -1;
				timerSecond = Number(MData.storage('timerSecond')) - diffTime%60 + 60;
			}else{
				timerMinute = Number(MData.storage('timerMinute')) - parseInt(diffTime/60);
				timerSecond = Number(MData.storage('timerSecond')) - diffTime%60;
			}

			MData.storage('timerMinute', timerMinute);
			MData.storage('timerSecond', timerSecond);

			return true
		}
	}
};


//생체인증 사용여부 판단
var easyLoginUseCheck = function(){
	//간편로그인이 사용으로 되어 있을 경우 마지막 사용한 간편인증 방법 불러옴
	if(MData.storage('useEasyLogin') == "Y"){
		
		if(MData.storage('lastLoginType') == "BIO"){
			bioCert.auth((status, result) => {
				if(result.message == "SUCCESS" || result.result == "SUCCESS"){
					//마지막 로그인 방법 표시
					MData.storage('lastLoginType', 'BIO');
					//생체인증 데이터 있음으로 등록
					MData.storage('BioLoginData', 'Y');
					//간편 로그인 사용함으로 등록
					MData.storage("useEasyLogin", 'Y');
					//로그인 함수 호출
					login();
				}else{
					if (bioFailCnt <= 5) {
						//생체인증 횟수 증가 +1
						bioFailCnt++;
						MPopup.confirm({
							message : "생체 인증 인식에 실패하였습니다. 다시 시도하시겠습니까?", 
							buttons : ["재시도", "닫기"],
							callback : function(idx){
								if(idx == 0){
									easyLoginUseCheck();
								}
							},
						})
					} else {
						MPopup.confirm({
							message : "생체 인증 시도 5회 초과로 기존 저장된 데이터는 삭제됩니다. 생체인증 재등록 후 사용해 주세요.", 
							buttons : ["확인"],
							callback : function(idx){
								if(idx == 0){
									MData.storage('BioLoginData', '');
									MData.storage('lastLoginType', '')
								}
							},
						})
					}
				}
			});
		}
		
		else if(MData.storage('lastLoginType') == "PATTERN"){
			ptCert.lock((status, result) => {
				if(result.message == "SUCCESS" || result.result == "SUCCESS"){
					//마지막 로그인 방법 표시
					MData.storage('lastLoginType', 'PATTERN');
					//패턴인증 데이터 있음으로 등록
					MData.storage('PatternLoginData', 'Y');
					//간편 로그인 사용함으로 등록
					MData.storage("useEasyLogin", 'Y');
					//로그인 함수 호출
					login();
				} else if(result.result == 'REMOVE' || result.message == 'Invalid Pattern.') {
					MPopup.confirm({
						message : "패턴인증 시도 5회 초과로 기존 저장된 데이터는 삭제됩니다. 패턴인증 재등록 후 사용해 주세요.", 
						buttons : ["확인"],
						callback : function(idx){
							if(idx == 0){
								MData.storage('PatternLoginData', '');
								MData.storage('lastLoginType', '')
							}
						},
					})
				}
			});
		}
		
		else if(MData.storage('lastLoginType') == "PIN"){
			pinCert.auth((status, result) => {
				if(result.message == "SUCCESS" || result.result == "SUCCESS"){
					//마지막 로그인 방법 표시
					MData.storage('lastLoginType', 'PIN');
					//핀인증 데이터 있음으로 등록
					MData.storage('PinLoginData', 'Y');
					//간편 로그인 사용함으로 등록
					MData.storage("useEasyLogin", 'Y');
					//로그인 함수 호출
					login();

				} else if(result.result == 'FAIL') {
					MPopup.confirm({
						message : "핀인증 시도 5회 초과로 기존 저장된 데이터는 삭제됩니다. 핀인증 재등록 후 사용해 주세요.", 
						buttons : ["확인"],
						callback : function(idx){
							if(idx == 0){
								MData.storage('PinLoginData', '');
								MData.storage('lastLoginType', '')
							}
						},
					})
				}
			});
		}
	};
};


//로그인
var login = function(){
	var tokenData = MData.storage('encData').token === "" ? "" : MData.storage('encData').token;
	var endPwdData = MData.storage('encData').encPwd === "" ? "" : MData.storage('encData').encPwd;
	var simpleYnData = StringUtil.isEmpty($('#userId').val()) && StringUtil.isEmpty($('#userPw').val()) && MData.storage("useEasyLogin") == "Y" ? "Y" : "N";
	var platIdx = M.navigator.device().os == 'ios' ? '2' : '1' ;

//	var encId = M.execute('exWNAES256Encrypt', "ia02155");
//	var encPw = M.execute('exWNAES256Encrypt', "SK12345678");
	var encId = simpleYnData != 'Y' ? M.execute('exWNAES256Encrypt', $('#userId').val().toLowerCase())  : '';
	var encPw =simpleYnData != 'Y' ?  M.execute('exWNAES256Encrypt', $('#userPw').val()) : '';
	var encIdNative = simpleYnData != 'Y' ? M.execute('exWNAES256Encrypt', $('#userId').val().toLowerCase())  : M.execute('exWNAES256Encrypt', MData.storage('encData').userId);
	var encPwNative =simpleYnData != 'Y' ?  M.execute('exWNAES256Encrypt', $('#userPw').val()) : MData.storage('encData').encPwd;

	var mdnNum = '';
	if (M.navigator.device().os == 'iOS') {
		
	} else {
		
	}
	console.log('mdnNum : ' + mdnNum);
	
	var sendData = {
			token : tokenData,
            encPwd : endPwdData,
            userId : encId,			
            passwd : encPw,		
            osName : M.navigator.device().os,
            osVersion : M.navigator.device().version,
//            mdn : "3",
            mdn : mdnNum,
            pakgId :  M.info.app().app.id,    
            regularityYN : "N",
            simpleYN : simpleYnData,
            platIdx : platIdx,
	}
	console.log('sendData : ' + JSON.stringify(sendData));
	
	MNet.httpSend({
		path : "skimp/common/api/auth/SKIMP-0002",
		sendData : sendData,
		callback : function(rst, setting){
			
//			$('.full_loding').addClass('none');

			console.log(rst);
			if(rst.status == "1000"){
				
				//앱스토어 전문에서 사용할 encData 저장
				MData.storage('encData', {
					token : rst.token,
					encPwd : rst.encPwd,
					userId : rst.userId,
				});
				
				//개별앱에서 필요한 로그인 정보를 Native 에 저장
				M.execute('exWNSetUserInfo', {
					userId : encIdNative,
					encPwd : encPwNative,
					token : rst.token,
				});
				
				M.plugin('push').remote.registerServiceAndUser({
					cuid: rst.userId,
					name: rst.empNm,
					callback: function( status ) {
						var info = M.plugin('push').info();

						if (status.status == 'SUCCESS') {
							console.log('[' + info.CLIENT_UID + '/' + info.CLIENT_NAME + ']의 서비스/유저 등록을 성공 하였습니다.');
						}
						else {
							console.log('[' + info.CLIENT_UID + '/' + info.CLIENT_NAME + ']의 서비스/유저 등록을 실패 하였습니다.');
						}

                        if(!StringUtil.isEmpty(MData.global('appSchemeInfo'))){
                            if(!StringUtil.isEmpty(MData.global('appLoginType'))){
                                //업데이트 케이스일 경우
                                if(MData.global('appLoginType') == 'update'){
                                    //앱 리스트 페이지로 이동
                                    //리스트 페이지에서 스킴값으로 찾아서 해당 앱 상세 화면으로 별도 이동로직
                                    var scheme = MData.global('appSchemeInfo');
                                    MData.removeGlobal('appSchemeInfo');
                                    MData.removeGlobal('appLoginType');
                                    MPage.html({
                                        url : "SKIMP-FR-007.html",
                                        action : "CLEAR_TOP",
                                        param : {
                                            isGoDetail : "T",
                                            scheme : scheme
                                        }
                                    });
                                }
                                //로그아웃 되어있어서 로그인을 다시 하고 원래 앱으로 돌려줄 경우
                                else{
                                    if(wnIf.device == DT_ANDROID){
                                        M.apps.open(MData.global('appSchemeInfo'));
                                    }else{
                                        M.apps.open(MData.global('appSchemeInfo')+"://");
                                    }
                                    setTimeOut(function(){M.sys.exit();},500);
                                }
                            }
                        }else{
                            if (StringUtil.isEmpty(MData.storage('pushCallBack')) && StringUtil.isEmpty(MData.storage('tempLoc')) && StringUtil.isEmpty(MData.storage('tempExt'))) {
                                if(MData.storage('firstLogin') == "N"){
                                    MPage.html({
                                        url : "SKIMP-FR-007.html",
                                        action : "CLEAR_TOP",
                                    });
                                }else{
                                    MPage.html({
                                        url : "SKIMP-FR-004.html",
                                        action : "CLEAR_TOP",
                                    });
                                }
                            } else {
                                loginAuth();
                            }
                        }
					}
	            });
				
			} else if (rst.status == "3000") {
				failCnt++;
				if (failCnt >= 5) {
					openPopup('layer02', setTimer());			
					failCnt = 0;
				} else {
					MPopup.alert({
						message : "아이디 또는 비밀번호를 다시 확인해 주세요."
					});
				}
				$('.full_loding').addClass('none');
			} else {
				
				var msgCont = '';
				
				if(rst.status == "2000"){
					msgCont = "등록된 아이디가 없습니다. [확인] 버튼을 누르시면 로그인 페이지로 이동됩니다.";
				} else if (rst.status == "5000" || rst.status == "6000") {
					msgCont = "사용자 로그인 정보가 만료되었습니다. [확인] 버튼을 누르시면 로그인 페이지로 이동됩니다.";
				} else if (rst.status == "8000") {
					msgCont = "로그아웃된 상태입니다. [확인] 버튼을 누르시면 로그인 페이지로 이동됩니다.";
				} else { 
					msgCont = "사용자 인증 정보가 변경되었습니다. [확인] 버튼을 누르시면 로그인 페이지로 이동됩니다.";
				}
				
				MPopup.confirm({
					message : msgCont,
					buttons : ["확인"],
					callback : function(idx){
						if(idx == 0){
							$('.full_loding').addClass('none');

							MPage.html({
								url : "SKIMP-FR-001.html",
//								action : "CLEAR_TOP",
							});
						}
					},
				})
			}
		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
			$('.full_loding').addClass('none');
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
			MData.storage('timerMinute', $('.time').attr('timerMinute'));
			MData.storage('timerSecond', $('.time').attr('timerSecond'));
			MData.storage('endTime', moment().format('YYYYMMDDHHmmss'));
		},

		onResume : function(){
		}
}