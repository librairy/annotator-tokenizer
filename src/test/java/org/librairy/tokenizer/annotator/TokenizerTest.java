package org.librairy.tokenizer.annotator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.tokenizer.Config;
import org.librairy.tokenizer.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class TokenizerTest {

    private static final Logger LOG = LoggerFactory.getLogger(TokenizerTest.class);


    @Autowired
    Tokenizer tokenizer;

    @Test
    public void phrase(){

        String text  = "this is a sample for testing";
        List<Token> tokens = tokenizer.tokenize(text, Language.EN);

        LOG.info("Tokens: " + tokens);

    }

}
