package org.librairy.tokenizer.service;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.tokenizer.Application;
import org.librairy.tokenizer.Config;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.Token;
import org.librairy.tokenizer.annotator.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class ItemServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(ItemServiceTest.class);

    @Autowired
    UDM udm;

    @Autowired
    ItemService itemService;


    @Test
    public void readItems(){

        udm.find(Resource.Type.ITEM).all().parallelStream().forEach(resource -> {

            LOG.info("Trying to tokenize: " + resource);
            itemService.handle(resource);


        });
    }

}
