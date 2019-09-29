package com.mihone.redismq.reflect;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class RedisMQ {

    private RedisMQ(){}
    public static <T> void start(Class<T> clazz){


           URL url=  clazz.getClass().getClassLoader().getResource("com");
            System.out.println(url);




    }
}
