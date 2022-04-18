package com.julymelon.community;

import com.julymelon.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Test
    public void testTextMail(){
        mailClient.sendMail("1194301673@qq.com","TEST","Hi，yrh！");
        mailClient.sendMail("18201314014@163.com","TEST","Hi，yrh！");
    }

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","yrh");
        String process = templateEngine.process("/mail/demo", context);
        System.out.println(process);
        mailClient.sendMail("1194301673@qq.com","TEST",process);
        mailClient.sendMail("18201314014@163.com","TEST",process);
    }
}
