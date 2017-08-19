package cn.together.user.web.ctrl.wechat;

import cn.together.common.core.util.CastUtil;
import cn.together.user.base.AbstractController;
import cn.together.user.model.WechatAccessToken;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Controller
@RequestMapping("/wechat")
public class WechatController extends AbstractController {

    /**
     *
     * 今日特惠的获取openId
     * @param code
     * @param state
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/promcallback", method = RequestMethod.GET)
    public void promCallback(String code, String state, HttpServletResponse response) throws IOException {

        WechatAccessToken accessToken = getAccessToken(code);

        if ("125".equals(state)) {
            response.sendRedirect("http://test-user.jointogether.cn/user/pay?openid=" + accessToken.getOpenid());
        } else {
            response.sendRedirect("/user/pay?openid=" + accessToken.getOpenid());
        }
    }

    /**
     *
     * 支付订单的获取openId
     * @param code
     * @param state
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/orderCallback", method = RequestMethod.GET)
    public void orderCallback(String code, String state, HttpServletResponse response) throws IOException {

        WechatAccessToken accessToken = getAccessToken(code);

        if ("125".equals(state)) {
            response.sendRedirect("http://mosl.s1.natapp.cc/user/pay_coupon?openid=" + accessToken.getOpenid());
        } else {
            response.sendRedirect("/user/pay_coupon?openid=" + accessToken.getOpenid());
        }
    }
    /**
     * 支付订单预约成功回调
     * @param code
     * @param status
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/appointOrderCallback", method = RequestMethod.GET)
    public void appointOrderCallback(String code, String status, HttpServletResponse response) throws IOException {
        WechatAccessToken accessToken = getAccessToken(code);
        response.sendRedirect("http://mosl.s1.natapp.cc/book_success?openid=" + accessToken.getOpenid());
    }

    /**
     * 预约成功回调
     * @param code
     * @param status
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/appointCallback", method = RequestMethod.GET)
    public void appointCallback(String code, String status, HttpServletResponse response) throws IOException {

        WechatAccessToken accessToken = getAccessToken(code);
        response.sendRedirect("/book_success?openid=" + accessToken.getOpenid());
    }

    /**
     *
     * 获取AccessToken
     * @param code
     * @return
     * @throws IOException
     */
    private WechatAccessToken getAccessToken(String code) throws IOException {

        String redirectUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wx749344c1b88932c2"
                + "&secret=68a65c13b9338aa1ff306f75d0dbadf7&code=" + code + "&grant_type=authorization_code";

        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(redirectUrl);
        HttpResponse response = httpClient.execute(httpGet);

        String returnStr = EntityUtils.toString(response.getEntity(), "UTF-8");

        JSONObject responseJson = JSON.parseObject(returnStr);
        WechatAccessToken accessToken = CastUtil.toObject(responseJson, WechatAccessToken.class);

        return accessToken;
    }


}
