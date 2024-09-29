/**
 * intro.js
 * @ 인트로
 * 2021.05.21
 */
var pageInit = function(){
	$('.version').html("Ver." + M.info.app().app.version);

	pageEvent();
};

var pageEvent = function(){
	//리소스업데이트
	resourceUpdate();
};

var chkFraud =function(){
	M.plugin('prevention').fraud(
			{

			        callback : function(event) {

			                console.log(event);

			                if (event.status === 'SUCCESS') {
			                        if (event.rooting === true) {
			                                console.log('루팅폰이 입니다.')
			                        } else {
			                                if (event.valid === false) {
			                                        alert('앱이 유효하지 않습니다\n계속 실행할 수 없어 종료합니다.');
			                                        M.sys.exit();
			                                } else {
			                                        console.log('유효한 앱입니다.');
			                                    	//리소스업데이트
			                                    	resourceUpdate();
			                                }
			                        }
			                } else {
			                        if (event.error) {
			                                console.log(event.error);
			                                alert('앱이 유효하지 않습니다\n계속 실행할 수 없어 종료합니다.');
			                                M.sys.exit();
			                        } else {
			                                console.log(JSON.stringify(event));
			                        }
			                }
			        }
			});
}

var resourceUpdate = function(){
	if(CONFIG.IS_DEV){
		$(".animation > span").each(function() {
			$(this).data("origWidth", $(this).width()).width(0) .animate({
				width: $(this).data("origWidth")
			}, 1000, function(){
				//개별앱에서 업데이트로 들어왔을 경우
				if(MData.global('appLoginType') == "update"){
					MPage.html({
						url : 'SKIMP-FR-007.html',
						action : "CLEAR_TOP",
					});
				}else{
					MPage.html({
						url : 'SKIMP-FR-003.html',
						action : "CLEAR_TOP",
					});
				}

			});
		});
	}else{
		M.net.res.update({
			finish : function(status, info, option) {
				switch (status) {
					// 리소스 업데이트 성공
					case 'SUCCESS':
						$(".progress-bar").css("width", '100%');


						MPage.html({
							url : 'SKIMP-FR-003.html',
							action : "CLEAR_TOP",
						});
						break;
						// 리소스 업데이트 성공 And Refresh
					case 'SUCCESS_AND_REFRESH':
	//	            	M.pop.alert("refresh");
						break;

					// 앱 권장 업데이트
					case 'RECOMMENDED_APP_UPDATING' :
						M.pop.alert({
						   title: '알림',
						   message: '앱 권장 업데이트가 있습니다.',
						   buttons: ['취소', '확인'],
						   callback: function(index) {
							  if(index == 1){
								  var appUrl = '';
			                         //개발일 경우
			                         if (CONFIG.IS_DEV) {
			                        	 appUrl = M.apps.browser('https://dev-gw-skimp.skinnovation.com/down/partner.jsp');
		                        	 //운영일 경우
									} else {
										appUrl = M.apps.browser('https://gw-skimp.skinnovation.com/down/partner.jsp');
									}
			                         
			                         M.apps.browser(appUrl);
							  }else{
								  MPage.html({
									url : 'SKIMP-FR-003.html',
									action : "CLEAR_TOP",
								  });
							  }
						   }
						});
						break;

					// 앱 강제 업데이트
					case 'FORCED_APP_UPDATING' :
						M.pop.alert({
						   title: '알림',
						   message: '설치된 앱이 낮은 버전입니다.\n업데이트 하시겠습니까?',
						   buttons: ['취소', '확인'],
						   callback: function(index) {
							  if(index == 1){
								  	//개발일 경우
			                         if (CONFIG.IS_DEV) {
			                        	 appUrl = M.apps.browser('https://dev-gw-skimp.skinnovation.com/down/partner.jsp');
		                        	 //운영일 경우
									} else {
										appUrl = M.apps.browser('https://gw-skimp.skinnovation.com/down/partner.jsp');
									}
			                         
			                         M.apps.browser(appUrl);							  
	                         }else{
								 M.sys.exit();
							  }
						   }
						});
						break;

					// 라이센스 체크 에러
					case 'LICENSE_IS_NOT_EXISTENCE':
					// 라이센스 무결성 회손
					case 'BROKEN_INTEGRITY_OF_LICENSE':
					// 라이센스 기간 만료
					case 'EXPIRED_LICENSE':
						M.pop.alert({
						   title: '알림',
						   message: '라이센스 에러입니다.',
						   buttons: ['취소', '확인'],
						   callback: function(index) {
							  if(index == 1){
								 M.net.res.retry();
							  }else{
								 M.sys.exit();
							  }
						   }
						});
						break;

					// 설치 메모리 부족
					case 'INSUFFICIENT_MEMORY':
						M.pop.alert({
						   title: '알림',
						   message: '설치 메모리가 부족합니다.',
						   buttons: ['취소', '확인'],
						   callback: function(index) {
							  if(index == 1){
								 M.net.res.retry();
							  }else{
								 M.sys.exit();
							  }
						   }
						});
						break;

					// 외장 메모리 카드 사용 오류
					case 'EXT_MEM_NOT_AVAIL':
						M.pop.alert({
						   title: '알림',
						   message: '외장 메모리 오류입니다.',
						   buttons: ['취소', '확인'],
						   callback: function(index) {
							  if(index == 1){
								 M.net.res.retry();
							  }else{
								 M.sys.exit();
							  }
						   }
						});
						break;

					// UNDEFINED ERROR
					default:
						M.pop.alert({
						   title: '알림',
						   message: '알 수 없는 오류입니다.',
						   buttons: ['취소', '확인'],
						   callback: function(index) {
							  if(index == 1){
								 M.net.res.retry();
							  }else{
								 M.sys.exit();
							  }
						   }
						});
						break;
				}
			},

			progress : function(total, read, remain, percentage, option) {
				var progressBarWidth = Math.max( Math.min( percentage, 100 ), 0 ) + "%";
				$(".animation > span").css("width", progressBarWidth);
	//	        $(".progress-percent").html( percentage + '%' );
			},

			error : function(errCode, errMsg, option) {
				M.debug.error("** error : ", errCode, errMsg);

				M.pop.alert({
				   title: '알림',
				   message: '알 수 없는 오류입니다.!'+errCode+" / "+errMsg,
				   buttons: ['취소', '확인'],
				   callback: function(index) {
					  if(index == 1){
//						 M.net.res.retry();
						  resourceUpdate();
					  }else{
						 M.sys.exit();
					  }
				   }
				});
			}
		});
	}

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

		}
}