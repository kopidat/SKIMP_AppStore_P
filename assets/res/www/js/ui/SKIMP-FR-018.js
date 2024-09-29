/**
  * SKIMP-FR-015.js
 * @ 공지사항
 * 2021.05.24
 */

var fileConvertStamp = "";
var fileConvertId = "";
var currNetwork = CONFIG.IS_DEV == true ? M.info.app().manifest.network.http.STORE_DEV.address : M.info.app().manifest.network.http.STORE_REAL.address;
var selFileNm = '';
var currentPageNum = '1';
var totalPageCnt = '';

var pageInit = function(){
	pageEvent();

	//기존 리스트 초기화
	$('#pushList').html('');

	pushListSearch();
};

var pageEvent = function(){
	//검색버튼 이벤트
	$(document).on('click', '#srchTxt', function(){
		//기존 리스트 초기화
		$('#pushList').html('');
		currentPageNum = '1';
		totalPageCnt = '';

		pushListSearch();
	});
	
	
	//푸시 내용보기
	$(document).on('click', '.notice-list > dl > dt', function(){
		if($(this).next().css('display') != 'none'){
			$(this).next().slideUp();
			$(this).removeClass('active');
		}else {
			$('.notice-list > dl > dd').slideUp();
			$(this).next().slideDown();
			$(this).addClass('active').parent().siblings('dl').find('dt.active').removeClass('active');
		}
		return false;
	});


	//첨부파일 열기
	$(document).on('click', '.btn-list button span em', function(){
		var fileName = $(this).parents('button').attr('data-fileNm');
//		var fileUrl = "https://skimp.skinnovation.com/api/notice/download/"+$(this).parents('button').attr('data-fileIdx');
		var fileUrl = currNetwork + "api/notice/download/"+$(this).parents('button').attr('data-fileIdx');
		selFileNm = $(this).parents('button').attr('data-fileNm');
		
		console.log("첨부파일명 : " + selFileNm + "     첨부파일 taget url : " + fileUrl);
		docUpload(fileName, fileUrl);
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

//		if(thisId != "btnNotice"){
			MPage.html({
				url : thisPageUrl,
				animation : "NONE",
			})
//		}
	});

	//scroll이벤트
	$("#content").on('scroll', function(e){
		let el = e.target;
		if ( (currentPageNum < totalPageCnt) && (el.scrollHeight - Math.abs(el.scrollTop) <= el.clientHeight) ) {
			currentPageNum = String(Number(currentPageNum) + 1);
			console.log('currentPageNum : ' + currentPageNum);
			pushListSearch();
		}
	});
};


var pushListSearch = function(){
	MNet.httpSend({
		path : "skimp/common/api/SKIMP-0018",
		sendData : {
			cuid: MData.storage('encData').userId,
			appId: M.info.app().app.id,
			pageNum: currentPageNum,
			pageSize: '15',
			keyword : $('.input01').val(),
		},
		callback : function(rst, setting){
			console.log(rst);

			var listHtml = '';
			var listData = rst.pushList;
			totalPageCnt = Math.ceil(rst.count/15);
			
			//#content 영역 숨기기
			$('#content > div').addClass('none');
			
			//알림리스트 전체 개수 변경
			if (listData.length <= 15 && currentPageNum == 1) {
				$('.app-total span').html(listData.length);
			} else {
				$('.app-total span').html((Number(currentPageNum) * 15) + listData.length);
			}
			
			//기존 리스트 초기화
//			$('#pushList').html('');
			
			if (listData.length == 0) {
				$('.no-date').removeClass('none');
				$('.app-total span').html('0');
				
				//#content 영역 보이기
				$('#content > div').not('.no-date, #pushList').removeClass('none');
				
				setTimeout(function (){
					!$('.full_loding').hasClass('none') ? $('.full_loding').addClass('none') : ''
				}, 400);

			} else {

				listData.forEach((item, idx) => {
					var tempTitle = '';
					var tempBody = '';
					
					try {
					    var json = JSON.parse(item.message);
//					    return (typeof json === 'object');
						tempTitle = JSON.parse(item.message).title;
						tempBody = JSON.parse(item.message).body	;
					} catch (e) {
						tempTitle = item.message;
						tempBody = item.message;
					}
					
					listHtml += '<dl>';
					listHtml += '	<dt seqno="'+item.seqno+'">';
					listHtml += '		<h3 class="">'+tempTitle+'</h3>';
					listHtml += '		<p class="date">'+item.regdate+'</p>';
					listHtml += '	</dt>';
					listHtml += '	<dd style="display:none">';
					listHtml += '		<div class="notice-content">'+tempBody+'</div>';
					listHtml += '	</dd>';
					listHtml += '</dl>';
				});
				
				$('#pushList').append(listHtml);
				
				//#content 영역 보이기
				$('#content > div').not('.no-date').removeClass('none');
				
				$('.no-date').addClass('none');
				
				setTimeout(function (){
					!$('.full_loding').hasClass('none') && !$('#content > div').not('.no-date').hasClass('none') ? $('.full_loding').addClass('none') : ''
				}, 400);
			}
		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
			$('.full_loding').addClass('none');
		}
	});
}


//문서 업로드
var docUpload = function(fileName, fileUrl){
	MNet.httpSend({
		path : "skimp/common/api/SKIMP-0007",
		sendData : {
			fileName : fileName, //$('#fileNm').val(), "sample.pdf"
			url : fileUrl, //$('#fileUrl').val(),  "https://dev-skimp.skinnovation.com/img/sample.pdf"
			convertType	: "IMAGE",
			token : "",
			pakgId : "skimp.common.app.demo",
		},
		callback : function(rst, setting){
			console.log(rst);
			fileConvertStamp = rst.stamp;
			fileConvertId = rst.id;

			docConvertState();
		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
			MPopup.alert(errMsg);
			$('.full_loding').addClass('none');
		}
	});
}

//문서변환 상태 확인
var docConvertState = function(){
	MNet.httpSend({
		path : "skimp/common/api/SKIMP-0010",
		sendData : {
			id : fileConvertId,
            token : "",
            pakgId : "skimp.common.app.demo",
		},
		callback : function(rst, setting){
			console.log(rst);
			docConvertInfo(rst);
		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
			MPopup.alert(errMsg);
			$('.full_loding').addClass('none');
		}
	});
};


//문서변환 정보 확인
var docConvertInfo = function(){
	MNet.httpSend({
		path : "skimp/common/api/SKIMP-0011",
		sendData : {
			id : fileConvertId,
            token : "",
			pakgId : "skimp.common.app.demo",
		},
		indicator : false,
		callback : function(rst, setting){
			console.log(rst);

			if(rst.message == "성공" && rst.success == true){
				MPage.html({
					url : "SKIMP-FR-020.html",
					param : {
						stamp : fileConvertStamp,
						id : fileConvertId,
						pageCnt : rst.page,
						fileNm : selFileNm,
					}
				})
				$('.full_loding').addClass('none');
			}else{
				setTimeout(function(){
					docConvertInfo();
				}, 1000);

			}


		},
		errCallback : function(errCd, errMsg){
			console.log(errCd, errMsg);
			$('.full_loding').addClass('none');
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