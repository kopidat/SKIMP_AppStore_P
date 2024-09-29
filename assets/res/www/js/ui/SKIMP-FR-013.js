/**
 * SKIMP-FR-013.js
 * @ 업데이트
 * 2021.05.24
 */


var categoryList = [];


var pageInit = function(evt){
	pageEvent();

	categoryListSearch(evt);
};

var pageEvent = function(){
	//검색버튼 이벤트
	$(document).on('click', '#srchTxt', function(){
		appListSearch();
	});
	
	//리스트 새로고침
	$(document).on('click', '.btn-refresh', function(evt){
//		categoryListSearch(evt);
		//앱 목록 조회
		appListSearch();
	});

	
	//앱 상세 이동
	$(document).on('click', 'ul li div', function(){
		MPage.html({
			url : 'SKIMP-FR-014.html',
			param : {
				appNo : $(this).attr('data-appNo'),
				platIdx : $(this).attr('data-plat_idx'),
				packageNm : $(this).attr('data-package_nm'),
				downUrl : $(this).siblings('button').attr('data-install-url'),
				appNm : $(this).siblings('button').attr('data-appNm'),
				appIdx : $(this).siblings('button').attr('data-appIdx'),
				sort : $(this).siblings('button').attr('data-sort'),
			}
		});
	});
	
	
	//앱설치 버튼
	$(document).on('click', '.btn-download, .btn-update', function(e){
		var thisInstallUrl = $(this).attr('data-install-Url');
		var thisAppName = $(this).attr('data-appNm');
		var thisAppIdx = $(this).attr('data-appIdx');

		//다운로드 버튼은 다운로드 카운트 추가 후 앱 설치
		if( $(e.target).attr('class') == "btn-download" ) {
			downloadCntCheck(thisAppIdx, thisInstallUrl, thisAppName);
		}else{
			if(MNavigator.device("ios")){
				M.apps.browser("itms-services://?action=download-manifest&url="+thisInstallUrl,"UTF-8")
			}else{
				M.apps.install(thisInstallUrl, thisAppName);
			}
		}
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

		if(thisId != "btnUpdate"){
			MPage.html({
				url : thisPageUrl,
				animation : "NONE",
			})
		}
	});

};


//카테고리 목록 조회
var categoryListSearch = function(evt){
	MNet.httpSend({
		path : "skimp/common/api/SKIMP-0016",
		sendData : {
			
		},
		callback : function(rst, setting){
			console.log(rst);
			$('#appList').html("");

			categoryList = getListPopArr(rst.categoryList, "catg_nm", "catg_cd");
			categoryList.unshift({"title" : "전체", "value" : ""});

			var categoryHtml = '';
			rst.categoryList.forEach((item, idx) => {
				categoryHtml += '<h3 data-cate-cd="'+item.catg_cd+'">'+item.catg_nm+'</h3>'
				categoryHtml += '<ul>'
				categoryHtml += '</ul>'
			});
			$('#appList').append(categoryHtml);
			
			//카테고리 숨기기
			$('#appList h3').addClass('none')

			appListSearch(evt);
		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
		}
	});
}



//앱 목록 조회
var appListSearch = function(evt){
	//os ios : 2, aos : 1
	var deviceOS = MNavigator.device("ios") ? "2" : "1";
	var osType = StringUtil.isEmpty(evt) ? M.navigator.device().os : evt.os;

	MNet.httpSend({
		path : "skimp/common/api/SKIMP-0005",
		sendData : {
			userId: MData.storage('encData').userId,
			platIdx : deviceOS,
			keyword : $('.input01').val(),
		},
		callback : function(rst, setting){
			console.log(rst);

			var appList = rst.appInfoList;
			var appListHtml = '';
			var appsInfo = addAppStatus(appList, osType);
			var updateAppCnt = 0;

			//기존에 그려진 리스트 초기화
			$('#appList li').remove();
			
			//#content 영역 숨기기
			$('#content > div').addClass('none');
			
			appsInfo.forEach((item, idx) => {
				//앱 설치여부에 따른 분기
				if(item.app_installed == true){
					//현재버전과 설치버전이 같으면 설치됨, 다르면 업데이트
					if( appVerCompare(item.app_ver, item.installed_ver) ){
//							appListHtml += '	<button class="btn-installed">설치됨</button>';
					}else{
						appListHtml += '<li>';
						appListHtml += '	<img src="'+item.icon_app_bic_url+'" alt="">';
						appListHtml += "	<div data-appNo='"+item.app_idx+"' data-plat_idx='"+item.plat_idx+"' data-package_nm='"+item.package_nm+"'>";
						appListHtml += '		<p class="title">'+item.app_nm+'</p>';
						appListHtml += '		<p class="txt">'+item.app_info+'</p>';
						appListHtml += '	</div>';
						appListHtml += '	<button class="btn-update" data-install-Url="'+item.bin_url+'" data-appNm="'+item.app_nm+'">업데이트</button>';
						appListHtml += '</li>';
						updateAppCnt += 1;
						
						//앱의 카테고리 별로 분리해서 리스트를 보여줌
						for(var i=0; i<$('#appList h3').length; i++){
							if($('#appList h3').eq(i).attr('data-cate-cd') == item.cate_cd){
								$('#appList h3').eq(i).removeClass('none');
								$('#appList h3').eq(i).next('ul').removeClass('none');
								$('#appList h3').eq(i).next('ul').append(appListHtml);
								appListHtml = '';
								
								break;
							}
						}
					}
				}
			});
			
			//전체 업데이트 가능 앱 개수 업데이트
			$('#updateAppCnt').text(StringUtil.addComma(updateAppCnt));
			
			//#content 영역 보이기
			$('#content > div').not('.no-date').removeClass('none');
			
			if (updateAppCnt == 0) {
				$('.no-date').removeClass('none');
			} else {
				$('.no-date').addClass('none') 
			}
			
			setTimeout(function (){
				!$('.full_loding').hasClass('none') && !$('#content > div').not('.no-date').hasClass('none') ? $('.full_loding').addClass('none') : ''
			}, 200);
		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
			$('.full_loding').addClass('none');
		}
	});
}



var MStatus = {
		onReady : function(evt){
			pageInit(evt);
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