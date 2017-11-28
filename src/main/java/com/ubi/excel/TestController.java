package com.ubi.excel;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 冯志宇
 *         Created by 冯志宇 on 2017/11/28.
 */
@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping(value = "test",method = RequestMethod.GET)
    public String test(){
        return "恭喜发财！！";
    }

}
