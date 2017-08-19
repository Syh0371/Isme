package cn.together.trader.util;

import cn.together.common.core.util.ImageUtil;
import cn.together.trader.dto.JsoupDTO;
import cn.together.trader.dto.JsoupText;
import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import javax.swing.text.html.HTML;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Together_IT on 2017/8/2.
 */
public class JsoupUtils {

    public static String htmlToJson(String  html) throws IllegalAccessException {
        List<JsoupDTO> jsoupDTOList = new ArrayList<JsoupDTO>();
        html = "<html><body>" + html + "</body></html>";
        Document doc = Jsoup.parse(html);
        Elements texts = doc.select("p");
        for (Element text:texts ) {
            for (Node node:text.childNodes() ) {
                 JsoupDTO jsoup = new JsoupDTO();
                 if(node.nodeName()=="img"){
                     jsoup.setImg(node.attr("src"));
                 }else if(node.nodeName()=="b"){
                     if(node.childNodeSize()>0&&node.childNode(0).nodeName()!="br"){
                         if(node.childNode(0).nodeName()=="font"&&node.childNode(0).childNode(0).attr("text")!=""){
                             jsoup.setJsoupText(new JsoupText(node.childNode(0).childNode(0).attr("text"),node.childNode(0).attr("color"),true));
                         }else if(node.childNode(0).attr("text")!=""){
                             jsoup.setJsoupText(new JsoupText(node.childNode(0).attr("text"),null,true));
                         }else{
                             jsoup.setJsoupText(null);
                         }
                     }
                 }else if(node.nodeName()=="font"){
                     jsoup.setJsoupText(new JsoupText(node.childNode(0).attr("text"),node.attr("color"),false));
                 }else if(node.nodeName()=="h1"){
                     jsoup.setHead(node.childNode(0).attr("text"));
                 }else if(node.nodeName()!="img"&&node.nodeName()!="br"){
                     jsoup.setJsoupText(new JsoupText(node.attr("text"),null,false));
                 }else if(node.nodeName()=="br"){
                     jsoup = null;
                 }

                 jsoupDTOList.add(jsoup);
            }
        }
        String json = simpleListToJsonStr(jsoupDTOList);
        System.out.println(json);
        return json;
    }

    public static String JsonToHtml(List<JsoupDTO> jsoupDTOS) {
        String Html = "";

            for (JsoupDTO jsoup : jsoupDTOS) {
                if(jsoup != null){
                    if (jsoup.getHead() != "" && jsoup.getHead() != null && !"null".equals(jsoup.getHead())) {
                        Html += "<h1>" + jsoup.getHead() + "</h1>";
                    }
                    if ("null".equals(jsoup.getHead()) && jsoup.getJsoupText() == null && "null".equals(jsoup.getImg())) {

                    } else {
                        Html += "<p>";
                    }

                    if (jsoup.getImg() != "" && jsoup.getImg() != null && !"null".equals(jsoup.getImg())) {
                        Html += "<img src ='" + jsoup.getImg() + "' style= 'max-width: 100%;' />";
                    }
                    if (jsoup.getJsoupText() != null && !"null".equals(jsoup.getJsoupText().getText())) {
                        if (jsoup.getJsoupText().isBold() == true && jsoup.getJsoupText().getFontColor() != "" && jsoup.getJsoupText().getFontColor() != null && !"null".equals(jsoup.getJsoupText().getFontColor())) {
                            Html += "<font color='" + jsoup.getJsoupText().getFontColor() + "' >";
                            Html += "<b>" + jsoup.getJsoupText().getText() + "</b>";
                            Html += "</font>";
                        } else if (jsoup.getJsoupText().isBold() == true && "null".equals(jsoup.getJsoupText().getFontColor())) {
                            Html += "<b>" + jsoup.getJsoupText().getText() + "</b>";
                        } else {
                            Html += jsoup.getJsoupText().getText();
                        }
                    }
                    if ("null".equals(jsoup.getHead()) && jsoup.getJsoupText() == null && "null".equals(jsoup.getImg())) {
                        Html += "<br>";
                    }

                    if ("null".equals(jsoup.getHead()) && jsoup.getJsoupText() == null && "null".equals(jsoup.getImg())) {

                    } else {
                        Html += "</p>";
                    }
                }
            }

        return Html;
    }

    public static String simpleListToJsonStr(List<?> list) throws IllegalArgumentException, IllegalAccessException{
        if(list==null||list.size()==0){
            return "[]";
        }
        String jsonStr = "[";
        for (Object object : list) {
            jsonStr += simpleObjectToJsonStr(object)+",";
        }
        jsonStr = jsonStr.substring(0,jsonStr.length()-1);
        jsonStr += "]";
        return jsonStr;
    }

    public static String simpleObjectToJsonStr(Object obj) throws IllegalArgumentException, IllegalAccessException{
        if(obj==null){
            return "null";
        }
        String jsonStr = "{";
        Class<?> cla = obj.getClass();
        Field fields[] = cla.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if(field.getType() == long.class){
                jsonStr += "\""+field.getName()+"\":"+field.getLong(obj)+",";
            }else if(field.getType() == double.class){
                jsonStr += "\""+field.getName()+"\":"+field.getDouble(obj)+",";
            }else if(field.getType() == float.class){
                jsonStr += "\""+field.getName()+"\":"+field.getFloat(obj)+",";
            }else if(field.getType() == int.class){
                jsonStr += "\""+field.getName()+"\":"+field.getInt(obj)+",";
            }else if(field.getType() == boolean.class){
                jsonStr += "\""+field.getName()+"\":"+field.getBoolean(obj)+",";
            }else if(field.getType() == Integer.class||field.getType() == Boolean.class
                    ||field.getType() == Double.class||field.getType() == Float.class
                    ||field.getType() == Long.class){
                jsonStr += "\""+field.getName()+"\":"+field.get(obj)+",";
            }else if(field.getType() == String.class){
                jsonStr += "\""+field.getName()+"\":\""+field.get(obj)+"\",";
            }
            else if(field.getType() == JsoupText.class){
                String value = simpleObjectToJsonStr(field.get(obj));
                jsonStr += "\""+field.getName()+"\":"+value+",";
            }
        }
        jsonStr = jsonStr.substring(0,jsonStr.length()-1);
        jsonStr += "}";
        return jsonStr;
    }

    public static List<JsoupDTO> jsonStrToList(String jsonStr) {
        List<JsoupDTO> list2=(List<JsoupDTO>)JSONArray.parseArray(jsonStr, JsoupDTO.class);
        return list2;
    }
}
