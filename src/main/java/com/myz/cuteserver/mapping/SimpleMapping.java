package com.myz.cuteserver.mapping;

import com.myz.cuteserver.annotation.UriMapping;
import com.myz.cuteserver.annotation.UriVariable;

import java.util.Date;

/**
 * @author: zhaomingyumt
 * @date: 2023/11/24 7:17 PM
 * @description:
 */
public class SimpleMapping {

    @UriMapping("/echo")
    public String echo() {
        return "echo on";
    }

    @UriMapping("/hello")
    public String hello(@UriVariable("name") String name, @UriVariable("age") String age) {
        String date= new Date().toString();
        String out = String.format("hello name=%s age=%s date=%s.", name, age, date);
        return out;
    }
}
