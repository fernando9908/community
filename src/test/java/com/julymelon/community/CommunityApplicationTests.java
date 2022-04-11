package com.julymelon.community;

import com.julymelon.community.dao.AlphaDao;
import com.julymelon.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContest() {
        System.out.println(applicationContext);
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());
        AlphaDao alphaHibernate = (AlphaDao) applicationContext.getBean("alphaHibernate");
        System.out.println(alphaHibernate.select());
    }

    @Test
    public void TestBeanManagement() {
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
        AlphaService alphaService2 = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService2);
    }

    @Test
    public void testBeanConfig() {
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) applicationContext.getBean("simpleDateFormat");
        /*主动获取的JavaBean只是先了控制反转
         * 实际开发中使用依赖注入
         * 不要自己主动获取
         * 使用注解直接注入*/
        System.out.println(simpleDateFormat);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    @Qualifier("alphaHibernate")
    private AlphaDao alphaHibernate;

    @Autowired
    private AlphaService alphaService;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Test
    public void testDI() {
        System.out.println(alphaDao);
        System.out.println(alphaDao.select());
        System.out.println(alphaHibernate);
        System.out.println(alphaHibernate.select());
        System.out.println(alphaService);
        System.out.println(simpleDateFormat);
    }
}
