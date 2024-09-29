
// flex를 사용 하지 못하는 하위 버전을 지원하기 위하여 아래와 같이 높이값을 지정해주는 script를 사용합니다.
$(window).ready(function(event){

//	// 자주묻는 질문
//	var allAccordion = $('.notice-list > dl > dd').hide();
//	$('.notice-list > dl > dt').click(function() {
//		if($(this).next().css('display') != 'block'){
//			allAccordion.slideUp();
//			$(this).next().slideDown();
//			$(this).addClass('active').parent().siblings('dl').find('dt.active').removeClass('active');
//		}else {
//			$(this).next().slideUp();
//			$(this).removeClass('active');
//		}
//		return false;
//	});

	//checkbox 체크시 설정페이지
//	$('.login-simple input[type=checkbox]').click(function() {
//		if($(this).is(':checked')) {
//			$('.simple-auth').show();
//		} else {
//			$('.simple-auth').hide();
//		}
//	});





});




function layer_open(el) {

	var $href = $(this).attr('href');
	var temp = $('#' + el);
	//var temp_layer = $('#' + el).children(".popup-layer");


	if(this) {
		$('#' + el).fadeIn();
		$('#' + el).css("visibility","inherit");
		$('#' + el).find('.pop-layer').removeClass('layerclose');
		$('body').addClass('over_-h');


	} else {
		temp.fadeIn();
		$('#' + el).css("visibility","over_-h");
	}
	$('.popup-layer .color-choice.main button, .popup-layer .font-cancel,button.close, .pop-close, .popup-layer .cancel').click(function() {
        $(this).parents('.popup-layer').find('.pop-layer').addClass('layerclose');
		$(this).parents('.popup-layer').fadeOut();
		$('body').removeClass('over_-h');
		return false;
	});

	
};