package com.julymelon.community.controller;

import com.julymelon.community.service.AlphaService;
import com.julymelon.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot!!";
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        int code = Integer.parseInt(request.getParameter("code"));
        System.out.println(code);

        // 返回响应数据
        if (code == 3) {
            response.setContentType("text/html;charset=utf-8");
            try (
                    PrintWriter writer = response.getWriter();
            ) {
                writer.write("<h1>牛客网</h1>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //    get请求
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@RequestParam(name = "current", required = false, defaultValue = "1") int current,
                              @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }

    //    post请求
//    @RequestMapping(path = "/student", method = RequestMethod.POST)
//    @ResponseBody
//    public String saveStudent(@RequestParam(name = "name", required = false, defaultValue = "hahaha") String name,
//                              @RequestParam(name = "age", required = false, defaultValue = "10") int age) {
//        System.out.println(name);
//        System.out.println(age);
//        return "success";
//    }

    @RequestMapping(path = "/student", method = RequestMethod.POST)
    public ModelAndView saveStudent(@RequestParam(name = "name", required = false, defaultValue = "hahaha") String name,
                                    @RequestParam(name = "age", required = false, defaultValue = "10") int age) {
        System.out.println(name);
        System.out.println(age);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", name);
        modelAndView.addObject("age", age);
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "张三");
        modelAndView.addObject("age", 23);
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "北京大学");
        model.addAttribute("age", 120);
        return "/demo/view";
    }


    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<String, Object>();
        emp.put("name", "张三");
        emp.put("age", 23);
        emp.put("salary", 8000.00);
        return emp;
    }


    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> emps = new ArrayList<>();
        Map<String, Object> emp = new HashMap<String, Object>();
        emp.put("name", "张三");
        emp.put("age", 23);
        emp.put("salary", 8000.00);
        emps.add(emp);

        emp = new HashMap<String, Object>();
        emp.put("name", "李四");
        emp.put("age", 24);
        emp.put("salary", 6000.00);
        emps.add(emp);

        emp = new HashMap<String, Object>();
        emp.put("name", "王五");
        emp.put("age", 25);
        emp.put("salary", 10000.00);
        emps.add(emp);

        return emps;
    }

    @RequestMapping(path = "cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {

        // 创建cookie
        // cookie只能存一个数据，且只能存字符串
        // 需要使用response对象
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置cookie生效的范围
        cookie.setPath("/community/alpha");
        // 设置cookie的生存时间
        cookie.setMaxAge(60 * 10);
        // 发送cookie
        response.addCookie(cookie);

        return "set cookie";
    }

    @RequestMapping(path = "cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {
        // request中得到的是一个cookie数组
        System.out.println(code);
        return "get cookie";
    }

    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) {
        // 底层也使用到了cookie
        // 使用cookie存sessionId，以便让浏览器和服务器中的很多session中的一个对应
        // session可以存多个数据
        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";
    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        // 黏性session、同步session、共享session
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

    // ajax示例
    // 通常页面是通过异步的方式向服务器提交一些数据
    // 异步请求不返回网页
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        // 不需要返回业务数据
        return CommunityUtil.getJSONString(0, "操作成功!");
    }

}
