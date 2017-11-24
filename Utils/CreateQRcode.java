package cn.together.saas.util;

import cn.together.saas.dto.info.FileInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.util.HashMap;

/**
 * @author yahui.Sun
 * @create 2017-11-06 10:39 星期一
 **/
public class CreateQRcode {

    private static final  String TEMP_PREFIX = "temp_qr";
    private static final  int RANDOM_STR_LENGTH = 8;

    /**
     * 生成二维码(QRCode)图片
     * @param content
     */
    public String encoderQRCode(String content) {
        int width = 300;
        int height = 300;
        FileInfo fileInfo =null;
                // 二维码的图片格式
        String format = "png";
        /**
         * 设置二维码的参数
         */
        HashMap hints = new HashMap();
        // 内容所使用编码
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,width,height,hints);
            // 生成二维码
            String fileName  =RandomStringUtils.randomAlphanumeric(RANDOM_STR_LENGTH) + ".png" ;
            File tempFile = File.createTempFile(TEMP_PREFIX,fileName);
            //File outputFile = new File("D:"+ File.separator + "TDC-test.png");
            MatrixToImageWriter.writeToFile(bitMatrix, format, tempFile);
             fileInfo = UploadUtils.upload(tempFile,fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回服务器上传地址
        return  fileInfo.getRealPath();
    }
}
