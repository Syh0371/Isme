function switchTab(cursel,T_ymd){
	 if(cursel==1){
			var len = $("#zbs").html();
			var size = $.trim(len).length;
			if(size==0){
				$.ajax({
					type:'post',
					url:newsMain,
					data:{
						sid:'10001',
						pn:'1'
					},
			          success : function(json) {
			        	  var count = json.Resp.data;
			        	  var zb =document.getElementById('zbs');
			        	  if(count!=null){
				      		for(var i=0;i<count.length;i++){
				      			zb.innerHTML+='<li> <a href='+count[i].url+'><div class="zbsimg"><img src='+count[i].img+'></div><dl><dt>'+count[i].title+'</dt><dd>'+count[i].abstract+'<p><span >直播中<img src="images/home_2.png"></span>'+count[i].nid+'</p></dd></dl></a> </li>'; 
				      		}
			        	  }else{
			        		  zb.innerHTML+='<li> <a href="#"><div class="zbsimg"><img src="images/zt.jpg"></div><dl><dt>当前无直播</dt><dd>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<p><span class="zant" >无直播<img src="images/home_3.png"></span>0</p></dd></dl></a> </li>'; 
			        	  }
			          }  
			      });
		}
		}else if(cursel==2){
			var l = $("#con_one_2").html();
			var s = $.trim(l).length;
			if(s==0){
				$.ajax({
					type:'post',
					url:newsMain,
					data:{
						sid:'10002',
						pn:'1'
					},
			       success : function(json) {
				var count = json.Resp.data;
				if(count!=null){
					for(var i=0;i<count.length;i++){
						document.getElementById('con_one_2').innerHTML+=' <dl class="homenews"><dt>【'+count[i].type+'】</dt><dd><h2><a href='+count[i].url+'>'+count[i].title+'</a></h2><p>'+count[i].abstract+'</p><span>'+count[i].date+'<i><img src="images/home_5.png">'+count[i].nid+'</i></span></dd></dl>'; 
					}
				}else{
						document.getElementById('con_one_2').innerHTML+=' <dl class="homenews"><dt>【无最新资讯】</dt><dd><h2><a href="#">无最新资讯</a></h2></dd></dl>';
					}
				}
			});
		}}else if(cursel==3){
			var leng = $("#rili").html();
			var sizes = $.trim(leng).length;
			if(sizes==1086||sizes==1085){
				$.ajax({
					type:'post',
					url: newsMain,
					data:{
						sid:'10003',
						date:'2016-09-21'
					},
			      success : function(json) {
				var count = json.Resp.data;
				if(count!=null){
					for(var i=0;i<count.length;i++){
						document.getElementById('rili').innerHTML+=' <li><p class="rilitime"><A href='+count[i].url+'><img src="images/blan.png" ></A>'+count[i].date+'</p><p>'+count[i].title+'</p><p class="qys">'+count[i].abstract+'</p><p><img src="images/home_8.png" width="15" height="14"> <img src="images/home_8.png" width="15" height="14"> <img src="images/home_8.png" width="15" height="14"> <img src="images/home_9.png" width="15" height="14"> <img src="images/home_9.png" width="15" height="14"> </p></li>'; 
					}
				}else{
					document.getElementById('rili').innerHTML+='';
				}
				}
			});
		}else{
			if(T_ymd!=null&&T_ymd!=""&&typeof(T_ymd)!= undefined){
				$.ajax({
					type:'post',
					url: newsMain,
					data:{
						sid:'10003',
						date:T_ymd
					},
			      success : function(json) {
				var count = json.Resp.data;
				if(count!=null){
					for(var i=0;i<count.length;i++){
						document.getElementById('rili').innerHTML+=' <li><p class="rilitime"><A href='+count[i].url+'><img src="images/blan.png" ></A>'+count[i].date+'</p><p>'+count[i].title+'</p><p class="qys">'+count[i].abstract+'</p><p><img src="images/home_8.png" width="15" height="14"> <img src="images/home_8.png" width="15" height="14"> <img src="images/home_8.png" width="15" height="14"> <img src="images/home_9.png" width="15" height="14"> <img src="images/home_9.png" width="15" height="14"> </p></li>'; 
					}
				}else{
					document.getElementById('rili').innerHTML=' <p class="rilitime">'+T_ymd+'</p><p>当天无任何财经信息</p></li>';
				}
				}
			});
			}
		}
			
			
			
			
			var newDate = new Date();
			var date_year = newDate.getFullYear();
			var date_month = newDate.getMonth() + 1;
			var date_today = newDate.getDate();
			var date_day = newDate.getDay();
			//计算一个月内的天数,注意闰月
				var dayNum_in_month = [31,28,31,30,31,30,31,31,30,31,30,31];
				var isleapyear = date_year % 4;
				if(isleapyear == 0)
				{
					dayNum_in_month[1] = 29;
				}	
				var month_alldays = dayNum_in_month[date_month - 1]; //一共天数
				function GetDateYMD(AddDayCount,_ymd) {    
					   var dd = new Date();  
					   dd.setDate(dd.getDate()+AddDayCount);//获取AddDayCount天后的日期  
					   var y = dd.getFullYear();   
					   var m = (dd.getMonth()+1)<10?"0"+(dd.getMonth()+1):(dd.getMonth()+1);//获取当前月份的日期，不足10补0  
					   var d = dd.getDate()<10?"0"+dd.getDate():dd.getDate();//获取当前几号，不足10补0  
					   if(_ymd=="y"){
						   return y;
					   }else if(_ymd =="m"){
						   return m;
					   }else if(_ymd =="d"){
						   return d;
					   }else if(_ymd =="ymd"){
						   return y+"-"+m+"-"+d; 
					   }
					}
				var set_Tday =[-10,-9,-8,-7,-6,-5,-4,-3,-2,-1,0,1,2,3,4,5,6,7,8,9]; //获取当前日期前后十天
				
				//-----------------------------------------------------------------------------
				var count_TDay =[];
				// 获取需要的日期 ****-**-**
				for(var i=0;i<set_Tday.length;i++){
					count_TDay+= GetDateYMD(set_Tday[i],"ymd");
				}
				var count_Week =new Array();
				var week="";
				// 获取星期  0代表日
				for(var i=0;i<count_TDay.length;i++){
					if(!i%9==0||i==0){
						week+=count_TDay[i];
						if(week.length==10){
							count_Week +=new Date(week).getDay();
							week="";
						}
					}
				}
				var count_Day ="";
				// 获取 dd 20
				for(var i=0;i<set_Tday.length;i++){
					if(i<=19){
					count_Day+= GetDateYMD(set_Tday[i],"d")+",";
					}
				}
				var set_Day = new Array();
				 set_Day = count_Day.split(",");
				 
				var count_Mm ="";
				// 获取 mm 20
				for(var i=0;i<set_Tday.length;i++){
					if(i<=19){
					count_Mm+= GetDateYMD(set_Tday[i],"m")+",";
					}
				}
				var set_Mm = new Array();
				 set_Mm = count_Mm.split(",");
				var count_Yy ="";
				// 获取 YY 20
				for(var i=0;i<set_Tday.length;i++){
					if(i<=19){
					count_Yy+= GetDateYMD(set_Tday[i],"y")+",";
					}
				}
				var set_Yy = new Array();
				 set_Yy = count_Yy.split(",");
				
				var show_week= ['日','一','二','三','四','五','六']; 
				var weekstr="";
				for(var i=0;i<count_Week.length;i++){
					if(i<=19){
					weekstr+=show_week[count_Week[i]]+" ";
					}
				}
				var set_week = new Array();
				set_week =weekstr.split(" ");
				for(var k=0;k<set_Day.length;k++){
					if(set_Day[k]==date_today){
						set_week[k]="今";
					}
				}
				$("#month").text(date_month+"月");
				for(var j=0;j<set_Day.length;j++){
					if(j==10){
						document.getElementById('calendar').innerHTML+='<li class="rlhover"  onclick="set_clickTime(this)" ><p class="pday">'+set_Day[j]+'</p><span class="sday">'+set_week[j]+'</span><span class="hM">'+set_Mm[j]+'</span><span class="hM">'+set_Yy[j]+'</span></li>';
					}else{
						document.getElementById('calendar').innerHTML+='<li class="rlhover"  onmouseover="setFocus(this);setTouch(this)" onclick="set_clickTime(this)" onmouseout="setFocusOut(this);setTouch(this)" ><p>'+set_Day[j]+'</p><span>'+set_week[j]+'</span><span class="hM">'+set_Mm[j]+'</span><span class="hM">'+set_Yy[j]+'</span></li>';
					}
				}	
				//判断客户端
//				var sUserAgent = navigator.userAgent.toLowerCase();
//			    var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
//			    var bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
//			    var bIsMidp = sUserAgent.match(/midp/i) == "midp";
//			    var bIsUc = sUserAgent.match(/ucweb/i) == "ucweb";
//			    var bIsAndroid = sUserAgent.match(/android/i) == "android";
//			    var bIsCE = sUserAgent.match(/windows ce/i) == "windows ce";
//			    var bIsWM = sUserAgent.match(/windows mobile/i) == "windows mobile";
//			    
//			//	alert("屏幕分辨率"+screen.width + "*" + screen.height );
//			//	alert("屏幕可显示面积："+screen.availWidth + "*" + screen.availHeight);
//			    
//			    if (!(bIsIpad || bIsIphoneOs || bIsMidp || bIsUc || bIsAndroid || bIsCE || bIsWM) ){
//			        //window.location.href=B页面;
//			    }
			     // 7    13
				var Iswith =  screen.width;
				var _hidden =document.getElementById('calendar').children;
					if(Iswith>650){
						for(var i =0;i<3;i++){
							_hidden[i].style.display="none";
						}
					}else{
						for(var i =0;i<6;i++){
							_hidden[i].style.display="none";
						}
					
				}
				
		}
	 
 }
 
function set_clickTime(obj){
	 var _click =obj.childNodes;
	 var _cTime =_click[0].textContent;
	 var _cM = _click[2].textContent;
	 var _cY = _click[3].textContent;
	 var _ymd=_cY+"-"+_cM+"-"+_cTime;
	 switchTab(3,_ymd);
 };
	//当鼠标指针指到当前日期单元格
	function setFocus(obj)
	{
		var _node = obj.childNodes;
		_node[0].style.backgroundColor = "#e93030";
		_node[0].style.color="#fff";
		_node[1].style.color="#e93030";
		_node[0].style.cursor="default";
		_node[1].style.cursor="default";
	}
	//当鼠标指针离开当前日期单元格
	function setFocusOut(obj)
	{
		var _node = obj.childNodes;
		_node[0].style.backgroundColor = "#fff";
		_node[0].style.color="#e93030";
		_node[1].style.color="#999";
	}
	function setTouch(obj){  
	    document.addEventListener('touchstart',touch, false);  
	    document.addEventListener('touchmove',touch, false);  
	    document.addEventListener('touchend',touch, false);  
	    function touch (event){  
	        var event = event || window.event;  
	        if(event.type=="touchstart"){
	        	var size =document.getElementById('calendar').childNodes;
	        	var _node = obj.childNodes;
	        	var str =_node[0].textContent+_node[1].textContent+_node[2].textContent+_node[3].textContent;
	        	for(var i=0;i<size.length;i++){
	        		if(size[i].textContent==str){	
	        			 var a =size[i].childNodes;		//白
	        			a[0].style.backgroundColor = "#fff";
	        									//红
	        			a[0].style.color="#e93030";
	        								//灰
	        			a[1].style.color="#999";
	        			str="";
	        		}
	        	}
	        }else{
	        	var _node = obj.childNodes;			//红
	    		_node[0].style.backgroundColor = "#e93030";
	    		_node[0].style.color="#fff";
	    		_node[1].style.color="#e93030";
	        }
	    }  
	}  
		var click_Num = 3;
		var Isw =  screen.width;
	function click_Btleft(){
		var va =document.getElementById('calendar').children;
		if(Isw>650){
			if(click_Num<6){
				click_Num++;
				va[0].style.display="none";
				for(var i =0;i<click_Num;i++){
					va[i].style.display="none";
				}
			}
		}else{
			click_Num=6;
			if(click_Num<11){
				click_Num++;
				va[0].style.display="none";
				for(var i =0;i<click_Num;i++){
					va[i].style.display="none";
				}
			}
		}
	}
	function click_Btright(){
		var va =document.getElementById('calendar').children;
		if(click_Num>0){
			va[click_Num-1].style.display="block";
			click_Num--;
		}
	}
	
	
	
	
	
