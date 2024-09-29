/**
 * @파일명 : common.js
 * @생성일 : 2021.05.21
 * @작성자 : 김동창
 */




/*************************************************************************************************
gw 로그인 인증 영역 start  ↓ ↓ ↓ ↓
*************************************************************************************************/

//로그인 인증
var loginAuth = function(ext, fromWhere){
	var platIdx = M.navigator.device().os == 'iOS' ? '2' : '1' ;
	var mdnNum = '';
	var targetExt = ext;
	var targetLoc= fromWhere;
	var fromBack = '';

	if (M.navigator.device().os == 'iOS') {
		
		M.plugin('push').notificationCenter.badge({
			badge: 0
		});

	} else {
		
	}
	console.log('mdnNum : ' + mdnNum);
	
	var sendData = {
			token : MData.storage('encData').token,
			encPwd : MData.storage('encData').encPwd,
            osName : M.navigator.device().os,
            osVersion : M.navigator.device().version,
//          mdn : "3",
            mdn : mdnNum,
            pakgId :  M.info.app().app.id,    
			regularityYN : "N",
			platIdx : platIdx,
	}
	console.log('sendData : ' + JSON.stringify(sendData));
	
	MNet.httpSend({
		path : "skimp/common/api/auth/SKIMP-0001",
		sendData : sendData,
		callback : function(rst, setting){
			console.log(rst);
			
			status = rst.status;
			
			$('.full_loding').addClass('none');
			
			var msgCont = '';
					
			if (rst.status == "1000") {
				console.log("정상 인증 완료 : " + rst.status);
				if (!StringUtil.isEmpty(targetLoc) || !StringUtil.isEmpty(MData.storage('tempLoc'))) {
                    loginAuthRst(targetExt, targetLoc);
				}
				setTimeout(function(){
				    if(!StringUtil.isEmpty(MData.global('appSchemeInfo'))){
                        if(!StringUtil.isEmpty(MData.global('appLoginType'))){
                            //업데이트 케이스일 경우
                            if(MData.global('appLoginType') == 'update'){
                                //앱 리스트 페이지로 이동
                                //리스트 페이지에서 스킴값으로 찾아서 해당 앱 상세 화면으로 별도 이동로직
                                var scheme = MData.global('appSchemeInfo');
                                MData.removeGlobal('appSchemeInfo');
                                MData.removeGlobal('appLoginType');
                                if (M.page.info().currentPage == "SKIMP-FR-007.html") {
                                    if($('.no-date').hasClass('none')){
                                        $("[data-scheme="+scheme+"]").siblings('div').click();
                                    }
                                }else{
                                    MPage.html({
                                        url : "SKIMP-FR-007.html",
                //                        action : "CLEAR_TOP",
                                        param : {
                                            isGoDetail : "T",
                                            scheme : scheme
                                        }
                                    });
                                }
                            }
                        }
                    }
				},500);
			} else {
				if(rst.status == "2000"){
					msgCont = "등록된 아이디가 없습니다. [확인] 버튼을 누르시면 로그인 페이지로 이동됩니다.";
				} else if (rst.status == "5000" || rst.status == "6000") {
					msgCont = "사용자 로그인 정보가 만료되었습니다. [확인] 버튼을 누르시면 로그인 페이지로 이동됩니다.";
				} else if (rst.status == "3000") {
					msgCont = "아이디 또는 비밀번호를 확인해 주세요. [확인] 버튼을 누르시면 로그인 페이지로 이동됩니다.";
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
								action : "CLEAR_TOP",
							});
						}
					},
				})
			}
			
			
//			if(rst.status == "1000"){
//				//개별앱에서 앱을 실행한 경우
//				if(!StringUtil.isEmpty(MData.global('appSchemeInfo'))){
//					M.apps.open(MData.global('appSchemeInfo').scheme+"://");
//				}else{
//					if(MData.storage('firstLogin') == "N"){
//						//개별앱에서 앱을 실행한 경우
//						MPage.html({
//							url : "SKIMP-FR-007.html",
//							action : "CLEAR_TOP",
//						});
//					}else{
//						MPage.html({
//							url : "SKIMP-FR-004.html",
//							action : "CLEAR_TOP",
//						});
//					}
//				}
//			} else {
//				MPopup.alert({
//					message : "다시 로그인 시도해 주시기 바랍니다."
//				});
//			}
			
		},
		errCallback : function(errCd, errMsg){
			$('.full_loding').addClass('none');

			console.log(errCd, errMsg);
		}
	});
}

var loginAuthRst = function(ext, targetLoc) {
	if (!StringUtil.isEmpty(MData.storage('tempLoc')) && MData.storage('tempLoc') == 'push' && !StringUtil.isEmpty(MData.storage('tempExt')) && MData.storage('tempExt') == 'app') {
		MData.storage('pushCallBack', '');
		MData.storage('tempLoc', ''); 
		MData.storage('tempExt', ''); 

		MPage.html({
			url : "SKIMP-FR-007.html",
			action : "CLEAR_TOP",
		});
		
	} else if (!StringUtil.isEmpty(MData.storage('tempLoc')) && MData.storage('tempLoc') == 'push' && !StringUtil.isEmpty(MData.storage('tempExt')) && MData.storage('tempExt') == 'typeKeyInvalid') {
		MData.storage('pushCallBack', '');
		MData.storage('tempLoc', ''); 
		MData.storage('tempExt', ''); 

		MPage.html({
			url : "SKIMP-FR-018.html",
			action : "CLEAR_TOP",
		});
		
	} else if (targetLoc == 'fromBack') {
		switch (M.page.info().currentPage) {
		case "SKIMP-FR-007.html":
			$('.btn-refresh').trigger('click');
			break;
		
		case "SKIMP-FR-013.html":
			$('.btn-refresh').trigger('click');
			break;

		case "SKIMP-FR-018.html":
			pushListSearch();
			break;	

		default:
			break;
		}
		
	} else {
		MPage.html({
			url : "SKIMP-FR-007.html",
			action : "CLEAR_TOP",
		});
	}
}

/*************************************************************************************************
gw 로그인 인증 영역 end ↑ ↑ ↑ ↑ 
*************************************************************************************************/




/*************************************************************************************************
개별앱 호출 영역 start  ↓ ↓ ↓ ↓
*************************************************************************************************/

var fn_storeopen = function(scheme, loginType) {
	console.log("fn_storeopen(scheme, loginType)");
	console.log("scheme / loginType = " + scheme + " / " + loginType);
//	alert("scheme / loginType = " + scheme + " / " + loginType);
	MData.global('appSchemeInfo', scheme);
	MData.global('appLoginType', loginType);
}

/*************************************************************************************************
개별앱 호출 영역 end ↑ ↑ ↑ ↑ 
*************************************************************************************************/




/*************************************************************************************************
간편 로그인 인증 호출 영역 start  ↓ ↓ ↓ ↓
*************************************************************************************************/


//디바이스 저장 파라미터 정의 : lastLoginType - 마지막 로그인 타입 / useEasyLogin - 간편인증 사용여부 / LoginData - 각각의 간편인증 데이터
var callSetSimpleLogin = function(thisId) {
	if (thisId == MData.storage('lastLoginType')) {
		return;
	} else {
		//라디오 버튼 전체 해제
		$('.simple-auth input').prop('checked', false);
		//재설정 버튼 전체 숨기기
		$('.simple-auth ul').addClass('none');
		//선택한 라디오 버튼 활성화
//		$(this).prop('checked', true);
		$('#'+thisId).prop('checked', true);

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
			        		
			        		$('#BIO').prop('checked', false);
			        		
//			        		//등록된 생체인증 정보가 없을 때, 설정 페이지로 이동 --> 다시 앱으로 돌아올 때, bioRegRechk() //지문인증 등록 후 재확인 요청 함수
//			    			if(M.navigator.device("ios")) {     // ios일 경우
//			    		        var result = M.plugin('3rd_iOS_fingerprint_basic').moveSetting();
//			    		        console.log('result : ' + JSON.stringify(result));
//			    		    } else if (M.navigator.device("android")) {     // android일 경우
//			    		        var result = M.plugin('3rd_fingerprint_basic').moveSetting();
//			    		        console.log('result : ' + JSON.stringify(result));
//			    		    }

			        	} else {
			        		$('#BIO').prop('checked', false);
			        		
			        		//인증 실패 시 화면 재설정
			        		resetScreen();
			        	}
			        }
				});
			}else{
				bioRegRechk();
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
									//패턴인증 변경 버튼 보이기
									$('#btnChangePattern').parent().parent('ul').removeClass('none');
									//마지막 로그인 방법 표시
									MData.storage('lastLoginType', 'PATTERN');
									//패턴인증 데이터 있음으로 등록
									MData.storage('PatternLoginData', 'Y');
									//간편 로그인 사용함으로 등록
									MData.storage("useEasyLogin", 'Y');
									MPopup.alert("패턴 인증이 설정되었습니다.");

									//인증 성공 시 데이터에 따른 화면처리
									setScreen();

								}else{
									$(this).parent().parent('ul').addClass('none');
									$('.simple-auth input').prop('checked', false);
									$('#Pattern').prop('checked', false);
									//패턴인증 데이터 없음으로 등록
									MPopup.alert("패턴 설정이 미인증 되었습니다.");
									
									//인증 실패 시 화면 재설정
									resetScreen();
								}
							});
						}else{
							$('.simple-auth input').prop('checked', false);
							
							//인증 실패 시 화면 재설정
							resetScreen();
						};
					}
				});
			}else{
				//패턴인증 사용가능할 경우, 패턴인증 사용 라디오 버튼 활성화
				$('#btnChangePattern').parent().parent('ul').removeClass('none');
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
									$('#PIN').prop('checked', false);
									MPopup.alert("핀번호 인증이 설정되었습니다.");

									//인증 성공 시 데이터에 따른 화면처리
									setScreen();
									
								}else{
									$(this).parent().parent('ul').addClass('none');
									$('.simple-auth input').prop('checked', false);
									$('#PIN').prop('checked', false);
									MPopup.alert("핀번호 설정이 미인증 되었습니다.");
									
									//인증 실패 시 화면 재설정
									resetScreen();
								}
								
								
							});
							
							$('#PIN').prop('checked', false);
							
							//인증 성공 시 데이터에 따른 화면처리
							setScreen();
							//인증 실패 시 화면 재설정
							resetScreen();
							
						}else{
							$('.simple-auth input').prop('checked', false);
							
							//인증 실패 시 화면 재설정
							resetScreen();
						};
					}
				});
			}else{
				//핀번호인증 사용가능할 경우, 핀번호인증 사용 라디오 버튼 활성화
				$('#btnChangePIN').parent().parent('ul').removeClass('none');
				//마지막 로그인 방법 표시
				MData.storage('lastLoginType', 'PIN');
			}
		}
	}
}


//생체인증 사용가능 여부 확인
var bioReChk = function(){
	bioCert.check((status, result) => {
		console.log(status);
		if(status == "SUCCESS"){
			if(result.result != "SUCCESS"){
				//402 리턴 값일 때, 생체인증 사용 가능한 기기라고 판단
				bioCertRstCd = result.result_code;
			}
			//생체인증 사용 라디오 버튼 활성화
			$('#BIO').removeClass('none');
			//생체인증 사용 문구 변경
			if (MData.storage('lastLoginType') != "BIO"){
				$('.un-used').first().html('(미등록)');
			} else {
				$('.un-used').first().html('');
			}
		}else{
			//생체인증 사용 라디오 버튼 비활성화
			$('#BIO').addClass('none');
			//생체인증 사용 문구 변경
			$('.un-used').first().html('(사용불가 기종)');
		}
	});
}


//지문인증 등록 후 재확인
var bioRegRechk = function(){
	bioCert.auth((state, result) => {
		//인증 성공일 때
		if(result.result == "SUCCESS"){
			//마지막 로그인 방법 표시
			MData.storage('lastLoginType', 'BIO');
			//생체인증 데이터 있음으로 등록
			MData.storage('BioLoginData', 'Y');
			//간편 로그인 사용함으로 등록
			MData.storage("useEasyLogin", 'Y');
			$('#BIO').prop('checked', true);
			MPopup.alert("생체 인증이 설정되었습니다.");

			//인증 성공 시 데이터에 따른 화면처리
			setScreen();

		}else{
			$(this).parent().parent('ul').addClass('none');
			$('.simple-auth input').prop('checked', false);
			//생체인증 데이터 있음으로 등록
			MData.storage('BioLoginData', '');
			MPopup.alert("생체 인증등록이 미인증 되었습니다.");
			
			//인증 실패 시 화면 재설정
			resetScreen();
		}
	});
}


//인증 실패 시 화면 재설정
var resetScreen = function(){
	//저장된 생체(BioLoginData), 패턴(PatternLoginData), 핀(PinLoginData) 데이터가 있을 경우
	if(!StringUtil.isEmpty(MData.storage('PatternLoginData')) || !StringUtil.isEmpty(MData.storage('PinLoginData'))  || !StringUtil.isEmpty(MData.storage('BioLoginData'))){
		//간편 인증 로그인을 사용으로 지정
		MData.storage("useEasyLogin", 'Y');
		switch (MData.storage('lastLoginType')) {
		//생체인증 데이터가 있을 경우
		case 'BIO':
			$('#BIO').prop('checked', true);
			break;
		//핀인증 데이터가 있을 경우
		case 'PIN':
			$('#PIN').prop('checked', true);
			$('#btnChangePIN').parent().parent('ul').removeClass('none');
			break;
		//패턴인증 데이터가 있을 경우
		case 'PATTERN':
			$('#Pattern').prop('checked', true);
			$('#btnChangePattern').parent().parent('ul').removeClass('none');
			break;
		//기본값
		default:
			//라디오 버튼 전체 해제
			$('.simple-auth input').prop('checked', false);
			break;
		}
	}else{
		//간편 인증 로그인을 미사용으로 지정
		MData.storage("useEasyLogin", 'N');
		//마지막 로그인한 간편 인증 없음으로 지정
		MData.storage('lastLoginType', '');
	}
}


//간편로그인 사용 체크
var easyLoginUseCheck = function(){
	if(MData.storage('useEasyLogin') == "Y" && (!StringUtil.isEmpty(MData.storage('PatternLoginData')) || !StringUtil.isEmpty(MData.storage('PinLoginData'))  || !StringUtil.isEmpty(MData.storage('BioLoginData')))){
		$('.login-simple input[type=checkbox]').prop('checked', true);

		//마지막 로그인 방법 표시
		if(MData.storage('lastLoginType') == "BIO"){
			$('#BIO').prop('checked', true);
		}else if(MData.storage('lastLoginType') == "PATTERN"){
			$('#Pattern').prop('checked', true);
			$('#btnChangePattern').parent().parent('ul').removeClass('none');
		}else if(MData.storage('lastLoginType') == "PIN"){
			$('#PIN').prop('checked', true);
			$('#btnChangePIN').parent().parent('ul').removeClass('none');
		}

		$('.simple-auth').show();
		
	}else{
	 	//간편인증 사용 비활성화
		$('.login-simple input[type=checkbox]').prop('checked', false);
		//간편인증 내역 숨기기
		$('.simple-auth').hide();
	}

	setScreen();
};


//인증 성공 시 데이터에 따른 화면처리
var setScreen = function(){
	//지문인증 데이터가 사용함 일 경우
	if(MData.storage('BioLoginData') == 'Y'){
		$('.simple-auth dl').eq(0).find('dt').text('생체인증 사용');
	}else{
		bioReChk(); //생체인증 사용가능 여부 확인
		// $('.simple-auth dl').eq(0).find('dt').html('생체인증 사용<em class="un-used">(미등록)</em>');
	}
	//패턴인증 데이터가 사용함 일 경우
	if(MData.storage('PatternLoginData') == 'Y'){
		$('.simple-auth dl').eq(1).find('dt').text('패턴인증 사용');
	}else{
		$('.simple-auth dl').eq(1).find('dt').html('패턴인증 사용<em class="un-used">(미등록)</em>');
	}
	//핀번호인증 데이터가 사용함 일 경우
	if(MData.storage('PinLoginData') == 'Y'){
		$('.simple-auth dl').eq(2).find('dt').text('핀번호 사용');
		if (MData.storage('lastLoginType') == "PIN") {
			$('#PIN').prop('checked', true);
		}
	}else{
		$('.simple-auth dl').eq(2).find('dt').html('핀번호 사용<em class="un-used">(미등록)</em>');
	}
}

/*************************************************************************************************
간편 로그인 인증 호출 영역 end ↑ ↑ ↑ ↑ 
*************************************************************************************************/




var commUI = function(){
	//뒤로가기
	$(document).on('click', '.prev', function(){
		M.page.back();
	});
}




//indicator 없는 화면에 추가
if (StringUtil.isEmpty($('body').find('.full_loding'))) {
	$('body').append('<div class="full_loding none"></div>');
}




//LayerPopup 오픈
var openPopup = function(layerId, afterFunction){
   	$("#"+layerId).remove();
   	$.get('Popup.html', function(item){
   		let templateHtml = $('<div></div>').append(item);
   		$('body').append( templateHtml.find('#'+layerId) );
   		if (afterFunction!=undefined && typeof afterFunction == "function") {
   			afterFunction();
   		}
   		layer_open(layerId);
   	});
}




//리스트 팝업에 맞게 데이터 변환
var getListPopArr = function(Arr, key, value) {
	var tmpArr = [];
	if(key || value) {
		for(var i = 0; i < Arr.length; i++){
			var tmpObj = {};
			tmpObj.title = Arr[i][key];
			tmpObj.value = Arr[i][value];
			tmpArr.push(tmpObj);
		}
	} else {
		for(var i = 0; i < Arr.length; i++){
			var tmpObj = {};
			tmpObj.title = Arr[i];
			tmpObj.value = Arr[i];
			tmpArr.push(tmpObj);
		}
	}

	return tmpArr;
};




//디바이스에 앱 설치여부, 설치된 앱 버전 정보를 앱 리스트에 추가
var addAppStatus = function(appList, os) {
    try {
        var appsInfo = appList || [];
        var os = os || 'iOS';

        appList.forEach((item, idx) => {
        	if(os == "iOS" || os == 'ios'){
        		appsInfo[idx].app_installed = M.apps.info(item.url_scheme).installed;

        		//ios 의 경우 타 앱의 버전을 가지고 올 수 없어서 조회된 앱정보의 버전을 storage 에 저장해두고 현재버전으로 사용
//        		StringUtil.isEmpty(MData.storage(item.package_nm)) ? MData.storage(item.package_nm, item.app_ver) : "";
        		var tempPkgNm = item.package_nm.split('.').join('_');
        		appsInfo[idx].installed_ver = MData.storage(tempPkgNm);
//        		appsInfo[idx].installed_ver = item.app_ver;
        	}else{
				appsInfo[idx].app_installed = M.apps.info(item.package_nm).installed;
				appsInfo[idx].installed_ver = M.apps.info(item.package_nm).version;
        	}
        });
    }
    catch(e) {
        console.error(e);
        WNLog('JS Exception', e.toString());
    }
    return appsInfo;
}




//리스트의 앱 버전과 설치된 앱의 버전을 비교
var appVerCompare = function(currentVer, installedVer){
	var cVer = Number(currentVer.split('.').join(''));		//현재버전
	var iVer = Number(installedVer.split('.').join(''));		//설치된버전

	if(iVer < cVer){
		return false;
	}else{
		return true;
	}
}




/*************************************************************************************************
비밀번호 5회 이상 틀릴 시, 타이머 계산 영역 start  ↓ ↓ ↓ ↓
*************************************************************************************************/

//타이머 동작
var setTimer = function(){
	var timerSecond = StringUtil.isEmpty(MData.storage('timerSecond')) ? 60 : Number(MData.storage('timerSecond'));
	var timerMinute = StringUtil.isEmpty(MData.storage('timerMinute')) ? 4 : Number(MData.storage('timerMinute'));

    var timerStart = setInterval(function(){
		//60초가 될때마다 1분 감소
		if(timerSecond == 0){
			timerMinute -= 1;
			timerSecond = 60;
			$('.time').text("0"+timerMinute+":00");
		}

		timerSecond -= 1;

		//분, 초가 한자리 수 일 때, 0 붙여서 표기
		if(String(timerMinute).length == 1){
			if(String(timerSecond).length == 1){
				$('.time').text("0"+timerMinute+":0"+timerSecond);
			}else{
				$('.time').text("0"+timerMinute+":"+timerSecond);
			}
		}else{
			if(String(timerSecond).length == 1){
				$('.time').text(timerMinute+":0"+timerSecond);
			}else{
				$('.time').text(timerMinute+":"+timerSecond);
			}
		}

		$('.time').attr('timerMinute', timerMinute).attr('timerSecond', timerSecond);

		//0분이되면 타이머 종료
		if(timerMinute == 0 && timerSecond == 0){
			clearInterval(timerStart);
			MData.removeStorage('timerMinute');
			MData.removeStorage('timerSecond');
			MData.removeStorage('endTime');

			$('#layer02').remove();
		}

	}, 1000);
}

/*************************************************************************************************
비밀번호 5회 이상 틀릴 시, 타이머 계산 영역 ↑ ↑ ↑ ↑ 
*************************************************************************************************/




/*************************************************************************************************
첨부파일 업로드 영역 start  ↓ ↓ ↓ ↓
*************************************************************************************************/

//문서변환 파일 업로드
var fileUploadTest = function(){
	var form = '<form id="uploadForm"></form/>';

	$('body').append(form);

	$('#uploadForm').append('<input type="file" name="file">');
	$('#uploadForm').attr('action', "http://dev-skimp.skinnovation.com/doc/file/upload?convertType=IMAGE&imageFormat=JPEG");

	var url = "http://dev-skimp.skinnovation.com/doc/file/upload";

	httpSend({
		url : url,
		method : "POST",
		formName : "uploadForm",
		contentType : false,
		callback : function(rst){
			console.log(rst);
			MData.global('docId', rst.id);
			fileConvertTest();
		}
	});
};


//문서변환 파일 컨버팅
var fileConvertTest = function(){
	var form = '<form id="convertForm"></form/>';
	var id = MData.global('docId');

	$('body').append(form);

	$('#convertForm').attr('action', "http://dev-skimp.skinnovation.com/doc/file/convert?id=19");

	var url = "http://dev-skimp.skinnovation.com/doc/file/convert/19";

	httpSend({
		url : url,
		method : "GET",
		formName : "convertForm",
		contentType : false,
		callback : function(rst){
			console.log(rst);
			if(rst.status != "S"){
				var start = setInterval(fileConvertTest, 1000);
			}else{
				clearInterval(start);


//				fileDownloadTest();
			}
		}
	});
}


//문서변환 파일정보 조회
var fileInfoTest = function(){
	var form = '<form id="infoForm"></form/>';
	var id = MData.global('docId');

	$('body').append(form);

	$('#uploadForm').append('<input type="file" name="file">');
	$('#infoForm').attr('action', "http://dev-skimp.skinnovation.com/doc/file/convert/info?id=22");

	var url = "http://dev-skimp.skinnovation.com/doc/file/convert/info?id=22";

	httpSend({
		url : url,
		method : "POST",
		formName : "infoForm",
		contentType : false,
		callback : function(rst){
			console.log(rst);

		}
	});
}


//문서변환 파일 다운로드
var fileDownloadTest = function(){
	var form = '<form id="downloadForm"></form/>';

	$('body').append(form);

//	$('#downloadForm').attr('action', "http://dev-skimp.skinnovation.com/doc/file/download");
	$('#downloadForm').append('<input type="hidden" name="stamp" value="o5VrQdO7SCYklQW90vasW9V8UzfQGXE5KW8suyFUbNDlfGZJIN17z2nmaie7x5Zx">');

	var url = "http://dev-skimp.skinnovation.com/doc/file/download/19/1.stream";

	httpSend({
		url : url,
		method : "POST",
		formName : "downloadForm",
		contentType : "multipart/form-data",
		callback : function(rst){
			console.log(rst);
		}
	})
}


//문서변환
var httpSend = function(opt){
	var form = $('#'+opt.formName)[0];
    var formData = new FormData(form);

    $.ajax({
    	crossOrigin : true,
        url: opt.url,
        data: formData,
        method: opt.method,
        contentType : opt.contentType,
        enctype : "multipart/form-data",
        dataType : 'json',
        processData : false,
        success : function (rst) {
            if(opt.callback){
            	$('#'+opt.formName).remove();
            	opt.callback(rst);
            };
        },
        error: function (err) {
        	console.log(err);
        	$('#'+opt.formName).remove();
            alert("실패하였습니다.");
        }
    });
}

/*************************************************************************************************
첨부파일 업로드 영역 ↑ ↑ ↑ ↑ 
*************************************************************************************************/




/*************************************************************************************************
푸시 영역 start  ↓ ↓ ↓ ↓
*************************************************************************************************/

//android push receiver
var onReceiveAndroidNotification = function(rst){
	console.log(rst);
}


//push receiver
var onReceiveNotification = function(rst){
    console.log("onReceiveNotification rst = " + rst);
    console.log("onReceiveNotification M.data.storage(pushcallback) = " + M.data.storage("pushcallback"));
    console.log("onReceiveNotification M.data.storage(payload) = " + M.data.storage("payload"));
    console.log("onReceiveNotification M.data.storage(testKey) = " + M.data.storage("testKey"));
    console.log("onReceiveNotification = " + JSON.stringify(rst));
	
	var tempExt = '';
	var ext = '';
	
	try {
	    tempExt = JSON.parse(JSON.parse(rst.payload).mps.ext);
	    console.log('tempExt : ' + tempExt);
//	    return (typeof json === 'object');
	    if ('type' in tempExt) {
			ext = tempExt.type == 'app' ? 'app' :'typeKeyInvalid';
		} else {
			ext = 'typeKeyInvalid'
		}
	} catch (e) {
		ext = 'typeKeyInvalid';
	}
	
	if (M.navigator.device().os == 'Android') {
		MData.storage('pushCallBack', rst.pushcallback);
	} else {
		if (rst.status != 'ACTIVE') {
			MData.storage('pushCallBack', rst.status);
		} else {
			MData.storage('pushCallBack', '');
		}
	}
	
	MData.storage('tempExt', ext);
	MData.storage('tempLoc', 'push');

	if (StringUtil.isEmpty(MData.storage('pushCallBack')) && M.page.info().currentPage != "SKIMP-FR-003.html") {
		loginAuth(ext, 'push');
	}
	
//	var result = loginAuth(ext, 'push');
//	console.log(result);	
}


//ios 푸시 설정을 위해 추가 true 로 변경시 앱에서 푸시 수신 가능
M.plugin('push').remote.enabled(true);


//ios push badge 초기화 (항상 0 으로 셋팅)
if (M.navigator.device().os == 'iOS' ||  M.navigator.device().os == 'ios' ) {
	M.plugin('push').notificationCenter.badge({
		badge: 0
	});
}

/*************************************************************************************************
푸시 영역 ↑ ↑ ↑ ↑ 
*************************************************************************************************/