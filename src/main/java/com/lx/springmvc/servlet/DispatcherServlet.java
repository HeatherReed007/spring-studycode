package com.lx.springmvc.servlet;

import com.lx.springmvc.annotation.Component;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Component
public class DispatcherServlet extends HttpServlet {


    private Map<String, Object> ioc = new HashMap<>();
    private List<String> list = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            outputStream.write("Hello World!".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //加载配置文件
        String location = doLoad(config);

        //扫描包
        doScan(location);
        //查看当前扫描进入list的类名
        for (String s : list) {
            System.out.println(s);
        }
        System.out.println("current size = " + list.size());
        //实例化
        doNewInstance();
    }

    //将有@Compoent注解的类装进IOC容器中
    private void doNewInstance() {
        for (String s : list) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(s);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String typeName = clazz.getTypeName();
            String key = firstToLow(typeName);
            if (clazz.isAnnotationPresent(Component.class)) {
                try {
                    Object o = clazz.newInstance();
                    ioc.put(key, o);
                    System.out.println(key + "->" + ioc.get(key));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private String firstToLow(String s){
        char[] chars = s.substring(s.lastIndexOf('.') + 1, s.length()).toCharArray();
        chars[0] += 32;
        return new String(chars);
    }

    //将全限定类名装载至list容器
    private void doScan(String location) {

        //通过URL即统一资源标识符来获取文件地址
        URL url = this.getClass().getClassLoader().getResource(location.replaceAll("\\.", "/"));
        String file1 = url.getFile();
        File file = new File(file1);
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                doScan(location + "." + f.getName());
            } else {
                String name = location + "." + f.getName();
                if (name.endsWith(".class")) {
                    list.add(name.replaceAll(".class", ""));
                }
            }
        }
    }

    //加载配置
    private String doLoad(ServletConfig config) {

        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classpath:", ""));
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty("location");
    }
}
