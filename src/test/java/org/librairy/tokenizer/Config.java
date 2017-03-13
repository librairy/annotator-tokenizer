/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Configuration("org.librairy.tokenizer")
@ComponentScan({"org.librairy.tokenizer.annotator", "org.librairy.boot"})
@PropertySource({"classpath:boot.properties"})
public class Config {
}
