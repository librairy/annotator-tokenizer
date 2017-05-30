package org.librairy.tokenizer.service;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.UDM;
import org.librairy.tokenizer.Application;
import org.librairy.tokenizer.Config;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.Token;
import org.librairy.tokenizer.annotator.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
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

    @Autowired
    PartService partService;

    @Autowired
    EventBus eventBus;

    @Test
    public void tokenizeItemsAndParts(){

        udm.find(Resource.Type.ITEM).all().stream().forEach(resource -> {

            LOG.info("Trying to tokenize: " + resource);
//            itemService.handle(resource);

            eventBus.post(Event.from(resource), RoutingKey.of(Resource.Type.ITEM, Resource.State.CREATED));


        });

        udm.find(Resource.Type.PART).all().stream().forEach(resource -> {

            LOG.info("Trying to tokenize: " + resource);
//            partService.handle(resource);
            eventBus.post(Event.from(resource), RoutingKey.of(Resource.Type.PART, Resource.State.CREATED));


        });
    }

    @Test
    public void sample(){

        String uri = "http://librairy.linkeddata.es/resources/items/9_d3ELrTNPCbp";

        itemService.handle(uri);

    }

}
