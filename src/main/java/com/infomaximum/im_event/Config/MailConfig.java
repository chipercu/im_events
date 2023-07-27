package com.infomaximum.im_event.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Created by a.kiperku
 * Date: 27.07.2023
 */

@Configuration
public class MailConfig {


    @Bean
    public JavaMailSender getJavaMailSender()
    {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.mail.ru");
        mailSender.setPort(25);

        mailSender.setUsername("chipercualexandru@mail.ru");
        mailSender.setPassword("ggpSv0eHHXSDcsj0aAxL");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.ssl.protocols","TLSv1.2");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.mail.ru");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public SimpleMailMessage emailTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("chipercualexandru@mail.ru");
        message.setFrom("chipercualexandru@mail.ru");
        message.setText("FATAL - Application crash. Save your job !!");
        return message;
    }



}
