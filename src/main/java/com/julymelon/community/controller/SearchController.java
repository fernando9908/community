package com.julymelon.community.controller;

import com.julymelon.community.entity.DiscussPost;
import com.julymelon.community.entity.Page;
import com.julymelon.community.entity.SearchResult;
import com.julymelon.community.service.ElasticsearchService;
import com.julymelon.community.service.LikeService;
import com.julymelon.community.service.UserService;
import com.julymelon.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    // search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {

        //搜索帖子
        try {
            // current仍然表示的是偏移量
            SearchResult searchResult = elasticsearchService.searchDiscussPost(keyword, (page.getCurrent() - 1) * 10, page.getLimit());

            List<DiscussPost> list = searchResult.getList();

            // 聚合数据
            List<Map<String, Object>> discussPosts = new ArrayList<>();
            if (list != null) {
                for (DiscussPost post : list) {
                    Map<String, Object> map = new HashMap<>();
                    // 帖子 和 作者
                    map.put("post", post);
                    map.put("user", userService.findUserById(post.getUserId()));
                    // 点赞数目
                    map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                    discussPosts.add(map);
                }
            }
            model.addAttribute("discussPosts", discussPosts);
            model.addAttribute("keyword", keyword);
            //分页信息
            page.setPath("/search?keyword=" + keyword);
            page.setRows(searchResult.getTotal() == 0 ? 0 : (int) searchResult.getTotal());
        } catch (IOException e) {
            logger.error("系统出错，没有数据：" + e.getMessage());
        }
        return "/site/search";
    }

}
