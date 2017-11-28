package com.ubi.excel;

import com.qingstor.sdk.config.EvnContext;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.service.Bucket;
import com.qingstor.sdk.service.QingStor;
import com.ubi.mapper.ArticleFileMapper;
import com.ubi.mapper.ArticleMapper;
import com.ubi.mapper.ArticleTagMapper;
import com.ubi.model.Article;
import com.ubi.model.ArticleTag;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 冯志宇
 * Created by 冯志宇 on 2017/11/25.
 */
@Service
public class ExcelInput {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private Environment env;
    @Autowired
    private ArticleFileMapper articleFileMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Value("${FilePath}")
    private String basePath;
    private Workbook wb;
    private Sheet sheet;
    private Row row;

    public ExcelInput() {
    }

    public ExcelInput(String filepath) throws IOException {
        if(filepath==null){
            return;
        }
        String ext = filepath.substring(filepath.lastIndexOf("."));
        try {
            InputStream is = new FileInputStream(filepath);
            if(".xls".equals(ext)){
                wb = new HSSFWorkbook(is);
            }else if(".xlsx".equals(ext)){
                wb = new XSSFWorkbook(is);
            }else{
                wb=null;
            }
        } catch (FileNotFoundException e) {
           e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] readExcelTitle() throws Exception{
        if(wb==null){
            throw new Exception("Workbook对象为空！");
        }
        sheet = wb.getSheetAt(0);
        row = sheet.getRow(0);
        // 标题总列数
        int colNum = row.getPhysicalNumberOfCells();
        System.out.println("colNum:" + colNum);
        String[] title = new String[colNum];
        for (int i = 0; i < colNum; i++) {
            title[i] = row.getCell(i).getStringCellValue();
        }
        return title;
    }

    public Map<Integer, Map<Integer,Object>> readExcelContent() throws Exception{
        if(wb==null){
            throw new Exception("Workbook对象为空！");
        }
        Map<Integer, Map<Integer,Object>> content = new HashMap<>();

        //第一个工作簿
        sheet = wb.getSheetAt(0);
        int rowNum = sheet.getLastRowNum();
        row = sheet.getRow(0);
        int colNum = row.getPhysicalNumberOfCells();
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= rowNum; i++) {
            row = sheet.getRow(i);
            int j = 0;
            Map<Integer,Object> cellValue = new HashMap<>();
            while (j < colNum) {
                Object obj = getCellFormatValue(row.getCell(j));
                cellValue.put(j, obj);
                j++;
            }
            content.put(i, cellValue);
        }
        return content;
    }

    private Object getCellFormatValue(Cell cell) {
        Object cellvalue = "";
        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                case Cell.CELL_TYPE_FORMULA: {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);;
                    } else {
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING:
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                default:
                    cellvalue = "";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;
    }

    /**政策类型*/
    public enum articletype{
        国家政策(1),地方政策(2),补贴政策(3);

        private int i;
        private articletype(int i){
            this.i=i;
        }
        @Override
        public String toString() {
            return this.i+"";
        }
    }

    /**项目类型*/
    public enum projecttype{
        天然气分布式能源(1),分布式光伏(2),供热工程(3),燃气工程(4),其它(6);

        private int i;
        private projecttype(int i){
            this.i=i;
        }
        @Override
        public String toString() {
            return this.i+"";
        }
    }

    /**查询事项*/
    public enum searchType{
        备案核准(0),规划许可(1),施工许可(2),土地使用证(3),电力接入(4);

        private int i;
        private searchType(int i){
            this.i=i;
        }
        @Override
        public String toString() {
            return this.i+"";
        }
    }

    /**
     * 上传文件
     * @param fileName
     * @return
     * @throws FileNotFoundException
     * @throws QSException
     */
    public String fileupload(String fileName) throws FileNotFoundException, QSException {
        // 定义常量后获取连接
        EvnContext evn = new EvnContext(env.getProperty("file.qingstore.accessKeyId"),
                env.getProperty("file.qingstore.secretAccessKey"));
        // 根据连接创建区域
        QingStor storService = new QingStor(evn, "LFRZ1");
        // 通过区域拿到需要的桶
        Bucket bucket = storService.getBucket(env.getProperty("file.qingstore.bucketName"), "LFRZ1");
        // 通过桶拿到输入流
        Bucket.PutObjectInput input = new Bucket.PutObjectInput();

        /**上传文件*/
        InputStream inputStream=null;
        try {
            String filePath=basePath+"\\"+fileName;
            System.out.println(filePath);
            File file=new File(filePath);
            inputStream=new FileInputStream(file);
            input.setContentLength(file.length());
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }

        // 通过流写文件,设置数据流
        input.setBodyInputStream(inputStream);

        // 将流中的数据放入桶,向文件服务器发送请求
        String prefix=fileName.substring(fileName.lastIndexOf(".")+1);
        String remotFileName=Misc.base58Uuid() +"."+prefix;
        Bucket.PutObjectOutput putObjectOutput = bucket.putObject(remotFileName, input);

        return remotFileName;
    }

    /**
     * 导入excel数据
     * @param filepath
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertArticle(String filepath) {
        try {
            ExcelInput excelReader = new ExcelInput(filepath);
            // 读取Excel表格内容
            Map<Integer, Map<Integer,Object>> map = excelReader.readExcelContent();
            System.out.println("获得Excel表格的内容:");
            int count=0;
            for (int i = 1; i <= map.size(); i++) {
                if (map.get(i).get(0)==null||"".equals(map.get(i).get(0))){
                    break;
                }
                Map<Integer,Object> objectMap=map.get(i);
                /**根据标题查询该政策是否已经存在*/
                System.out.println("正在查询该政策是否存在");
                Long articleid=articleMapper.getArticleId(objectMap.get(0).toString());
                if (articleid!=null){
                    System.out.println(objectMap.get(0).toString()+" 已经存在");
                    continue;
                }

                /**政策信息导入*/
                System.out.println("导入政策信息...");
                Article article=new Article();
                article.setTitile(objectMap.get(0).toString());
                System.out.println(new SimpleDateFormat("yyyy-MM-dd").parse(objectMap.get(1).toString()));
                article.setDeploytime(new SimpleDateFormat("yyyy-MM-dd").parse(objectMap.get(1).toString()));
                article.setLabel(objectMap.get(2).toString());
                article.setArticletype(new Byte(articletype.valueOf(objectMap.get(3).toString()).toString()));
                article.setProjecttype(new Byte(projecttype.valueOf(objectMap.get(4).toString().replaceAll("/","")).toString()));
                article.setProvinceid(articleMapper.getProvinceId(objectMap.get(6).toString()));
                article.setSrouce(objectMap.get(7).toString());
                article.setSourceurl(objectMap.get(8).toString());
                article.setValidtime(new SimpleDateFormat("yyyy-MM-dd").parse("2020-11-11"));
                int insert = articleMapper.insert(article);

                /**文件导入*/
                String fileName=objectMap.get(9).toString();
                System.out.println("文件上传...");
                String remotFileName=fileupload(fileName);
                String filePath="/articlefile/download?fileid="+remotFileName;
                System.out.println("导入文件信息...");
                articleFileMapper.addFile(article.getArticleid(),remotFileName,fileName,filePath);

                /**标签导入*/
                ArticleTag articleTag=new ArticleTag();
                System.out.println("政策ID："+article.getArticleid());
                articleTag.setArticleid(article.getArticleid());
                //查询事项的值
                String dicvalue = articleMapper.getDicvalue(objectMap.get(5).toString(), objectMap.get(4).toString());
                System.out.println("查询事项的值："+dicvalue);
                if (dicvalue!=null){
                    articleTag.setArticletagid(new Byte(dicvalue));
                    System.out.println("导入标签信息...");
                    articleTagMapper.insert(articleTag);
                }
                count++;
                System.out.println(objectMap.get(i));
                System.out.println("已插入 "+count+" 条");
            }
            System.out.println("万德福,导入完成");
        } catch (FileNotFoundException e) {
            System.out.println("未找到指定路径的文件!");
            e.printStackTrace();
        }catch (Exception e) {
            System.out.println("导入失败");
            e.printStackTrace();
        }
    }
}
