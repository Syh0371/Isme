package cn.together.user.web.ctrl.payment;

import cn.together.api.crud.base.ActCouponServiceApi;
import cn.together.api.crud.base.StoreServiceApi;
import cn.together.api.crud.base.SubjectServiceApi;
import cn.together.api.order.base.OrderServiceApi;
import cn.together.api.order.base.OrderSkuServiceApi;
import cn.together.api.order.base.PaymentServiceApi;
import cn.together.api.order.base.PromSkuServiceApi;
import cn.together.api.order.base.SkuServiceApi;
import cn.together.api.sys.base.SmsServiceApi;
import cn.together.api.user.base.UserServiceApi;
import cn.together.common.core.dto.Result;
import cn.together.common.core.http.TogHttpResponse;
import cn.together.common.core.util.RandomUtil;
import cn.together.common.core.util.TimeUtil;
import cn.together.common.util.XmlUtil;
import cn.together.common.webapp.config.Configuration;
import cn.together.pojo.order.Order;
import cn.together.pojo.order.OrderPromSku;
import cn.together.pojo.order.OrderSku;
import cn.together.pojo.order.OrderStatus;
import cn.together.pojo.order.PayChildInfo;
import cn.together.pojo.order.Payment;
import cn.together.pojo.order.PaymentChild;
import cn.together.pojo.prom.*;
import cn.together.pojo.set.Store;
import cn.together.pojo.subject.SkuCom;
import cn.together.pojo.subject.SubSku;
import cn.together.pojo.subject.SubSkuStore;
import cn.together.pojo.subject.Subject;
import cn.together.pojo.user.User;
import cn.together.user.base.AbstractController;
import cn.together.user.constant.Constant;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import io.goeasy.GoEasy;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * 支付相关的回调 和 准备
 *
 */
@Controller
@RequestMapping("/payment")
public class PaymentController extends AbstractController {

    private Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentServiceApi paymentServiceApi;
    @Autowired
    private ActCouponServiceApi actCouponServiceApi;
    @Autowired private OrderServiceApi orderServiceApi;
    @Autowired private StoreServiceApi storeServiceApi;
    @Autowired private UserServiceApi userServiceApi;
    @Autowired private SmsServiceApi smsServiceApi;
    @Autowired private PromSkuServiceApi promSkuServiceApi;
    @Autowired private SkuServiceApi skuServiceApi;
    @Autowired private OrderSkuServiceApi orderSkuServiceApi;

    @Autowired private SubjectServiceApi subjectServiceApi;

    private GoEasy goEasy = new GoEasy(Configuration.getString("GoEasy.AppKey"));

    /**
     *
     * 准备支付条件
     * @param orderId
     * @param openid
     * @return
     */
    @RequestMapping(value = "/prepay",method = RequestMethod.GET)
    @ResponseBody
    public TogHttpResponse prepay(@RequestParam long orderId,
                                  @RequestParam String openid) {
        Order order = orderServiceApi.getOrder(orderId);

        /**
         *
         * 查询特惠项目
         */
        List<OrderSku> orderSkuList = orderSkuServiceApi.listByOrder(order.getId());
        OrderSku firstOrderSku = orderSkuList.get(0);
        OrderPromSku orderPromSku = promSkuServiceApi.queryOrderPromSkuById(firstOrderSku.getSkuComId());
        PromSku promSku = promSkuServiceApi.getPromSku(orderPromSku.getPromSkuId());
        PromSkuDetail promSkuDetail = promSkuServiceApi.queryPromSkuDetail(orderPromSku.getDetailId());

        if (promSkuDetail == null) return TogHttpResponse.FAILED;

        String appid = "wx749344c1b88932c2";
        String body = promSku.getPromSkuName();
        String deviceInfo = "WEB";
        String mechId = "1457615102";
        String notifyUrl = "https://user.jointogether.cn/payment/wechat/callback";
        String nonceStr = RandomUtil.randomStr(18);
        String outTradeNo = order.getOrderNo();
        String totalFee = String.valueOf(promSkuDetail.getPrice()
                .setScale(2, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(Constant.HUNDRED)).intValue());
        String tradeType = "JSAPI";
        String sign;

        String key = "YxwhFfJImN1ilboFjyRgUmjoaVsw4pLt";

        String signStr = "appid=" + appid + "&" + "body=" + body + "&device_info=" + deviceInfo + "&mch_id=" + mechId + "&nonce_str=" + nonceStr + "&notify_url=" + notifyUrl
                + "&openid=" + openid + "&out_trade_no=" + outTradeNo + "&total_fee=" + totalFee + "&trade_type=" + tradeType + "&key=" + key;

        sign = getMD5(signStr).toUpperCase();

        Map<String, String> param = new HashMap<String, String>();
        param.put("appid", appid);
        param.put("openid", openid);
        param.put("body", body);
        param.put("mch_id", mechId);
        param.put("notify_url", notifyUrl);
        param.put("nonce_str", nonceStr);
        param.put("out_trade_no", outTradeNo);
        param.put("sign", sign);
        param.put("total_fee", totalFee);
        param.put("trade_type", tradeType);
        param.put("device_info", deviceInfo);

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/xml");
        String xml = XmlUtil.mapToXml(param);
        StringEntity entity = new StringEntity(xml, "UTF-8");
        entity.setContentType("application/xml");
        entity.setContentEncoding("UTF-8");
        httpPost.setEntity(entity);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            String returnStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            Map<String, String> map = XmlUtil.xmlToMap(returnStr);
            String prepayId = map.get("prepay_id");
            return TogHttpResponse.SUCCESS(getWechatMap(prepayId));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return TogHttpResponse.FAILED("Fail");

    }

    /**
     *
     * 准备支付条件
     * @param orderId
     * @param openid
     * @return
     */
    @RequestMapping(value = "/orderPrepay",method = RequestMethod.GET)
    @ResponseBody
    public TogHttpResponse orderPrepay(@RequestParam long orderId,
                                       @RequestParam String openid,
                                       @RequestParam(required = false, defaultValue = "0",value = "couponDetailId") long couponDetailId) {
        Order order = orderServiceApi.getOrder(orderId);
        Map<String, String> needPayMoney = returnNeedPayMoney(orderId);

        if (needPayMoney.get("needPayMoney") == null) return TogHttpResponse.FAILED;
        String appid = "wx749344c1b88932c2";
        String body = needPayMoney.get("skuName");
        String deviceInfo = "WEB";
        String mechId = "1457615102";
        String notifyUrl = "http://mosl.s1.natapp.cc/payment/wechat/orderCallback";
        String nonceStr = RandomUtil.randomStr(18);
        String outTradeNo = order.getOrderNo();
        String totalFee =String.valueOf(returnPayableMoney(new BigDecimal(needPayMoney.get("needPayMoney")),couponDetailId).multiply(new BigDecimal(Constant.HUNDRED)).intValue()) ;
        String tradeType = "JSAPI";
        String sign;

        String key = "YxwhFfJImN1ilboFjyRgUmjoaVsw4pLt";

        String signStr = "appid=" + appid + "&" + "body=" + body + "&device_info=" + deviceInfo + "&mch_id=" + mechId + "&nonce_str=" + nonceStr + "&notify_url=" + notifyUrl
                + "&openid=" + openid + "&out_trade_no=" + outTradeNo + "&total_fee=" + totalFee + "&trade_type=" + tradeType + "&key=" + key;

        sign = getMD5(signStr).toUpperCase();

        Map<String, String> param = new HashMap<String, String>();
        param.put("appid", appid);
        param.put("openid", openid);
        param.put("body", body);
        param.put("mch_id", mechId);
        param.put("notify_url", notifyUrl);
        param.put("nonce_str", nonceStr);
        param.put("out_trade_no", outTradeNo);
        param.put("sign", sign);
        param.put("total_fee", totalFee);
        param.put("trade_type", tradeType);
        param.put("device_info", deviceInfo);

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/xml");
        String xml = XmlUtil.mapToXml(param);
        StringEntity entity = new StringEntity(xml, "UTF-8");
        entity.setContentType("application/xml");
        entity.setContentEncoding("UTF-8");
        httpPost.setEntity(entity);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            String returnStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            Map<String, String> map = XmlUtil.xmlToMap(returnStr);
            String prepayId = map.get("prepay_id");
            useCouponByUser(couponDetailId,orderId);//消费券回调后确认用券
            return TogHttpResponse.SUCCESS(getWechatMap(prepayId));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return TogHttpResponse.FAILED("Fail");

    }
    /**
     * 微信支付回调
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/wechat/orderCallback", method = RequestMethod.POST)
    public String orderCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, String> resultMap = new HashMap<String, String>();

        String xml = IOUtils.toString(request.getReader());

        logger.info(xml);

        Map<String, String> map = XmlUtil.xmlToMap(xml);

        List<String> keyList = new ArrayList<String>();
        for (String key : map.keySet()) {
            if (!key.endsWith("sign")) {
                keyList.add(key);
            }
        }

        Collections.sort(keyList);

        String signStr = "";
        for (String key : keyList) {
            signStr += key + "=" + map.get(key) + "&";
        }

        signStr += "key=YxwhFfJImN1ilboFjyRgUmjoaVsw4pLt";
        logger.info(signStr);
        String sign = getMD5(signStr).toUpperCase();

        logger.info(sign);

        String return_code = "";
        if (sign.endsWith(map.get("sign"))) {
            String outTradeNo = map.get("out_trade_no");
            final Order order = orderServiceApi.queryByOrderNo(outTradeNo);

            if (order != null && order.exists()) {
                orderServiceApi.updateStatus(order.getId(), OrderStatus.ORDER_STATUS_FORMAL.getValue());
                Payment orderPayment = paymentServiceApi.queryPaymentByOrderId(order.getId());

                if (orderPayment == null) {

                    Payment payMent = new Payment();
                    payMent.setOrderId(order.getId());
                    payMent.setUserId(order.getUserId());

                    User user = userServiceApi.get(order.getUserId());
                    payMent.setPayer(user.getName());

                    payMent.setTradeNo(String.valueOf(System.currentTimeMillis()));
                    payMent.setFinishTime(new Date());

                    Long totalFee = Long.valueOf(map.get("total_fee"));
                    payMent.setFee(new BigDecimal(totalFee / (Constant.HUNDRED * 1.0)));
                    Payment savedPayment = paymentServiceApi.addPayment(payMent);//实付
                    Couponrecord couponrecord = actCouponServiceApi.getUseCouponByOrderId(order.getId());//根据订单查询选择的券
                    Couponrule couponrule = new Couponrule();
                    BigDecimal discountFee = new BigDecimal("0.00");
                    long couponDetailId  = 0;
                    if(couponrecord!=null&&couponrecord.getId()>0){
                         actCouponServiceApi.UseCouponRecord(couponrecord.getId(),order.getId());//用券
                         actCouponServiceApi.UseCouponByUser(couponrecord.getCoupDetailedId());
                         couponrule = actCouponServiceApi.getCouponruleByCoupon(couponrecord.getCouponId());//优惠金额
                         BigDecimal totalAmount = returnTotalAmount(couponrule,new BigDecimal(totalFee / (Constant.HUNDRED * 1.0)));//应付
                         discountFee = totalAmount.subtract(savedPayment.getFee());//优惠
                        couponDetailId = couponrecord.getCoupDetailedId();
                    }
                    List<OrderSku> orderSkuList = orderSkuServiceApi.listByOrder(order.getId());
                    savePaymentInfo(orderSkuList,discountFee,savedPayment.getId(),couponDetailId);



                    Boolean isDebug = Configuration.getBoolean("Debug");
                    if (!isDebug) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendAppointMsg(order.getId());
                                sendMessage(order);
                            }
                        }).start();
                    }
                }
            }
            return_code = "SUCCESS";

            resultMap.put("return_code", return_code);
            resultMap.put("return_msg", "OK");

            PrintWriter out = response.getWriter();
            response.setContentType("text/plain");

            out.print(XmlUtil.mapToXml(resultMap));

            return null;

        }
        return_code = "FAIL";

        resultMap.put("return_code", return_code);
        resultMap.put("return_msg", "OK");

        PrintWriter out = response.getWriter();
        response.setContentType("text/plain");

        out.print(XmlUtil.mapToXml(resultMap));

        return null;
    }

    private void savePaymentInfo(List<OrderSku> orderSkuList,BigDecimal discountFee,long payMentId,long couponDetailId){
        BigDecimal discount = discountFee;
        for (OrderSku sku:orderSkuList) {
            PaymentChild paymentChild = new PaymentChild();
            paymentChild.setOrderSkuId(sku.getId());
            paymentChild.setPayId(payMentId);
            PaymentChild savedPaymentChild = paymentServiceApi.savePaymentChild(paymentChild);
            SkuCom skuCom = skuServiceApi.querySkuCom(sku.getSkuComId());
            int r=discount.compareTo(BigDecimal.ZERO);

             //大于0&& 不等于0
            if(r==1 && r!=0){
                discount = discount.subtract(skuCom.getPrice()).setScale(2, BigDecimal.ROUND_HALF_UP);
                int check = discount.compareTo(BigDecimal.ZERO);
                 //大于0
                if(check == 1){
                    PayChildInfo payChildInfo = new PayChildInfo();
                    payChildInfo.setFee(skuCom.getPrice());
                    payChildInfo.setPayChildId(savedPaymentChild.getId());
                    payChildInfo.setPayType(10L);
                    payChildInfo.setDesc(String.valueOf(couponDetailId));
                    paymentServiceApi.savePaymentChildInfoByCoupon(payChildInfo);
                    //小于0
                }else if(check==-1){
                    discount = discount.abs();//负数转正数
                    PayChildInfo payChildInfoTwo = new PayChildInfo();
                    payChildInfoTwo.setFee(skuCom.getPrice().subtract(discount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    payChildInfoTwo.setPayChildId(savedPaymentChild.getId());
                    payChildInfoTwo.setPayType(10L);//优惠
                    payChildInfoTwo.setDesc(String.valueOf(couponDetailId));
                    paymentServiceApi.savePaymentChildInfoByCoupon(payChildInfoTwo);
                    PayChildInfo payChildInfo = new PayChildInfo();
                    payChildInfo.setFee(discount);
                    payChildInfo.setPayChildId(savedPaymentChild.getId());
                    payChildInfo.setPayType(2L);//微信支付
                    paymentServiceApi.savePaymentChildInfoByCoupon(payChildInfo);
                    discount = new BigDecimal("0.00");
                }else{
                    PayChildInfo payChildInfo = new PayChildInfo();
                    payChildInfo.setFee(skuCom.getPrice());
                    payChildInfo.setPayChildId(savedPaymentChild.getId());
                    payChildInfo.setPayType(10L);
                    payChildInfo.setDesc(String.valueOf(couponDetailId));
                    paymentServiceApi.savePaymentChildInfoByCoupon(payChildInfo);
                }
                //等于0
            }else if( r==0 ){
                PayChildInfo payChildInfo = new PayChildInfo();
                payChildInfo.setFee(skuCom.getPrice());
                payChildInfo.setPayChildId(savedPaymentChild.getId());
                payChildInfo.setPayType(2L);
                paymentServiceApi.savePaymentChildInfoByCoupon(payChildInfo);
            }

        }
    }
    private BigDecimal returnTotalAmount(Couponrule couponrule,BigDecimal fee){
        BigDecimal TotalAmount = new BigDecimal("0.00");
        if(couponrule!=null && couponrule.getId()>0){
            Coupon coupon = actCouponServiceApi.getCoupon(couponrule.getCouponId());
            Couponrule couponRule = actCouponServiceApi.getCouponruleByCoupon(coupon.getId());
            if(coupon.getCouponType()==2){
                BigDecimal discountPrice=new BigDecimal(couponRule.getPrice());
                TotalAmount = fee.add(discountPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            }else if(coupon.getCouponType()==3){
                BigDecimal dis = new BigDecimal(couponRule.getDiscount()).divide(BigDecimal.valueOf(Constant.HUNDRED));
                TotalAmount = fee.divide(dis).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }else{
            TotalAmount = fee;
        }
        return TotalAmount;
    }


    private Map<String, String> returnNeedPayMoney(long orderId){
        Map<String, String> map = new HashMap<String, String>();
        List<OrderSku> orderSkuList = orderSkuServiceApi.listByOrder(orderId);
        BigDecimal needPay = new BigDecimal("0.00");
        String skuName = "";
        for (OrderSku sku:orderSkuList ) {
            SkuCom skuCom = skuServiceApi.querySkuCom(sku.getSkuComId());
            needPay =  needPay.add(skuCom.getPrice());
            if(skuName.length()<20){
                skuName += skuCom.getAliasName()+",";
            }
        }
        skuName  = skuName.substring(0,skuName.length()-1);
        map.put("needPayMoney", needPay.toString());
        map.put("skuName", skuName);
        return map;
    }
    @RequestMapping(value = "/returnPayMoneyByCoupon", method = RequestMethod.GET)
    @ResponseBody
    public TogHttpResponse returnPayMoneyByCoupon(@RequestParam String money ,@RequestParam long couponDetailId) {
        return TogHttpResponse.SUCCESS(returnPayableMoney(new BigDecimal(money),couponDetailId).toString());
    }
    private BigDecimal returnPayableMoney(BigDecimal needMoney, long couponDetailId){
        BigDecimal payableMoney = new BigDecimal("0.00");
        if(couponDetailId > 0){
            Coupon coupon = actCouponServiceApi.getCoupon(actCouponServiceApi.getCoupondetailed(couponDetailId).getCouponId());
            Couponrule couponRule = actCouponServiceApi.getCouponruleByCoupon(coupon.getId());
            if(coupon.getCouponType()==2){
                BigDecimal discountPrice=new BigDecimal(couponRule.getPrice());
                payableMoney = needMoney.subtract(discountPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            }else if(coupon.getCouponType()==3){
                BigDecimal dis = new BigDecimal(couponRule.getDiscount()).divide(BigDecimal.valueOf(Constant.HUNDRED));
                payableMoney = needMoney.multiply(dis).setScale(2, BigDecimal.ROUND_HALF_UP);
            }else if(coupon.getCouponType()==1){
                BigDecimal discountPrice=new BigDecimal(couponRule.getPrice());
                payableMoney = needMoney.subtract(discountPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            }else{
                payableMoney = needMoney;
            }

        }else{
            payableMoney = needMoney;
        }
        int r=payableMoney.compareTo(BigDecimal.ZERO);
        if(r==-1||r==0)payableMoney = new BigDecimal("0.01");
        return payableMoney;
    }

    private Result useCouponByUser(long couponDetailId,long orderId){
        Result result  = new Result(true);
        if(couponDetailId>0){
            Couponrecord checkRecord = actCouponServiceApi.checkIsHave(couponDetailId,orderId);
            if(checkRecord == null){
                Coupondetailed coupondetailed  =actCouponServiceApi.getCoupondetailed(couponDetailId);
                Couponrecord couponrecord =  actCouponServiceApi.addCouponrecord(new Couponrecord(coupondetailed.getCouponId(),coupondetailed.getUserId(),couponDetailId,2,0,1,orderId));
                if(couponrecord==null || couponrecord.getId()<=0) result.setResult(false);
            }
        }return result;
    }

    public Map<String, String> getWechatMap(String prepayId) {
        String appid = "wx749344c1b88932c2";
        String timeStamp = String.valueOf(new Date().getTime());
        String nonceStr = RandomUtil.randomStr(18);
        String packageStr = "prepay_id=" + prepayId;
        String signType = "MD5";

        String stringA = "appId=wx749344c1b88932c2&nonceStr=" + nonceStr + "&package=prepay_id=" + prepayId + "&signType=MD5" + "&timeStamp=" + timeStamp;
        String stringSignTemp = stringA + "&key=YxwhFfJImN1ilboFjyRgUmjoaVsw4pLt";
        String paySign = getMD5(stringSignTemp).toUpperCase();

        Map<String, String> map = new HashMap<String, String>();
        map.put("appid", appid);
        map.put("timeStamp", timeStamp);
        map.put("nonceStr", nonceStr);
        map.put("package", packageStr);
        map.put("signType", signType);
        map.put("paySign", paySign);

        return map;
    }

    public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 微信支付回调
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/wechat/callback", method = RequestMethod.POST)
    public String wechatCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, String> resultMap = new HashMap<String, String>();

        String xml = IOUtils.toString(request.getReader());

        logger.info(xml);

        Map<String, String> map = XmlUtil.xmlToMap(xml);

        List<String> keyList = new ArrayList<String>();
        for (String key : map.keySet()) {
            if (!key.endsWith("sign")) {
                keyList.add(key);
            }
        }

        Collections.sort(keyList);

        String signStr = "";
        for (String key : keyList) {
            signStr += key + "=" + map.get(key) + "&";
        }

        signStr += "key=YxwhFfJImN1ilboFjyRgUmjoaVsw4pLt";
        logger.info(signStr);
        String sign = getMD5(signStr).toUpperCase();

        logger.info(sign);

        String return_code = "";
        if (sign.endsWith(map.get("sign"))) {
            String outTradeNo = map.get("out_trade_no");
            final Order order = orderServiceApi.queryByOrderNo(outTradeNo);

            if (order != null && order.exists()) {
                orderServiceApi.updateStatus(order.getId(), OrderStatus.ORDER_STATUS_FORMAL.getValue());
                Payment orderPayment = paymentServiceApi.queryPaymentByOrderId(order.getId());

                if (orderPayment == null) {
                    Payment payMent = new Payment();
                    payMent.setOrderId(order.getId());
                    payMent.setUserId(order.getUserId());

                    User user = userServiceApi.get(order.getUserId());
                    payMent.setPayer(user.getName());

                    payMent.setTradeNo(String.valueOf(System.currentTimeMillis()));
                    payMent.setFinishTime(new Date());

                    Long totalFee = Long.valueOf(map.get("total_fee"));
                    payMent.setFee(new BigDecimal(totalFee / (Constant.HUNDRED * 1.0)));
                    Payment savedPayment = paymentServiceApi.addPayment(payMent);

                    List<OrderSku> orderSkuList = orderSkuServiceApi.listByOrder(order.getId());
                    OrderSku firstOrderSku = orderSkuList.get(0);

                    PaymentChild paymentChild = new PaymentChild();
                    paymentChild.setOrderSkuId(firstOrderSku.getId());
                    paymentChild.setPayId(savedPayment.getId());
                    PaymentChild savedPaymentChild = paymentServiceApi.savePaymentChild(paymentChild);

                    PayChildInfo payChildInfo = new PayChildInfo();
                    payChildInfo.setFee(new BigDecimal(totalFee / (Constant.HUNDRED * 1.0)));
                    payChildInfo.setPayChildId(savedPaymentChild.getId());
                    payChildInfo.setPayType(2L);

                    paymentServiceApi.savePaymentChildInfo(payChildInfo);

                    Boolean isDebug = Configuration.getBoolean("Debug");
                    if (!isDebug) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendAppointMsg(order.getId());
                                sendMessage(order);
                            }
                        }).start();
                    }
                }
            }
            return_code = "SUCCESS";

            resultMap.put("return_code", return_code);
            resultMap.put("return_msg", "OK");

            PrintWriter out = response.getWriter();
            response.setContentType("text/plain");

            out.print(XmlUtil.mapToXml(resultMap));

            return null;

        }
        return_code = "FAIL";

        resultMap.put("return_code", return_code);
        resultMap.put("return_msg", "OK");

        PrintWriter out = response.getWriter();
        response.setContentType("text/plain");

        out.print(XmlUtil.mapToXml(resultMap));

        return null;
    }

    /**
     * 发送预约短信
     *
     * @param orderId
     */
    private void sendAppointMsg(long orderId) {

        Order order = orderServiceApi.getOrder(orderId);
        User user = userServiceApi.get(order.getUserId());
        List<SkuCom> skuList = getSkuList(orderId);

        String skuJson = "";
        for (SkuCom skuCom : skuList) {
            skuJson += skuCom.getAliasName() + ",";
        }

        skuJson = skuJson.substring(0, skuJson.length() - 1);

        Store store = storeServiceApi.getStore(order.getStoreId());
        smsServiceApi.sendAppointMsg(user.getMobile(), order.getPersonCount(), user.getName(), skuJson, TimeUtil.STANDARD_DATE_HOUR_MINUTE_FORMAT.format(order.getStartTime()),
                store.getName(), store.getAddress(), store.getTel());
    }

    private void sendMessage(Order order) {
        goEasy.publish("order_channel", JSON.toJSONString(order));
    }

    private List<SkuCom> getSkuList(long orderId) {

        List<SkuCom> skuComList = new ArrayList<SkuCom>();

        List<OrderSku> orderSkuList = orderSkuServiceApi.listByOrder(orderId);
        for (OrderSku orderSku : orderSkuList) {

            if (orderSku.getType() == OrderSku.TYPE.FORMAL) {
                SkuCom skuCom = skuServiceApi.querySkuCom(orderSku.getSkuComId());
                SubSkuStore subSkuStore = skuServiceApi.querySubSkuStoreById(skuCom.getSkuStoreId());
                SubSku subSku = skuServiceApi.getSubSku(subSkuStore.getSkuId());
                skuCom.setSubSku(subSku);
                skuComList.add(skuCom);
            } else if (orderSku.getType() == OrderSku.TYPE.DISCOUNT) {
                OrderPromSku orderPromSku = promSkuServiceApi.queryOrderPromSkuById(orderSku.getSkuComId());
                PromSku promSku = promSkuServiceApi.getPromSku(orderPromSku.getPromSkuId());
                PromSkuDetail promSkuDetail = promSkuServiceApi.queryPromSkuDetail(orderPromSku.getDetailId());
                SubSku subSku = skuServiceApi.getSubSku(promSku.getSkuId());
                Subject subject = subjectServiceApi.getSubject(subSku.getSubjectId());
                subSku.setSubject(subject);

                SkuCom skuCom = new SkuCom();
                skuCom.setId(orderPromSku.getId());

                skuCom.setAliasName(promSku.getPromSkuName());
                skuCom.setPrice(promSkuDetail.getPrice());

                skuComList.add(skuCom);
            }
        }
        return skuComList;
    }
}
