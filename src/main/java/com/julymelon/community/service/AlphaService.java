package com.julymelon.community.service;

import com.julymelon.community.dao.AlphaDao;
import com.julymelon.community.dao.DiscussPostMapper;
import com.julymelon.community.dao.UserMapper;
import com.julymelon.community.entity.DiscussPost;
import com.julymelon.community.entity.User;
import com.julymelon.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService，在构造器方法之后调用");
    }

    public AlphaService() {
        System.out.println("实例化AlphaService，即调用构造器方法");
    }

    @PreDestroy
    public void destory(){
        System.out.println("销毁AlphaService");
    }

    public String find(){
        return alphaDao.select();
    }

    // 一个业务如果包含多个数据库的处理操作，那么就需要事务管理。即一个业务就是一个事务。
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 事务的传播机制：解决事务之间交叉的问题
    // REQUIRED: 支持当前事务(外部事务),如果不存在则创建新事务.
    // REQUIRES_NEW: 创建一个新事务,并且暂停当前事务(外部事务).
    // NESTED: 如果当前存在事务(外部事务),则嵌套在该事务中执行(独立的提交和回滚),否则就会REQUIRED一样.
    // 隔离级别四个
    // 发生的错误有两类五个，更新类2个，第一类和第二类丢失更新，读取类3个，脏读、幻读、不可重复读
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道!");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        // 人为报错
        // 添加事务之后会回滚,即加注解或别的方式，否则上面的数据会被添加
        // 添加事务的实质就是让一段代码成为整体
        Integer.valueOf("abc");

        return "ok";
    }

    @Autowired
    private TransactionTemplate transactionTemplate;

    public Object save2() {
        // 和注解中声明的一样
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("我是新人!");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("abc");

                return "ok";
            }
        });
    }

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    // 加注解：让该方法在多线程环境下,被异步的调用.
    @Async
    public void execute1() {
        logger.debug("execute1");
    }

    // 注掉，不然后面运行程序时都会执行
    /*@Scheduled(initialDelay = 10000, fixedRate = 1000)*/
    public void execute2() {
        logger.debug("execute2");
    }

}
