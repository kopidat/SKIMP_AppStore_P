/**
 * SKIMP-FR-020.js
 * @ 첨부파일 뷰어
 * 2021.06.03
 */
var pageIdxCnt = 1;
var fileConvertStamp = MData.param('stamp');
var fileConvertId = MData.param('id');
var allPageCnt = MData.param('pageCnt');
var fileNm = MData.param('fileNm');
var currNetwork = CONFIG.IS_DEV == true ? "https://dev-3rd-skimp.skinnovation.com/" : "https://3rd-skimp.skinnovation.com/";

var pageInit = function(){
	pageEvent();
	
	$('#fileTitle').html(fileNm);
	
	drawImageViewer();
};

var pageEvent = function(){

	$(document).on('click', '.btn-next', function(){
		pageIdxCnt ++;

		var offset1 = $('#idx'+pageIdxCnt).position().top;
		$('html').animate({scrollTop : offset1}, 400);

		$('.number').text(pageIdxCnt.toString()+'/'+allPageCnt);
	});

	$(document).on('click', '.btn-prev', function(){
		pageIdxCnt > 1 ? pageIdxCnt -- : '';

		if(1 <= pageIdxCnt){
			var offset = $('#idx'+pageIdxCnt).offset();
			$('html').animate({scrollTop : offset.top}, 400);

			$('.number').text(pageIdxCnt.toString()+'/'+allPageCnt);
		}
	});

	$(document).on('click', '.close', function(){
		M.page.back({
			action : "CLEAR_TOP",
		});
	});
	
	$(window).on('scroll', function() {
		var scrollTop = $(this).scrollTop();
		var wHeight = $(this).height();
		
		console.log(scrollTop, wHeight);
 	
		if (pageIdxCnt == 1 && scrollTop < wHeight) {
			$('.number').text('1/'+allPageCnt);
			pageIdxCnt = 1;
		} else {
			$("#content div").not('.view').each(function(idx, div) {
				var offset = $(div).offset().top;
				console.log('offset : ' + offset);
				console.log('thisDiv : ' + $(this) + 'pageNum : ' + $(this).attr('pageNum'));

				if (offset >= scrollTop && offset < (scrollTop + wHeight)) {
//				$('.number').text(idx+'/'+allPageCnt);
					$('.number').text($(this).attr('pageNum')+'/'+allPageCnt);
					pageIdxCnt = $(this).attr('pageNum');
					return false;
				}
			});
		}
	});

};


var drawImageViewer = function(){
	var listHtml = '';

	for(var i=1; i<=allPageCnt; i++){
		 listHtml += '<div class="a" id="idx'+i+'" pageNum="' +i+ '"><img src="'  +currNetwork+ 'doc/file/download/'+fileConvertId+'/'+i+'.stream?stamp='+fileConvertStamp+'" alt=""></div>'
		 console.log('drawImageUrl : ' + currNetwork+ 'doc/file/download/'+fileConvertId+'/'+i+'.stream?stamp='+fileConvertStamp);
	}

	$('.content .view').append(listHtml);

	for(var i=0; i<$('#content div').length; i++){
		$('#content div').eq(i).attr('data-height', Math.ceil($('#content div').eq(i).position().top));
	}

	$('.number').text('1/'+allPageCnt);
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