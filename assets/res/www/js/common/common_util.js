/**
 * @파일명 : common.util.js
 * @생성일 : 2021.05.21
 * @작성자 : 김동창
 */

// 날짜 관련 Util
var DateUtil = {
	date_ymd_div : ".",
	date_hms_div : ":",
	
	// Date Format 년월일
	getYMD : function(date){
		if(arguments.length == 0) {
			return moment().format("YYYY"+ date_ymd_div + "MM" + date_ymd_div + "DD");
		}
		else {
			return moment(date, "YYYYMMDD").format("YYYY"+ date_ymd_div + "MM" + date_ymd_div + "DD");
		}
	},
	
	getYM : function(date){
		if(arguments.length == 0) {
			return moment().format("YYYY"+ date_ymd_div + "MM");
		}
		else {
			return moment(date, "YYYYMM").format("YYYY"+ date_ymd_div + "MM");
		}
	},
	
	getHMS : function(date){
		if(arguments.length == 0) {
			return moment().format("HH" + date_ymd_div + "mm" + date_hms_div + "ss");
		}
		else {
			return moment(date, "HHmmss").format("HH" + date_ymd_div + "mm" + date_hms_div + "ss");
		}
	},
	
	getHM : function(date){
		if(arguments.length == 0) {
			return moment().format("HH" + date_hms_div + "mm");
		}
		else {
			return moment(date, "HHmm").format("HH" + date_hms_div + "mm");
		}
	},
	
	getYMDHM : function(date){
		if(arguments.length == 0) {
			return moment().format("YYYY"+ date_ymd_div + "MM" + date_ymd_div + "DD HH" + date_hms_div + "mm");
		}
		else {
			return moment(date, "YYYYMMDDHHmm").format("YYYY"+ date_ymd_div + "MM" + date_ymd_div + "DD HH" + date_hms_div + "mm");
		}
	},
	
	monthDiff : function(_fromDt, _toDt){
		var from = moment([_fromDt.substr(0,4), Number(_fromDt.substr(4,2)) - 1]);
		var to = moment([_toDt.substr(0,4), Number(_toDt.substr(4,2)) - 1]);
	
		return from.diff(to, "months")
	},
	
	setDateDiff : function(_addDay, _nowDate){
		if(arguments.length == 0) {
			return moment().add(_addDay, 'day').format('YYYY' + this.ymd_div + 'MM' + this.ymd_div + 'DD');
		}
		else {
			return moment(StringUtil.extractNumber(_nowDate), 'YYYYMMDD').add(_addDay, 'day').format('YYYY' + this.ymd_div + 'MM' + this.ymd_div + 'DD');
		}
	},
	
	/** 
	 * 시간 형태 변경
	 * @param str
	 * @returns
	 */
	setTimeFormat : function(str, format) {
		if(isEmpty(str)) 
			str = "today";
		if(isEmpty(format)) 
			format = "HH:mm";
		if(str == 'today') {
			return moment().format(format);
		}
		else {
			if (str.length == 6) {
				return moment(StringUtil.extractNumber(str), 'HHmmss').format(format);
			}
			else {
				return moment(StringUtil.extractNumber(str), 'HHmm').format(format);
			}
		}
	},
	
	/** 
	 * 날짜 더하기 빼기
	 * @param _nowDate 기본 날짜
	 * @param _addDay 연산될 날짜
	 * @returns
	 */
	setDateAddOrSub : function(_nowDate, _addDay) {
		if(_nowDate == 'today') {
			return moment().add(_addDay, 'day').format('YYYY' + date_ymd_div + 'MM' + date_ymd_div + 'DD');
		}
		else {
			return moment(StringUtil.extractNumber(_nowDate), 'YYYYMMDD').add(_addDay, 'day').format('YYYY' + date_ymd_div + 'MM' + date_ymd_div + 'DD');
		}
	},
	
	/** 
	 * 날짜 차이 계산 
	 * @param _fromDt	기준 날짜(YYYYMMDD)
	 * @param _toDt		연산될 날짜(YYYYMMDD)
	 * @param falg		차이 계산할 기준(y-년, m-월, d-일)
	 * @returns
	 */
	dateDiff : function(_fromDt, _toDt, flag) {
		var from = moment([_fromDt.substr(0,4), Number(_fromDt.substr(4,2)) - 1, Number(_fromDt.substr(6, 2))]);
		var to = moment([_toDt.substr(0,4), Number(_toDt.substr(4,2)) - 1, Number(_toDt.substr(6, 2))]);
		switch(flag) {
		case "y":
			flag = "years";
			break;
		case "m":
			flag = "months";
			break;
		case "d":
			flag = "days";
			break;
		}
		return from.diff(to, flag);
	},
	
	setMonthAddOrSub : function(_nowDate, _addMonth) {
		return moment(StringUtil.extractNumber(_nowDate), 'YYYYMMDD').add(_addMonth, 'month').format('YYYYMMDD');
	},
	
	changeLtToHHmm : function(lt){
		return moment(lt, "LT").format("HHmm");
	},
	
	changeHHmmToLt : function(HHmm, isInitDate){
		var tmpTime = moment(HHmm, "HHmm").format("LT");
		var tmpTimeArr = tmpTime.split(" ");
		var tmpHHArr = tmpTimeArr[0].split(":");
		if(isInitDate == "Y"){
			return StringUtil.zeroPad(tmpHHArr[0], 2) + "" + tmpHHArr[1] + "" + tmpTimeArr[1];
		}
		else{
			if(tmpTimeArr[1] == "AM"){
				return "오전 " + StringUtil.zeroPad(tmpHHArr[0], 2) + ":" + tmpHHArr[1];
			}
			else{
				return "오후 " + StringUtil.zeroPad(tmpHHArr[0], 2) + ":" + tmpHHArr[1];
			}
		}
	},
	
	getSelectDayToKor : function(_selectDate){
		var arrDayName = new Array( "일", "월", "화", "수", "목", "금", "토");
		var dayInt = moment(_selectDate, "YYYYMMDD").day();
	
		return arrDayName[dayInt];
	},
	
	changeMinutesToHours : function(time){
		return moment.utc(moment.duration(time, "minutes").asMilliseconds()).format("HH:mm");
	},

};


// 문자열 관련 유틸
var StringUtil = {
	
	/**
	 * 숫자만 추출
	 * @param str
	 * @returns
	 */
	extractNumber : function(str){
		try {
			return str.replace(/[^0-9]/g, '');
		}
		catch (e) {
			return str;
		}
	},
	
	/**
	 * 세자리 단위로 콤마를 추가한다.
	 * @param str
	 * @returns
	 */
	addComma : function(str){
		if(!Number(str)){ // 숫자가 아닐때에만
			str = StringUtil.extractNumber(String(str));
		}
		else{
			str = String(str);
		}
		try {
			return str.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
		}
		catch (e) {
			return str;
		}
	},
	
	/**
	 * 콤마를 삭제한다
	 * @param str
	 * @returns
	 */
	removeComma : function(str){
		str = String(str);
		try {
			return str.replace(/[^\d]+/g, '');
		}
		catch (e) {
			return str;
		}
	},
	
	/** 
	 * 값의 존재 여부 확인
	 */
	isEmpty : function(obj) {
		var hasOwnProperty = Object.prototype.hasOwnProperty;
		if (typeof obj == "number")
			obj = obj+"";
		if (obj == null) 
			return true;
		if (obj == undefined) 
			return true;
		if (obj.length > 0) 
			return false;
		if (obj.length === 0) 
			return true;
	
		for (var key in obj) {
			if (hasOwnProperty.call(obj, key)) return false;
		}
	
		return true;
	},
	
	
	/**
	 * 원하는 길이 만큼 0으로 채운 문자 반환
	 * Ex) zeroPad("22", "5") => "00022"
	 * @param str
	 * @param length
	 * @return String
	 */
	zeroPad : function(str, length){
		str += '';
		return str.length < length ? zeroPad("0"+str, length) : str;
	},
	
	/** 
	 * 원하는 길이 만큼 원하는 값으로 채운 문자 반환
	 * Ex) lpad("3", "-", 5) => "----3"
	 * @param s : 문자열
	 * @param c : 원하는 문자
	 * @param n : 원하는 길이
	 * @return String
	 */
	lpad : function(s, c, n) {
		var _s = typeof s == "number" ? s.toString() : s;
		if (! _s || ! c || s.length >= n) {
			return _s;
		}
	
		var max = (n - _s.length)/c.length;
		for (var i = 0; i < max; i++) {
			_s = c + _s;
		}
	
		return _s;
	},
	
	
	/**
	 * 공백 제거
	 * @param str
	 */
	strTrim : function(str){
		return str.replace(/\s/gi, '').replace(/ /g, '');
	},
	
	
	getBytes : function(src){
		var i=0;
		var sum=0;
		for(i=0; i<src.length; i++){
			if(escape(src.charAt(i)).length>4) sum++;
		}
		return sum;
	},
	
	/**
	 * object 복사
	 * obj : 복사할 데이터
	 */
	clone : function(obj){
		if (obj === null || typeof(obj) !== 'object')
			return obj;
	
		var copy = obj.constructor();
		for (var attr in obj) {
			if (obj.hasOwnProperty(attr)) {
				copy[attr] = obj[attr];
			}
		}
		return copy;
	},
	
	specialCharRemove : function(str){
		var pattern = /[^(가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9)]/gi;
	
		if(pattern.test(str)){
			str = str.replace(pattern,"");
		}
		return str;
	},
	
	replacePhoneNumber : function(num){
		var pattern="", phNum = "";
	
		if(num.length == 10)
			pattern = /(\d{2})(\d{4})(\d{4})/;
		else if(num.length == 11)
			pattern = /(\d{3})(\d{4})(\d{4})/;
		phNum = num.replace(pattern, '$1-$2-$3');
	
		return phNum;
	},
};


//비밀번호 관련
var pwUtil = {

    //비밀번호 일치여부 체크
    equalCheck : function(pw1, pw2){
        if(pw1 == pw2){
            return true;
        }
        else{
            return false;
        }
    },
    
    //비밀번호 길이체크
    lengthCheck : function(pw){
    	if(pw.length < 8 || pw.length > 13){
    		return false;
    	}
    	else{
    		return true;
    	}
    },
    
    //비밀번호 유효성 체크
    regexCheck : function(pw){
    	var chk1 = /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,20}$/.test(pw);			//영문,숫자 체크
    	var chk2 = /^(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,20}$/.test(pw);	//영문,특수문자 체크
    	var chk3 = /^(?=.*[^a-zA-Z0-9])(?=.*[0-9]).{8,20}$/.test(pw);		//특수문자,숫자 체크
    	
    	var result = {};
    	
    	if(pw.search("%") > -1){
    		result.code ="01";
    		result.msg = "특수문자에서 '%'는 사용 불가능합니다.";
    	}
    	else{
    		if(!(chk1 || chk2 || chk3)){
    			result.code = "02";
    			
    			if(/^[0-9]*$/.test(pw)){
    				result.msg = "영문 또는 특수문자를 조합하여 8자 이상 입력해주세요.";
    			}
    			else if(/^[a-zA-Z]*$/.test(pw)){
    				result.msg = "숫자 또는 특수문자를 조합하여 8자 이상 입력해주세요.";
    			}
    			else if(/^[~!@#$%^&*()_+|<>?:{}`]*$/.test(pw)){
    				result.msg = "영문 또는 숫자를 조합하여 8자 이상 입력해주세요."
    			}
    			else{
    				result.msg = "영문/숫자/특수문자 중 2개 이상 조합하여 8~20자 이내로 입력해주세요.";
    			}
    		}
    		else{
    			result.code = "00";
    			result.msg = "사용가능한 비밀번호 입니다.";
    		}
    	}
    	
    	return result;
    },
}