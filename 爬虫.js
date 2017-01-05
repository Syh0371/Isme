���ߣ�yuyuyu
���ӣ�https://zhuanlan.zhihu.com/p/24730075
��Դ��֪��
����Ȩ���������С���ҵת������ϵ���߻����Ȩ������ҵת����ע��������

var http = require('http');      // http ��·
var cheerio = require('cheerio');  // html ����
var fs = require("fs");        // ��

// ���ñ���ѯ��Ŀ����ַ
var queryHref = "http://www.haha.mx/topic/1/new/";   
// ���÷�ҳλ��
var querySearch = 1;                

var urls = [];


/**
 * ����url�Ͳ�����ȡ��ҳ����
 * @param {String}�� url
 * @param {int}�� serach
 */
function getHtml(href, serach) {
  var pageData = "";
  var req = http.get(href + serach, function(res) {
    res.setEncoding('utf8');
    res.on('data', function(chunk) {
      pageData += chunk;
    });

    res.on('end', function() {
      $ = cheerio.load(pageData);
      var html = $(".joke-list-item .joke-main-content a img");

      for(var i = 0; i < html.length; i++) {
        var src = html[i].attribs.src;
        // ɸѡ���ֹ�棬������Ķ���
        if (src.indexOf("http://image.haha.mx") > -1) {
          urls.push(html[i].attribs.src)
        }
      }
      if (serach == pagemax) {
        console.log("ͼƬ���ӻ�ȡ��ϣ�" + urls.length);
        console.log("������������" + urls.length);
        if (urls.length > 0) {
          downImg(urls.shift());
        } else {
          console.log("�������");
        }
      }
    });
  });
}


/**
 * ����ͼƬ
 * @param {String} imgurl��ͼƬ��ַ
 */
function downImg(imgurl) {
  var narr = imgurl.replace("http://image.haha.mx/", "").split("/")
  
  http.get(imgurl.replace("/small/", "/big/"), function(res) {
    var imgData = "";
    //һ��Ҫ����response�ı���Ϊbinary���������������ͼƬ�򲻿�
    res.setEncoding("binary"); 

    res.on("data", function(chunk) {
      imgData += chunk;
    });
    
    res.on("end", function() {
      var savePath = "./upload/topic1/" + narr[0]  + narr[1] + narr[2] + "_" + narr[4];
      fs.writeFile(savePath, imgData, "binary", function(err) {
        if(err) {
          console.log(err);
        }  else {
          console.log(narr[0]  + narr[1] + narr[2] + "_" + narr[4]);
          if (urls.length > 0) {
            downImg(urls.shift());
          } else {
            console.log("�������");
          }
        }
      });
    });
  });
}

var pagemax = 10;    // ��ȡ10ҳ������
function start(){
  console.log("��ʼ��ȡͼƬ����");
  for (var i = 1 ; i <= pagemax ; i++) {
    getHtml(queryHref, i);
  }
}

start();