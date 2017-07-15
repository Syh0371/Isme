package cn.together.saas.web.ctrl.data;

import cn.together.api.crud.base.EmployeeServiceApi;
import cn.together.api.crud.base.StoreServiceApi;
import cn.together.api.data.base.DataServiceApi;
import cn.together.api.func.base.RoleServiceApi;
import cn.together.api.order.base.*;
import cn.together.api.user.base.UserServiceApi;
import cn.together.common.core.dto.PagedList;
import cn.together.common.core.http.TogHttpResponse;
import cn.together.common.core.util.MobileUtil;
import cn.together.common.core.util.SexUtil;
import cn.together.common.core.util.TimeUtil;
import cn.together.pojo.data.Summary;
import cn.together.pojo.func.Role;
import cn.together.pojo.order.*;
import cn.together.pojo.prom.PromSku;
import cn.together.pojo.prom.PromSkuDetail;
import cn.together.pojo.set.Brand;
import cn.together.pojo.set.Employee;
import cn.together.pojo.set.Store;
import cn.together.pojo.subject.SkuCom;
import cn.together.pojo.subject.SubSku;
import cn.together.pojo.subject.SubSkuStore;
import cn.together.pojo.sys.Source;
import cn.together.pojo.user.Sex;
import cn.together.pojo.user.User;
import cn.together.saas.base.AbstractController;
import cn.together.saas.dto.info.FileInfo;
import cn.together.saas.util.UploadUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/data")
public class DataController extends AbstractController {

    @Autowired
    private DataServiceApi dataServiceApi;
    @Autowired
    private RoleServiceApi roleServiceApi;
    @Autowired
    private OrderServiceApi orderServiceApi;
    @Autowired
    private SkuServiceApi skuServiceApi;
    @Autowired
    private EmployeeServiceApi employeeServiceApi;
    @Autowired
    private PaymentServiceApi paymentServiceApi;
    @Autowired
    private StoreServiceApi storeServiceApi;
    @Autowired
    private OrderSkuServiceApi orderSkuServiceApi;
    @Autowired
    private UserServiceApi userServiceApi;
    @Autowired
    private PromSkuServiceApi promSkuServiceApi;

    private static String CREATEEXCEL_NAME = "营业额统计";
    private static String startT = "";
    private static String endtT = "";

    @RequestMapping(value = "/getTimes", method = RequestMethod.GET)
    public TogHttpResponse getTimes() {
        String payUnit = "[{id:1,name:'当天'},{id:2,name:'昨天'},{id:3,name:'本周'},{id:4,name:'上周'},{id:5,name:'本月'},{id:6,name:'上月'},{id:7,name:'自定义'}]";
        return TogHttpResponse.SUCCESS(payUnit);
    }

    @RequestMapping(value = "/getDatas", method = RequestMethod.GET)
    public TogHttpResponse getDatas(@RequestParam long brandId,
                                    @RequestParam long storeId,
                                    @RequestParam long time,
                                    @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
                                    @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime) {

        Set<Long> storeIds = new HashSet<Long>();
        Set<Long> brandIds = new HashSet<Long>();
        if (storeId > 0) {
            storeIds.add(storeId);
        } else {
            storeIds = buildAllStoreIds();
        }
        if (brandId > 0) {
            brandIds.add(brandId);
        } else {
            brandIds = buildAllBrand();
        }

        return TogHttpResponse.SUCCESS(dataServiceApi.getDatas(brandIds, storeIds, time, startTime, endTime));
    }
    @RequestMapping(value = "/setInExcel", method = RequestMethod.POST)
    public TogHttpResponse setInExcel(@RequestParam("filepath") String  filepath,@RequestParam("ext") String ext) throws IOException {
        Workbook wb = null;
            InputStream is = new FileInputStream(filepath);
            if(".xls".equals(ext)){
                wb = new HSSFWorkbook(is);
            }else if(".xlsx".equals(ext)){
                wb = new XSSFWorkbook(is);
            }else{
                wb=null;
            }

        //此时的Workbook应该是从 客户端浏览器上传过来的 uploadFile了,其实跟读取本地磁盘的一个样
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(6);
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
        //总的列数
        int colNum = row.getPhysicalNumberOfCells();


        //遍历excel,从第二行开始 即 rowNum=1,逐个获取单元格的内容,然后进行格式处理,最后插入数据库
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 7; i < rowNum; i++) {
            row = sheet.getRow(i);
            int j = 0;
            User user = new User();
            while (j < colNum) {
                Object obj = getCellFormatValue(row.getCell(j));
                if(obj!=null){
                    if(j==1){
                        String name =(String)obj;
                        if("".equals(name)){
                            user.setName("雅弗会员");
                        }else{
                            user.setName(name);
                        }
                    }
                    if(j==2){
                        String sex = (String)obj;
                        if("男".equals(sex)){
                            user.setSex(1);
                        }else{
                            user.setSex(2);
                        }
                    }
                    if(j==4){
                        String mobile = (String)obj;
                        if("".equals(mobile)){
                            user.setMobile("13661895474");
                        }else{

                            if(MobileUtil.isInvalid(mobile.trim())){
                                System.out.println(getType(mobile.trim()));
                                mobile = hexStringToString(mobile.trim());
                                user.setMobile(mobile.trim());
                            }else{
                                user.setMobile(mobile.trim());
                            }
                        }

                    }
                    user.setSourceId(12);
                }
                j++;
            }
            System.out.println(user.getName()+","+user.getSex()+","+user.getMobile()+","+user.getSourceId());
            User checkUser = userServiceApi.userServiceExist(user.getMobile());
            if(checkUser.getId()<=0) userServiceApi.add(user.getName(),user.getMobile(), Sex.get(user.getSex()),user.getSourceId());
        }

        return TogHttpResponse.SUCCESS("");
    }
    public static String getType(Object o){
        return o.getClass().toString();
    }
    /**
     * 16进制字符串转换为字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "gbk");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }
    /**
     *
     * 根据Cell类型设置数据
     *
     * @param cell
     * @return
     * @author zengwendong
     */
    private Object getCellFormatValue(Cell cell) {
        Object cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:// 如果当前Cell的Type为NUMERIC
                case Cell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式
                        // data格式是带时分秒的：2013-7-10 0:00:00
                        // cellvalue = cell.getDateCellValue().toLocaleString();
                        // data格式是不带带时分秒的：2013-7-10
                        Date date = cell.getDateCellValue();
                        cellvalue = date;
                    } else {// 如果是纯数字

                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING:// 如果当前Cell的Type为STRING
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                default:// 默认的Cell值
                    cellvalue = "";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;
    }
    @RequestMapping(value = "/getExcelData", method = RequestMethod.GET)
    public TogHttpResponse getExcelData(@RequestParam(value = "brandId", required = false, defaultValue = "0") long brandId,
                                        @RequestParam(value = "storeId", required = false, defaultValue = "0") long storeId,
                                        @RequestParam(value = "time", required = false, defaultValue = "0") long time,
                                        @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
                                        @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime, HttpServletRequest request, HttpServletResponse response) throws Exception {
        getTime(time, startTime, endTime);
        Set<Long> storeIds = new HashSet<Long>();
        Set<Long> brandIds = new HashSet<Long>();
        if (storeId > 0) {
            storeIds.add(storeId);
        } else {
            storeIds = buildAllStoreIds();
        }
        if (brandId > 0) {
            brandIds.add(brandId);
        } else {
            brandIds = buildAllBrand();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStartTime = sdf.parse(startT);
        Date dateendTime = sdf.parse(endtT);
        List<Order> orderList = queryOrderInfoList(brandId, storeId, dateStartTime, dateendTime);
        Summary summary = dataServiceApi.getDatas(brandIds, storeIds, time, startTime, endTime);
        return TogHttpResponse.SUCCESS(reportDownloadEXCECL(summary, orderList));
    }

    public FileInfo reportDownloadEXCECL(Summary summary, List<Order> orderList) throws Exception {
        // 创建excel工作簿
        FileOutputStream stream = null;
        File tempFile = null;
        Workbook wb = new HSSFWorkbook();
        try {
            getOrderInfoPay(wb, orderList);
            getSourcePay(wb, summary.getSourceList());
            getpayTypePay(wb, summary.getPayTypeList());
            if (CREATEEXCEL_NAME.length() > 5) {
                CREATEEXCEL_NAME = CREATEEXCEL_NAME.substring(0, 5);
            }
            CREATEEXCEL_NAME = CREATEEXCEL_NAME + "(" + startT + "~" + endtT + ")";

            tempFile = File.createTempFile("data", CREATEEXCEL_NAME + ".xls");
            tempFile.delete();
            tempFile.createNewFile();
            stream = FileUtils.openOutputStream(tempFile);
            wb.write(stream);

        }
        //抛出异常
        catch (Exception e) {
            e.printStackTrace();
            stream.close();
        } finally {
            stream.close();
        }
        return UploadUtils.upload(tempFile);
    }

    private void getOrderInfoPay(Workbook wb, List<Order> queryOrderInfoList) {
        Sheet sheet = wb.createSheet("门店流水数据统计");
        int rownum = 0; //定义行
        int colnum = 0; //定义列
        Row rowHead = sheet.createRow(rownum); //创建行
        CellStyle style = wb.createCellStyle(); //样式对象
        Font font = wb.createFont(); //字体对象
        rowHead.setHeight((short) 600);
        String[] heads = {"订单编号", "会员名称", "会员号", "电话", "订单内容", "开始时间", "结束时间", "所属门店", "服务技师", "收银时间", "收银人", "收银方式", "金额", "订单备注"};
        sheet.createFreezePane(0, 1, 0, 1);
        //遍历字符串数组
        for (int i = 0; i < heads.length; i++) {

            style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            //设置字体
            font.setColor(HSSFColor.BLACK.index); //HSSFColor.VIOLET.index //字体颜色
            font.setFontHeightInPoints((short) 11);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
            style.setFont(font);
            //设置背景颜色
            style.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 左右居中
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER); // 上下居中
            Cell cell = rowHead.createCell((short) i); //创建单元格
            sheet.autoSizeColumn(cell.getColumnIndex());
            cell.setCellStyle(style);
            String va = heads[i];
            cell.setCellValue(va);  //向单元格填入数据
        }
        //遍历行
        for (rownum = 0; rownum < queryOrderInfoList.size(); rownum++) {
            Row row = sheet.createRow(rownum + 1); //创建行
            //设置行高
            row.setHeight((short) 500);
            Order um = queryOrderInfoList.get(rownum);  //获取行数据
            if (um != null || um.equals("")) {
                //获取需要写入的数据
                String orderNo = um.getOrderNo();
                String userName = um.getUser().getName();
                String userNo = um.getUser().getUserNo();
                String userMobile = um.getUser().getMobile();
                String skuInfo = "";
                Date startTime = um.getStartTime();
                String starStr = "";
                String endStr = "";
                String finStr = "";
                if (startTime != null) {
                    starStr = TimeUtil.STANDARD_DATE_FORMAT.format(startTime);
                }
                Date endTime = um.getEndTime();
                if (endTime != null) {
                    endStr = TimeUtil.STANDARD_DATE_FORMAT.format(endTime);
                }
                Date finishiTime = um.getCashDate();
                if (finishiTime != null) {
                    finStr = TimeUtil.STANDARD_DATE_FORMAT.format(finishiTime);
                }
                String storeName = um.getStore().getName();
                String skuMech = "";
                Employee employee = employeeServiceApi.getEmployee(um.getCasherId());
                String finishiName = employee.getName();
                String finishiType = "";
                BigDecimal fee = new BigDecimal("0.00");
                for (SkuCom skuCom : um.getSkuComList()) {
                    skuInfo += skuCom.getAliasName() + ",";
                    if (skuCom.getMechList().size() > 0) {
                        for (Employee emp : skuCom.getMechList()) {
                            skuMech += emp.getName() + ",";
                        }
                    }
                    for (PayChildInfo payment : skuCom.getPaymentChildInfoList()) {
                        if (payment != null) {
                            if (!"".equals(payment.getPayTypeData().getName()) && payment.getPayTypeData().getName() != null) {
                                finishiType += payment.getPayTypeData().getName() + ",";
                            }
                            fee = fee.add(payment.getFee());
                        }

                    }
                }

                String desc = um.getRemark();
                Object[] value = {orderNo, userName, userNo, userMobile, skuInfo, starStr, endStr, storeName, skuMech, finStr, finishiName, finishiType, fee, desc};
                //遍历列
                for (colnum = 0; colnum < value.length; colnum++) {
                    Cell cell = row.createCell((short) colnum); //创建单元格
                    if (cell.getColumnIndex() == 0 || cell.getColumnIndex() == 4 || cell.getColumnIndex() == 8 || cell.getColumnIndex() == 13) {
                        sheet.setColumnWidth(cell.getColumnIndex(), 320 * 20);
                    } else {
                        sheet.setColumnWidth(cell.getColumnIndex(), 210 * 20);
                    }
                    //自适应
                    sheet.autoSizeColumn(cell.getColumnIndex());
                    Object val = value[colnum];
                    if (val == null) {
                        val = "";
                    }
                    cell.setCellValue(val.toString()); //写入数据
                }
            }
        }
    }

    private void getSourcePay(Workbook wb, List<Source> sourceList) {
        Sheet sheet = wb.createSheet("客户来源消费数据统计");
        int rownum = 0; //定义行
        int colnum = 0; //定义列
        Row rowHead = sheet.createRow(rownum); //创建行
        CellStyle style = wb.createCellStyle(); //样式对象
        Font font = wb.createFont(); //字体对象
        rowHead.setHeight((short) 600);
        String[] heads = {"编号", "名称", "业绩"};
        sheet.createFreezePane(0, 1, 0, 1);

        //遍历字符串数组
        for (int i = 0; i < heads.length; i++) {
            style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            //设置字体
            font.setColor(HSSFColor.BLACK.index);//HSSFColor.VIOLET.index //字体颜色
            font.setFontHeightInPoints((short) 11);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
            style.setFont(font);
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
            Cell cell = rowHead.createCell((short) i); //创建单元格
            cell.setCellStyle(style);
            String va = heads[i];
            cell.setCellValue(va);  //向单元格填入数据
        }
        //遍历行
        BigDecimal countFee = new BigDecimal("0.00");
        for (rownum = 0; rownum < sourceList.size(); rownum++) {
            Row row = sheet.createRow(rownum + 1); //创建行
            //设置行高
            row.setHeight((short) 500);
            Source um = sourceList.get(rownum);  //获取行数据
            if (um != null || um.equals("")) {
                //获取需要写入的数据
                long empId = um.getId();
                String empName = um.getName();
                BigDecimal fee = um.getFee();
                countFee = countFee.add(fee);
                Object[] value = {empId, empName, fee};
                //遍历列
                for (colnum = 0; colnum < value.length; colnum++) {
                    Cell cell = row.createCell((short) colnum); //创建单元格
                    sheet.setColumnWidth(cell.getColumnIndex(), 256 * 20);
                    Object val = value[colnum];
                    cell.setCellValue(val.toString()); //写入数据
                }
            }
        }
        Row row = sheet.createRow(rownum + 1); //创建行
        row.setHeight((short) 500);
        Object[] value = {"", "总计:", countFee};
        for (colnum = 0; colnum < value.length; colnum++) {
            Cell cell = row.createCell((short) colnum); //创建单元格
            sheet.setColumnWidth(cell.getColumnIndex(), 256 * 20);
            Object val = value[colnum];
            CellStyle styleFont = wb.createCellStyle(); //样式对象
            //设置字体
            font.setColor(HSSFColor.BLACK.index);//HSSFColor.VIOLET.index //字体颜色
            font.setFontHeightInPoints((short) 11);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
            styleFont.setFont(font);
            styleFont.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
            styleFont.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
            cell.setCellStyle(styleFont);
            cell.setCellValue(val.toString()); //写入数据
        }
    }

    private void getpayTypePay(Workbook wb, List<PayType> payTypeList) {
        Sheet sheet = wb.createSheet("支付消费数据统计");
        int rownum = 0; //定义行
        int colnum = 0; //定义列
        Row rowHead = sheet.createRow(rownum); //创建行
        CellStyle style = wb.createCellStyle(); //样式对象
        Font font = wb.createFont(); //字体对象
        rowHead.setHeight((short) 600);
        String[] heads = {"编号", "名称", "业绩"};
        sheet.createFreezePane(0, 1, 0, 1);
        //遍历字符串数组
        for (int i = 0; i < heads.length; i++) {
            style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            //设置字体
            font.setColor(HSSFColor.BLACK.index);//HSSFColor.VIOLET.index //字体颜色
            font.setFontHeightInPoints((short) 11);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
            style.setFont(font);
            //设置背景颜色
            style.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
            Cell cell = rowHead.createCell((short) i); //创建单元格
            cell.setCellStyle(style);
            String va = heads[i];
            cell.setCellValue(va);  //向单元格填入数据
        }
        //遍历行
        BigDecimal countFee = new BigDecimal("0.00");
        for (rownum = 0; rownum < payTypeList.size(); rownum++) {
            Row row = sheet.createRow(rownum + 1); //创建行
            //设置行高
            row.setHeight((short) 500);
            PayType um = payTypeList.get(rownum);  //获取行数据
            if (um != null || um.equals("")) {
                //获取需要写入的数据
                long empId = um.getId();
                String empName = um.getName();
                BigDecimal fee = um.getFee();
                countFee = countFee.add(fee);
                Object[] value = {empId, empName, fee};
                //遍历列
                for (colnum = 0; colnum < value.length; colnum++) {
                    Cell cell = row.createCell((short) colnum); //创建单元格
                    sheet.setColumnWidth(cell.getColumnIndex(), 256 * 20);
                    Object val = value[colnum];
                    cell.setCellValue(val.toString()); //写入数据
                }
            }
        }
        Row row = sheet.createRow(rownum + 1); //创建行
        row.setHeight((short) 500);
        Object[] value = {"", "总计:", countFee};
        for (colnum = 0; colnum < value.length; colnum++) {
            Cell cell = row.createCell((short) colnum); //创建单元格
            sheet.setColumnWidth(cell.getColumnIndex(), 256 * 20);
            Object val = value[colnum];
            CellStyle styleFont = wb.createCellStyle(); //样式对象
            //设置字体
            font.setColor(HSSFColor.BLACK.index);//HSSFColor.VIOLET.index //字体颜色
            font.setFontHeightInPoints((short) 11);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
            styleFont.setFont(font);
            styleFont.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
            styleFont.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
            cell.setCellStyle(styleFont);
            cell.setCellValue(val.toString()); //写入数据
        }
    }


    public List<Order> queryOrderInfoList(long brandId, long storeId, Date startTime, Date endTime) {
        PagedList<Order> pagedList = orderServiceApi.queryOrder(brandId, storeId, "", 6, startTime, endTime, 1, 10000);
        List<Order> list = pagedList.getList();
        for (Order order : list) {
            order.setStore(storeServiceApi.getStore(order.getStoreId()));
            order.setUser(userServiceApi.get(order.getUserId()));
            List<OrderSku> orderSkuList = orderSkuServiceApi.listByOrder(order.getId());
            List<SkuCom> skuComList = new ArrayList<SkuCom>();
            for (OrderSku orderSku : orderSkuList) {


                if (orderSku.getType() == OrderSku.TYPE.FORMAL) {

                    SkuCom skuCom = skuServiceApi.querySkuCom(orderSku.getSkuComId());
                    List<OrderSkuMech> orderSkuMechList = orderSkuServiceApi.queryOrderSkuMechByOrderSku(orderSku.getId());
                    List<Employee> mechList = new ArrayList<Employee>();
                    for (OrderSkuMech orderSkuMech : orderSkuMechList) {
                        Employee e = employeeServiceApi.getEmployee(orderSkuMech.getMechId());
                        mechList.add(e);
                    }
                    skuCom.setMechList(mechList);
                    skuComList.add(skuCom);
                } else if (orderSku.getType() == OrderSku.TYPE.DISCOUNT) {

                    OrderPromSku orderPromSku = promSkuServiceApi.queryOrderPromSkuById(orderSku.getSkuComId());
                    PromSku promSku = promSkuServiceApi.getPromSku(orderPromSku.getPromSkuId());
                    PromSkuDetail promSkuDetail = promSkuServiceApi.queryPromSkuDetail(orderPromSku.getDetailId());
                    SubSku subSku = skuServiceApi.getSubSku(promSku.getSkuId());


                    SkuCom skuCom = new SkuCom();
                    skuCom.setAliasName(promSku.getPromSkuName());
                    skuCom.setPrice(promSkuDetail.getPrice());
                    skuCom.setType(2);
                    List<OrderSkuMech> orderSkuMechList = orderSkuServiceApi.queryOrderSkuMechByOrderSku(orderSku.getId());
                    List<Employee> mechList = new ArrayList<Employee>();
                    for (OrderSkuMech orderSkuMech : orderSkuMechList) {
                        Employee e = employeeServiceApi.getEmployee(orderSkuMech.getMechId());
                        e.setClockType(orderSkuMech.getType());
                        mechList.add(e);
                    }

                    skuCom.setOrderSkuId(orderSku.getId());
                    skuCom.setMechList(mechList);
                    skuCom.setSubSku(subSku);
                    skuCom.setType(subSku.getType());
                    skuComList.add(skuCom);
                }

            }
            order.setSkuComList(skuComList);
            order.setEmployee(employeeServiceApi.getEmployee(order.getCasherId()));
        }
        setOrderData(list);
        return list;
    }

    private void setOrderData(List<Order> list) {
        List<PayType> payTypes = paymentServiceApi.getPayType();
        for (Order order : list) {
            order.setStore(storeServiceApi.getStore(order.getStoreId()));
            order.setUser(userServiceApi.get(order.getUserId()));
            List<OrderSku> orderSkuList = orderSkuServiceApi.listByOrder(order.getId());
            List<SkuCom> subSkuList = new ArrayList<SkuCom>();
            for (OrderSku orderSku : orderSkuList) {

                if (orderSku.getType() == OrderSku.TYPE.FORMAL) {

                    SkuCom skuCom = skuServiceApi.querySkuCom(orderSku.getSkuComId());
                    if (skuCom != null) {
                        SubSkuStore subSkuStore = skuServiceApi.querySubSkuStore(skuCom.getSkuStoreId());
                        SubSku subSku = skuServiceApi.getSubSku(subSkuStore.getSkuId());
                        skuCom.setSubSku(subSku);

                        List<Employee> mechList = getMechList(orderSku);
                        skuCom.setMechList(mechList);

                        List<PayChildInfo> payChildInfos = paymentServiceApi.queryPaymentChildInfoByOrderSku(orderSku.getId());

                        for (PayChildInfo info : payChildInfos) {
                            for (PayType payType : payTypes) {
                                if (info.getPayType() == payType.getId()) {
                                    info.setPayTypeData(payType);
                                }
                            }
                        }
                        skuCom.setPaymentChildInfoList(payChildInfos);
                        subSkuList.add(skuCom);
                    }

                } else if (orderSku.getType() == OrderSku.TYPE.DISCOUNT) { //特惠的项目

                    OrderPromSku orderPromSku = promSkuServiceApi.queryOrderPromSkuById(orderSku.getSkuComId());
                    PromSku promSku = promSkuServiceApi.getPromSku(orderPromSku.getPromSkuId());
                    PromSkuDetail promSkuDetail = promSkuServiceApi.queryPromSkuDetail(orderPromSku.getDetailId());
                    SubSku subSku = skuServiceApi.getSubSku(promSku.getSkuId());


                    SkuCom skuCom = new SkuCom();
                    skuCom.setType(OrderSku.TYPE.DISCOUNT);
                    skuCom.setAliasName(promSku.getPromSkuName());
                    skuCom.setPrice(promSkuDetail.getPrice());

                    List<OrderSkuMech> orderSkuMechList = orderSkuServiceApi.queryOrderSkuMechByOrderSku(orderSku.getId());
                    List<Employee> mechList = new ArrayList<Employee>();
                    for (OrderSkuMech orderSkuMech : orderSkuMechList) {
                        Employee e = employeeServiceApi.getEmployee(orderSkuMech.getMechId());
                        e.setClockType(orderSkuMech.getType());
                        mechList.add(e);
                    }

                    skuCom.setOrderSkuId(orderSku.getId());
                    skuCom.setMechList(mechList);
                    skuCom.setSubSku(subSku);

                    List<PayChildInfo> payChildInfos = paymentServiceApi.queryPaymentChildInfoByOrderSku(orderSku.getId());

                    for (PayChildInfo info : payChildInfos) {
                        for (PayType payType : payTypes) {
                            if (info.getPayType() == payType.getId()) {
                                info.setPayTypeData(payType);
                            }
                        }
                    }
                    skuCom.setPaymentChildInfoList(payChildInfos);

                    subSkuList.add(skuCom);

                }
            }
            order.setSkuComList(subSkuList);
            order.setPayment(paymentServiceApi.queryPaymentByOrderId(order.getId()));
            order.setEmployee(employeeServiceApi.getEmployee(order.getCasherId()));
        }
    }

    private List<Employee> getMechList(OrderSku orderSku) {
        List<OrderSkuMech> orderSkuMechList = orderSkuServiceApi.queryOrderSkuMechByOrderSku(orderSku.getId());
        List<Employee> mechList = new ArrayList<Employee>();
        for (OrderSkuMech orderSkuMech : orderSkuMechList) {
            Employee e = employeeServiceApi.getEmployee(orderSkuMech.getMechId());
            e.setClockType(orderSkuMech.getType());
            mechList.add(e);
        }
        return mechList;
    }

    private String dateTimeAddDay(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return (new SimpleDateFormat("yyyy-MM-dd")).format(cal.getTime());
    }

    private void getTime(long time, String startTime, String endTime) {
        final String tenTime = " 10-00-00";
        if (time == 1) {
            startT = getDate(1) + tenTime;
            endtT = getDate(3) + tenTime;
        }
        if (time == 2) {
            startT = getDate(2) + tenTime;
            endtT = getDate(1) + tenTime;
        }

        if (time == 3) {
            startT = getDate(6) + tenTime;
            endtT = dateTimeAddDay(getDate(7)) + tenTime;
        }

        if (time == 4) {
            startT = getDate(4) + tenTime;
            endtT = dateTimeAddDay(getDate(5)) + tenTime;
        }

        if (time == 5) {
            startT = getDate(10) + tenTime;
            endtT = dateTimeAddDay(getDate(11)) + tenTime;
        }

        if (time == 6) {
            startT = getDate(8) + tenTime;
            endtT = getDate(10) + tenTime;
        }

        if (time == 7) {
            startT = startTime + tenTime;
            endtT = endTime + tenTime;
        }
    }


    private static String getDate(int x) {
        String dateTime;
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch (x) {
            case 1:
                dateTime = sdf.format(calendar.getTime());
                break;
            case 2:
                calendar.add(5, -1);
                dateTime = sdf.format(calendar.getTime());
                break;
            case 3:
                calendar.add(5, 1);
                dateTime = sdf.format(calendar.getTime());
                break;
            case 4:
                dateTime = getWeekDate(4);
                break;
            case 5:
                dateTime = getWeekDate(5);
                break;
            case 6:
                dateTime = getWeekDate(6);
                break;
            case 7:
                dateTime = getWeekDate(7);
                break;
            case 8:
                dateTime = getUpMonth(8);
                break;
            case 9:
                dateTime = getWeekDate(9);
                break;
            case 10:
                dateTime = getMonthStart();
                break;
            case 11:
                dateTime = getMonthEnd();
                break;
            default:
                dateTime = sdf.format(calendar.getTime());
        }

        return dateTime;
    }

    private static String getWeekDate(int x) {
        String dateTime = "";
        Calendar calendar = Calendar.getInstance();
        int date = calendar.get(5);
        int n = calendar.get(7);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (n == 1) {
            n = 7;
        } else {
            --n;
        }

        switch (x) {
            case 4:
                calendar.set(5, date - 6 - n);
                dateTime = sdf.format(calendar.getTime());
                break;
            case 5:
                calendar.set(5, date - n);
                dateTime = sdf.format(calendar.getTime());
                break;
            case 6:
                calendar.set(5, date + 1 - n);
                dateTime = sdf.format(calendar.getTime());
                break;
            case 7:
                calendar.set(5, date + 7 - n);
                dateTime = sdf.format(calendar.getTime());
                break;
            default:
                calendar.set(5, date + 1);
                dateTime = sdf.format(calendar.getTime());
        }

        return dateTime;
    }

    private static String getUpMonth(int x) {
        String monthStr;
        java.util.Date nowDate = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowDate);
        cal.add(2, -1);
        if (x == 8) {
            cal.set(5, cal.getActualMinimum(5));
            System.out.println("上个月的第一天是：" + sdf.format(cal.getTime()));
            monthStr = sdf.format(cal.getTime());
        } else {
            cal.set(5, cal.getActualMaximum(5));
            System.out.println("上个月的最后一天是：" + sdf.format(cal.getTime()));
            monthStr = sdf.format(cal.getTime());
        }

        return monthStr;
    }

    private static String getMonthStart() {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int index = calendar.get(5);
        calendar.add(5, 1 - index);
        return sdf.format(calendar.getTime());
    }

    private static String getMonthEnd() {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(2, 1);
        int index = calendar.get(5);
        calendar.add(5, -index);
        return sdf.format(calendar.getTime());
    }


    private Set<Long> buildAllStoreIds() {
        Employee employee = getEmployee();
        Role role = roleServiceApi.getRoleByEmp(employee.getId());
        List<Brand> brandList = roleServiceApi.queryBrandStore(role.getId(), employee.getTraderId());
        Set<Long> set = new HashSet<Long>();
        for (Brand brand : brandList) {
            for (Store store : brand.getStoreList()) {
                set.add(store.getId());
            }
        }
        return set;
    }

    private Set<Long> buildAllBrand() {
        Employee employee = getEmployee();
        Role role = roleServiceApi.getRoleByEmp(employee.getId());
        List<Brand> brandList = roleServiceApi.queryBrandStore(role.getId(), employee.getTraderId());
        Set<Long> set = new HashSet<Long>();
        for (Brand brand : brandList) {
            set.add(brand.getId());
        }
        return set;
    }
}
