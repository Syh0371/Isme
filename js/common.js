
var getAliImgPath = "https://together-imgs.oss-cn-shanghai.aliyuncs.com/";
var brandStore = ''; //品牌门店

var setTitle = function () {
    $.ajax({
        type:"GET",
        url:"/emp/getInfo",
        success:function(res){
            if (res.errno == 0 ) {
                $(".touxain").html(res.data.empTitle);
                $('#userName').html(res.data.name);
                $('#empCode').html(res.data.empNo);
            }

        },
        error:function(){

        }
    });

};

var exit = function () {
    $.ajax({
        type:"GET",
        url:"/auth/logout",
        async:true,
        success:function(){
            window.location.href = "/login";
        },
        error:function(){

        }
    });
};


var getBrandStore = function (success) {
    $.ajax({
        type:"GET",
        url:"/getBrandStore",
        async:true,
        success:function(res){
            // console.log(res);
            if (res.errno == 0) {
                brandStore = res.data;
                success();
            } else {
                // console.log(res.errmsg);
            }
        }
    });
};

var html = "";
var menu_html = "";
 /*========菜单栏 数据的获取==========*/
var getmenu=function(link,flag){
	$.ajax({
    	type:"GET",
    	url:"/getMenuList",
    	async:true,
    	data:{
    		"link":link
    	},
    	success:function(res){
    	    var munes = res.data;
    	    html = "";
    	    if(flag == undefined){
    	    	flag = 1;
    	    }
    	    $.each(munes, function(i,item) {
    	    	var menuChilds = item.menuList;
    	    	if(item.name != "微信" && item.name != "会员"){
	    	    	if(flag == 0){
	    	    		if(i==0){
	    	    			html+='<li class="active">'+
	                                '<a aria-expanded="false" role="button" class="dropdown-toggle"  href="'+item.link+'"> '+item.name+'</a>'+
	                            '</li>';
	    	    		}else{
	    	    			isMenuChild(item,menuChilds);
	    	    		}
	    	    	}else{
	    	    		isMenuChild(item,menuChilds);
	    	    	}
    	    	}
    	    });
    	    $("#navMune").html(html);
    	}
    });
};

var isMenuChild = function(item,menuChilds){
	
	if(menuChilds.length > 0){
		if(item.check==1){
			menuChild(menuChilds);
			html+="<li class='dropdown active'><a aria-expanded='false' role='button' href='"+item.link+"' class='dropdown-toggle' data-toggle='dropdown'> "
			+item.name
			+" <span class='caret'></span></a><ul role='menu' class='dropdown-menu'>"
			+menu_html
			+"</ul></li>";
		}else{
			menuChild(menuChilds);
			html+="<li class='dropdown'><a aria-expanded='false' role='button' href='"+item.link+"' class='dropdown-toggle' data-toggle='dropdown'> "
			+item.name
			+" <span class='caret'></span></a><ul role='menu' class='dropdown-menu'>"
			+menu_html
			+"</ul></li>";
		}
		
	}else{
		if(item.check==1){
			html+='<li class="active">'+
                    '<a aria-expanded="false" role="button" class="dropdown-toggle"  href="'+item.link+'"> '+item.name+'</a>'+
                '</li>';
		}else{
			html+='<li class="">'+
                    '<a aria-expanded="false" role="button" class="dropdown-toggle"  href="'+item.link+'"> '+item.name+'</a>'+
                '</li>';
		}
	}
	
}

var menuChild = function(menuChilds){
	menu_html = "";
	$.each(menuChilds, function(i,item){
    	//console.log("res_item",item);
    	if(item.name !="权限信息"){
    		if(item.check==1){
		    	menu_html+='<li class = "active">'+
				'<a href="'+item.link+'">'+item.name+'</a>'+
		        '</li>';
		        
			}else{
				menu_html+='<li>'+
				'<a href="'+item.link+'">'+item.name+'</a>'+
		        '</li>';
			}
    	}
	    
		
    });
}


var getImgPath = function () {
    var employee = JSON.parse($.cookie("login"));
    return employee.imgPath;
};

/****获取页面跳转后的参数方法1****/
function GetQueryString(name) {
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if(r!=null) return  unescape(r[2]);
    return null;
}
/****获取页面跳转后的参数方法2****/
function GetRequest() {
    var url = location.search; //获取url中"?"符后的字串
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for(var i = 0; i < strs.length; i ++) {
            theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);
        }
    }
    return theRequest;
}

var request = GetRequest();

//文件上传
function uploadImage(img,success) {
    //判断是否有选择上传文件
    var imgPath = img.val();
    if (imgPath == "") {
        alert("请选择上传图片！");
        // layer.msg("请选择上传图片！", {icon: 5});
        return;
    }
    var formData = new FormData();
    formData.append('file', img[0].files[0]);
    $.ajax({
        type: "POST",
        url: "https://img.jointogether.cn/upload/image",
        data:formData,
        cache: false,
        async:true,
        processData: false,
        contentType: false,
        success: success,
        error: function() {
            alert("上传失败，请检查网络后重试");
            // layer.msg("上传失败，请检查网络后重试", {icon: 5});
        }
    });
}

//ajax封装调用
var client = {
    postJson: function (url, data, success, error ) {
        $.ajax({
            url:url,
            data: JSON.stringify(data),
            async:true,
            type: 'POST',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            success:success,
            error: error
        });
    },
    postJsonAsync: function (url, data, success, error ) {
        $.ajax({
            url:url,
            data: JSON.stringify(data),
            async:false,
            type: 'POST',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            success:success,
            error: error
        });
    },

    postForm: function (url, data, success, error) {
        $.ajax({
            url:url,
            data: data,
            async:true,
            type: 'POST',
            success:success,
            error: error
        });
    },

    get: function (url, data, success, error) {
        //console.log("+++++",JSON.parse($.cookie("login"))[0].token)
        $.ajax({
            url:url,
            data: data,
            async:true,
            type: 'GET',
            success:success,
            error: error
        });
    },
    getAsync: function (url, data, success, error) {
        //console.log("+++++",JSON.parse($.cookie("login"))[0].token)
        $.ajax({
            url:url,
            data: data,
            async:false,
            type: 'GET',
            success:success,
            error: error
        });
    },

};

$(function () {
    setTitle();

    //退出管理系统
    $(".exit").click(function(){
        exit();
    });

});

//日期转化星期
var dateWeek = function (date){
var x = "";
var day = new Date(date).getDay();
switch (day){
	case 0:
	  x="星期日";
	  break;
	case 1:
	  x="星期一";
	  break;
	case 2:
	  x="星期二";
	  break;
	case 3:
	  x="星期三";
	  break;
	case 4:
	  x="星期四";
	  break;
	case 5:
	  x="星期五";
	  break;
	case 6:
	  x="星期六";
	  break;
}
return x;
}



var getNowFormatDate = function (intx) {
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var month = "";
    if(intx == 0 || intx == 3) {
        month = date.getMonth() + 1;
    }else{
        month = date.getMonth();
    }

    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = "";
    if(intx == 0 || intx == 1){
        currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
            + " " + date.getHours() + seperator2 + date.getMinutes()
            + seperator2 + date.getSeconds();
    }else{
        currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate;
    }

    return currentdate;
}
Date.prototype.pattern=function(fmt) {
    var o = {
        "M+" : this.getMonth()+1, //月份
        "d+" : this.getDate(), //日
        "h+" : this.getHours()%12 == 0 ? 12 : this.getHours()%12, //小时
        "H+" : this.getHours(), //小时
        "m+" : this.getMinutes(), //分
        "s+" : this.getSeconds(), //秒
        "q+" : Math.floor((this.getMonth()+3)/3), //季度
        "S" : this.getMilliseconds() //毫秒
    };
    var week = {
        "0" : "/u65e5",
        "1" : "/u4e00",
        "2" : "/u4e8c",
        "3" : "/u4e09",
        "4" : "/u56db",
        "5" : "/u4e94",
        "6" : "/u516d"
    };
    if(/(y+)/.test(fmt)){
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    }
    if(/(E+)/.test(fmt)){
        fmt=fmt.replace(RegExp.$1, ((RegExp.$1.length>1) ? (RegExp.$1.length>2 ? "/u661f/u671f" : "/u5468") : "")+week[this.getDay()+""]);
    }
    for(var k in o){
        if(new RegExp("("+ k +")").test(fmt)){
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
        }
    }
    return fmt;
};
/*根据元素拿数组下标，根据下标的去除元素*/
Array.prototype.remove = function(val) {
	var index = this.indexOf(val);
	if (index > -1) {
		this.splice(index, 1);
	}
};




