package cn.together.saas.util;

import cn.together.common.core.util.CastUtil;
import cn.together.saas.dto.info.FileInfo;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;

/**
 * @author yahui.Sun
 * @create 2017-11-06 10:52 星期一
 **/
public class UploadUtils {
    private UploadUtils() {
    }

    private static final String UPLOAD_PATH = "https://img.jointogether.cn/upload/file/";

    public static FileInfo upload(File file, String fileName) throws Exception {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        FileBody fileBody = new FileBody(file);
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();

        HttpPost request = new HttpPost(UPLOAD_PATH);
        request.setEntity(reqEntity);

        HttpResponse response = httpclient.execute(request);
        String respString = EntityUtils.toString(response.getEntity(), "UTF-8");
        FileInfo fileInfo = CastUtil.toObject(JSONObject.parseObject(respString).getJSONObject("data"), FileInfo.class);
        fileInfo.setName(fileName);
        return fileInfo;
    }
}
