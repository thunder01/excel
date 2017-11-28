package com.ubi;

import com.ubi.excel.ExcelInput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 冯志宇
 * Created by 冯志宇 on 2017/11/26.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class JunitTest {
    @Autowired
    ExcelInput excelInput;
    @Value("${ExcelPath}")
    private String filepath;

    @Test
    public void insertArticle(){
        System.out.println(filepath);
        excelInput.insertArticle(filepath);
    }

}
