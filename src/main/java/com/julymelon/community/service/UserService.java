package com.julymelon.community.service;

import ch.qos.logback.core.net.server.Client;
import com.julymelon.community.dao.LoginTicketMapper;
import com.julymelon.community.dao.UserMapper;
import com.julymelon.community.entity.LoginTicket;
import com.julymelon.community.entity.User;
import com.julymelon.community.util.CommunityConstant;
import com.julymelon.community.util.CommunityUtil;
import com.julymelon.community.util.MailClient;
import com.julymelon.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.jws.soap.SOAPBinding;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    /*
    使用redis，构造缓存空间进行重构
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }
    */

    public User findUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    /*
    redis优化
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    */

    public LoginTicket findLoginTicket(String ticket) {
        // return loginTicketMapper.selectByTicket(ticket);

        // redis优化
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public Map<String, Object> register(User user) {

        HashMap<String, Object> map = new HashMap<String, Object>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        // 待激活
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        // string的format方法
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 这样thymeleaf中就不用使用@符号，而使用$符号
        // 使用a@符号会自动补全
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;

    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        /*
        // 存到数据库的作用类似于session
        loginTicketMapper.insertLoginTicket(loginTicket);
        */

        // 优化：存到redis中
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 按string格式进行存储，redis会自动将loginTicket转化为JSON字符串
        // 不用设置过期时间
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        // loginTicketMapper.updateStatus(ticket, 1);

        // 使用redis优化
        // 先取出来，再存回去
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        // 需要强转
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);

    }

    public int updateHeader(int userId, String headerUrl) {
        // 使用redis优化
        // return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        // 由于redis事务和mysql事务不能放在一起，因此先处理mysql，成功之后再清除缓存
        clearCache(userId);
        return rows;
    }

    // 课后作业
    // 修改密码
    public Map<String, Object> updatePassword(int userId, String oldPwd, String newPwd) {

        HashMap<String, Object> map = new HashMap<String, Object>();

        // 空值处理
        if (StringUtils.isBlank(oldPwd)) {
            map.put("oldPwdMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPwd)) {
            map.put("newPwdMsg", "新密码不能为空!");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPwd = CommunityUtil.md5(oldPwd + user.getSalt());
        if (!user.getPassword().equals(oldPwd)) {
            map.put("oldPwdMsg", "密码输入错误");
            return map;
        }

        // 更新密码
        userMapper.updatePassword(userId, CommunityUtil.md5(newPwd + user.getSalt()));
        clearCache(userId);

        return map;
    }

    // 重置密码
    public Map<String, Object> resetPassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱尚未注册!");
            return map;
        }

        // 重置密码
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);
        clearCache(user.getId());

        map.put("user", user);
        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    // 将用户另外在redis开辟一个缓存空间，一定时间后清除
    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    // 获取用户权限
    // 绕过security的认证，使用自定义方法
    // 目的是将结果存入context供security授权
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        // 本项目中一个用户只有一个权限
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
