package cn.together.web.controller;

import cn.together.common.core.http.TogHttpResponse;
import cn.together.web.poj.FilePOJ;
import cn.together.web.util.CheckFileType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Together_IT on 2017/5/22.
 */

@RestController
@RequestMapping("/file")
public class FileController {

    static String filepath = "F:/upload/";

    @RequestMapping(value = "/upLoadFile", method = RequestMethod.POST)
    public TogHttpResponse upLoadFile(HttpServletRequest request,@RequestParam("file") MultipartFile file)
            throws IllegalStateException, IOException {
        //用来检测程序运行时间
        long  startTime=System.currentTimeMillis();
        System.out.println("fileName："+file.getOriginalFilename());
        try {
            File files =new File("F:/upload");
            //如果文件夹不存在则创建
            if  (!files.exists()  && !files.isDirectory())
            {
                System.out.println("//不存在");
                files.mkdir();
            } else
            {
                System.out.println("//目录存在");
            }
            //获取输出流
            OutputStream os=new FileOutputStream("F:/upload/"+file.getOriginalFilename());
            //获取输入流 CommonsMultipartFile 中可以直接得到文件的流
            InputStream is=file.getInputStream();
            int temp;
            //一个一个字节的读取并写入
            while((temp=is.read())!=(-1))
            {
                os.write(temp);
            }
            os.flush();
            os.close();
            is.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long  endTime=System.currentTimeMillis();
        System.out.println("方法的运行时间："+String.valueOf(endTime-startTime)+"ms");
        return TogHttpResponse.SUCCESS("上传成功！");
    }
    @RequestMapping(value = "/fileDown", method = RequestMethod.GET)
    public void fileDown(HttpServletRequest request, HttpServletResponse response,  @RequestParam("filePath") String filePath)
            throws IllegalStateException, IOException {
        String fileName = filePath;
        List<FilePOJ> filePOJList = new ArrayList<FilePOJ>();
        boolean flag = false;
        File file = new File(filepath);
        String[] filelist = file.list();
        filePOJList= filePOJS(filelist);
        Pattern pattern = Pattern.compile(filePath);
        for (FilePOJ f :filePOJList) {
            Matcher matcher = pattern.matcher(f.getFileName());
            if(matcher.find()){
                filePath = f.getAbsolutePath();
                flag=true;
            }
        }

        if(flag==false) {
            filePath = "F:/upload/"+fileName;
        }
        byte[] con;
        con = getBytes(filePath);//
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("multipart/form-data");
        //2.设置文件头：最后一个参数是设置下载文件名(假如我们叫a.pdf)
        response.setHeader("Content-Disposition", "attachment;fileName="+new String((fileName).getBytes(), "iso-8859-1"));
        InputStream is = new ByteArrayInputStream(con);
        ServletOutputStream out = response.getOutputStream();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(out);
            bis = new BufferedInputStream(is);
            byte[] buff = new byte[2048];
            int bytesRead;
            // Simple read/write loop.
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
            bos.flush();
        } catch ( IOException e) {
            bos.close();
            bis.close();
//            os.close();
        }
    }


    /**
     * 读取某个文件夹下的所有文件
     */
    @RequestMapping(value = "/readFile", method = RequestMethod.GET)
    public static TogHttpResponse readFile(@RequestParam(value = "like", required = false, defaultValue = "") String like) throws IOException {
        List<FilePOJ> filePOJList = new ArrayList<FilePOJ>();
        File file = new File(filepath);


        if (!file.isDirectory()) {
            System.out.println("文件");
            System.out.println("path=" + file.getPath());
            System.out.println("absolutepath=" + file.getAbsolutePath());
            System.out.println("name=" + file.getName());
        } else if (file.isDirectory()) {
            String[] filelist = file.list();
            filePOJList= filePOJS(filelist);
        }
        if(!"".equals(like)&&like!=null){
            List<FilePOJ> results = new ArrayList();
            Pattern pattern = Pattern.compile(like, Pattern.CASE_INSENSITIVE);
            for(int i=0; i < filePOJList.size(); i++){
                Matcher matcher = pattern.matcher(((FilePOJ)filePOJList.get(i)).getFileName());
                if(matcher.find()){
                    results.add(filePOJList.get(i));
                }
            }
            return TogHttpResponse.SUCCESS(results);
        }

        Collections.sort(filePOJList, new FilePOJ());


        return TogHttpResponse.SUCCESS(filePOJList);
    }


    private static List<FilePOJ> filePOJS(String[] f) throws IOException {
        List<FilePOJ> filelist = new ArrayList<FilePOJ>();
        for (int i = 0; i < f.length; i++) {
            File readfile = new File(filepath + "\\" + f[i]);
            if (!readfile.isDirectory()) {
                String filetype2 = CheckFileType.getFileByFile(readfile);
                System.out.println(readfile.getName()+"----------------->"+filetype2);
                FilePOJ filePOJ = new FilePOJ();
                FileChannel fc= null;
                FileInputStream fis= new FileInputStream(readfile);
                fc= fis.getChannel();
                filePOJ.setId(i+1);
                filePOJ.setFileImg(getFileImg(filetype2));
                filePOJ.setFileName(readfile.getName());
                filePOJ.setFileSize(bytes2kb(fc.size()));
                filePOJ.setFilePath(readfile.getPath());
                filePOJ.setAbsolutePath(readfile.getAbsolutePath());
                filePOJ.setFileAddTime(getFileAddTime(readfile)==""?getFileUpdateTime(readfile):getFileAddTime(readfile));
                filePOJ.setFileUpdateTime(getFileUpdateTime(readfile));
                filelist.add(filePOJ);
            } else if (readfile.isDirectory()) {
//                        readFile(filepath + "\\" + filelist[i]);
            }
        }
        return filelist;
    }




    /**
     * byte(字节)根据长度转成kb(千字节)和mb(兆字节)
     *
     * @param bytes
     * @return
     */
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1)
            return (returnValue + "MB");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "KB");
    }

    /**
     * 读取修改时间的方法
     */
    public static String getFileUpdateTime(File file){
        Calendar cal = Calendar.getInstance();
        long time = file.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        cal.setTimeInMillis(time);
        //输出：修改时间   2009-08-17 10:32:38
        return  formatter.format(cal.getTime());
    }

    /**
     * 读取添加时间的方法
     */
    public static String getFileAddTime(File file){
        String str= "";
        String addTime = "";
        try {
            Process p = Runtime.getRuntime().exec(String.format("cmd /C dir %s /tc", file.getAbsolutePath()));
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int i = 0;
            while ((str = br.readLine()) != null) {
                i++;
                if (i == 6) {
                    if(str.length()>0){
                        addTime=str.substring(0, 17);
                    }
                }
            }

        } catch (IOException exc) {
            exc.printStackTrace();
        }
        return addTime;
    }
    private byte[] getBytes(String filePath){
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static String getFileImg(String name){
        Map<String,Integer> map=new HashMap<String, Integer>();
        map.put("png", 1);map.put("jpg", 2); //JPEG (jpg)
        map.put("gif", 3);  //GIF (gif)
        map.put("bmp", 5); //Windows Bitmap (bmp)
        map.put("html", 6);  //HTML (html)
        map.put("xml", 9); map.put("zip", 10); map.put("rar", 11);map.put("psd", 12);  //Photoshop (psd)
        map.put("dbx", 13);  //Outlook Express (dbx)
        map.put("xls", 14);  //MS Word
        map.put("doc", 15);  //MS Excel 注意：word 和 excel的文件头一样
        map.put("wpd", 16); //WordPerfect (wpd)
        map.put("pdf", 17);  //Adobe Acrobat (pdf)
        map.put("avi", 18);map.put("jar", 20);map.put("txt", 21); map.put("exe", 22);map.put("log", 23);map.put("sql", 24);
        map.put("ppt", 25);map.put("wpx", 26);map.put("lrc", 27);map.put("js", 28);map.put("jpe", 29);map.put("jpeg", 30);
        map.put("lnk", 31);
            switch (map.get(name)!=null?map.get(name):0){
                case 1:case 2:case 3:case 5:case 29:case 30:
                    return "icon_tp.png";
                case 6:case 25:case 31:
                    return "icon_tp1.png";
                case 9:case 21:case 27:
                    return "icon_wd.png";
                case 10:case 11:case 12:case 20:case 17:case 22:
                    return "icon_ysb1.png";
                case 13:case 14:case 15:case 16:case 26:
                    return "icon_wd2.png";
                case 28:case 23:case 24:
                    return "icon_wd1.png";
                default:
                    return "icon_wjj2.png";
            }
    }
}
