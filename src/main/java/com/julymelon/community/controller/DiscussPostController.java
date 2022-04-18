package com.julymelon.community.controller;

import com.julymelon.community.dao.DiscussPostMapper;
import com.julymelon.community.entity.Comment;
import com.julymelon.community.entity.DiscussPost;
import com.julymelon.community.entity.Page;
import com.julymelon.community.entity.User;
import com.julymelon.community.service.CommentService;
import com.julymelon.community.service.DiscussPostService;
import com.julymelon.community.service.UserService;
import com.julymelon.community.util.CommunityConstant;
import com.julymelon.community.util.CommunityUtil;
import com.julymelon.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 评论
        page.setLimit(5);
        page.setRows(post.getCommentCount());
        page.setPath("/discuss/detail/" + discussPostId);

        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, discussPostId, page.getOffset(), page.getLimit());
        // 由于需要更多信息，将每一条comment转存为map
        List<Map<String, Object>> comments = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> map = new HashMap<>();

                // 评论的作者
                int userId = comment.getUserId();
                User u = userService.findUserById(userId);
                map.put("user", u);

                // 评论的实体
                map.put("comment", comment);

                // 评论的回复
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replys = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyMap = new HashMap<>();
                        // 回复的作者
                        replyMap.put("user", userService.findUserById(reply.getUserId()));
                        // 回复的实体
                        replyMap.put("reply", reply);
                        // 回复的指向人：有可能有，有可能没有
                        // User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        // 也可用下面的代替，查不到指定的id就返回null
                        User target = userService.findUserById(reply.getTargetId());
                        replyMap.put("target", target);

                        replys.add(replyMap);
                    }
                }
                map.put("replys",replys);

                // 回复数量，评论的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                map.put("replyCount", replyCount);

                comments.add(map);
            }
        }
        model.addAttribute("comments",comments);

        return "/site/discuss-detail";
    }

}
