package com.julymelon.community.controller;

import com.julymelon.community.entity.DiscussPost;
import com.julymelon.community.entity.Page;
import com.julymelon.community.entity.User;
import com.julymelon.community.service.DiscussPostService;
import com.julymelon.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        // 括号中的参数可以不用传入model中，dispatcher自动传入了
        // 方法调用之前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                int userId = discussPost.getUserId();
                User user = userService.findUserById(userId);

                Map<String, Object> map = new HashMap<>();
                map.put("user", user);
                map.put("discussPost", discussPost);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        // model.addAttribute("page", page);
        return "/index";
    }
}
