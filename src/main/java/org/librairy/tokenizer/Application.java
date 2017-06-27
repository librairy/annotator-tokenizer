/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created on 21/05/16:
 *
 * @author cbadenes
 */
@SpringBootApplication
@ComponentScan({"org.librairy"})
@PropertySource({"classpath:boot.properties"})
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    static int port = 8888;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static EmbeddedServletContainerFactory getTomcatEmbeddedFactory(){
        TomcatEmbeddedServletContainerFactory servlet = new TomcatEmbeddedServletContainerFactory();
        servlet.setPort(port);
        return servlet;
    }

    public static void main(String[] args){
        try {

            if (args != null && args.length > 0){

                port = Integer.valueOf(args[0]);
            }

            ConfigurableApplicationContext context = SpringApplication.run(Application
                    .class, args);

            LOG.info(" ٩(͡๏̯͡๏)۶ librairy tokenizer is up and running!!");

        } catch (Exception e) {
            LOG.error("Error executing test",e);
            System.exit(-1);
        }

    }
}