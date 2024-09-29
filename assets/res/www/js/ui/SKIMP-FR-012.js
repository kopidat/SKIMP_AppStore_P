/**
 * SKIMP-FR-012.js
 * @ 앱 상세
 * 2021.05.24
 */

var appNo = MData.param('appNo');
var platIdx = MData.param('platIdx');
var packageNm = MData.param('packageNm');
var downUrl = MData.param('downUrl');
var appNm = MData.param('appNm');
var appIdx = MData.param('appIdx');
var sort = MData.param('sort');
var schemeNm = MData.param('scheme');
var pkgNm = MData.param('packageNm');
var thisAppVer = MData.param('appVer');


var pageInit = function(){
	pageEvent();

	appDetailSearch();
};

var pageEvent = function(){
	//앱설치 버튼
	$(document).on('click', '.btn-download, .btn-update', function(e){
		var thisInstallUrl = downUrl;
		var thisAppName = appNm;
		var thisAppIdx = appIdx

		//다운로드 버튼은 다운로드 카운트 추가 후 앱 설치
		if( $(e.target).attr('class') == "btn-download" ) {
    		//ios 의 경우 타 앱의 버전을 가지고 올 수 없어서 조회된 앱정보의 버전을 storage 에 저장해두고 현재버전으로 사용
    		MData.storage(pkgNm, thisAppVer);
			downloadCntCheck(thisAppIdx, thisInstallUrl, thisAppName);
		}else{
    		MData.storage(pkgNm, thisAppVer);

			if(MNavigator.device("ios")){
				M.apps.browser("itms-services://?action=download-manifest&url="+thisInstallUrl,"UTF-8")
			}else{
				M.apps.install(thisInstallUrl, thisAppName);
			}
		}
	});
	
	
	//앱 열기 버튼
	$(document).on('click', '.btn-installed', function(e){
//		var packageNm = $(this).data('packagenm');
//		var schemeNm = $(this).data('scheme')+'://';
		
		if(MNavigator.device("ios")){
			M.apps.open(schemeNm);
		}else{
			M.apps.open(packageNm);
		}
	});
};


//앱 상세조회
var appDetailSearch = function(){
	var deviceOS = MNavigator.device("ios") ? "2" : "1";
	
	MNet.httpSend({
        path : "skimp/common/api/SKIMP-0006",
        sendData : {
        	pakgId: packageNm,
        	platIdx : deviceOS,
        },
        callback : function(rst, setting){
            console.log(rst);

			var appIconInfo = rst.appIconImageInfo;
			var previewInfo = rst.previewImageInfo;
            var appInfo = rst.appInfo;

            var appEx = appInfo.app_info.replace(/\r\n/g, '<br>');
            
			//#content 영역 숨기기
			$('#content > div').addClass('none');
            
            !StringUtil.isEmpty($('.app-description').find('button')) ? $('.app-description').find('button').remove() : '';
            
			//현재버전과 설치버전이 같으면 설치됨, 다르면 업데이트
			if( sort == 'installed'){
//				$('.app-description').append('<button class="btn-installed" data-sort="installed">설치됨</button>');
				$('.app-description').append('<button class="btn-installed" data-sort="installed">열기</button>');
			}else if (sort == 'update'){
				$('.app-description').append('<button class="btn-update" data-install-Url="'+downUrl+'" data-appNm="'+appNm+'" data-sort="update">업데이트</button>');
			}else if (sort == 'download'){
				$('.app-description').append('<button class="btn-download" data-install-Url="'+downUrl+'" data-appNm="'+appNm+'" data-appIdx="'+appIdx+'" data-sort="download">다운로드</button>');
			}
			
			$('#downloadCnt').text(appInfo.down_cnt);
            $('#appIcon').attr('src', appIconInfo.img_path_1);
            $('#appNm').text(appInfo.app_nm);
            $('#appVer').text(appInfo.app_ver);
            $('#categoryNm').text(appInfo.cate_nm);
            $('.explain').html(appEx);

            //미리보기 이미지 최대 4개까지 보여줌
            !StringUtil.isEmpty(previewInfo.img_path_1) ? $('#previewImg button').eq(0).children('img').attr('src', previewInfo.img_path_1) : $('#previewImg button').eq(0).remove();
            !StringUtil.isEmpty(previewInfo.img_path_2) ? $('#previewImg button').eq(1).children('img').attr('src', previewInfo.img_path_2) : $('#previewImg button').eq(1).remove();
            !StringUtil.isEmpty(previewInfo.img_path_3) ? $('#previewImg button').eq(2).children('img').attr('src', previewInfo.img_path_3) : $('#previewImg button').eq(2).remove();
            !StringUtil.isEmpty(previewInfo.img_path_4) ? $('#previewImg button').eq(3).children('img').attr('src', previewInfo.img_path_4) : $('#previewImg button').eq(3).remove();
            
			//#content 영역 보이기
			$('#content > div').removeClass('none');
			
			setTimeout(function (){
				!$('.full_loding').hasClass('none') && !$('#content > div').hasClass('none') ? $('.full_loding').addClass('none') : ''
			}, 400);
        },
        errCallback : function(errCd, errMsg){
            console.log(errCd, errMsg);
			$('.full_loding').addClass('none');
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

		MPage.html({
			url : thisPageUrl,
			animation : "NONE",
		})
	});
}


//다운로드 횟수 체크
var downloadCntCheck = function(appIdx){
	MNet.httpSend({
		path : "/skimp/common/api/SKIMP-0015",
		sendData : {
			appIdx: appIdx,
		},
		callback : function(rst, setting){
			console.log(rst);

			if(MNavigator.device("ios")){
				M.apps.browser("itms-services://?action=download-manifest&url="+downUrl,"UTF-8")
			}else{
				M.apps.install(downUrl, appNm);
			}
			
			appDetailSearch();
		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
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
			loginAuth();
		}
}