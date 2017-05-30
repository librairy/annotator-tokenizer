package org.librairy.tokenizer.annotator;

import com.google.common.collect.ImmutableMap;
import com.sun.jndi.toolkit.url.Uri;
import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.boot.model.Annotation;
import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Item;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.dao.AnnotationsDao;
import org.librairy.boot.storage.dao.DomainsDao;
import org.librairy.boot.storage.dao.ItemsDao;
import org.librairy.boot.storage.exception.DataNotFound;
import org.librairy.boot.storage.generator.URIGenerator;
import org.librairy.tokenizer.Application;
import org.librairy.tokenizer.service.ItemService;
import org.librairy.tokenizer.service.PartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource(properties = {
//        "librairy.columndb.host = zavijava.dia.fi.upm.es",
//        "librairy.documentdb.host = zavijava.dia.fi.upm.es",
//        "librairy.graphdb.host = zavijava.dia.fi.upm.es",
//        "librairy.eventbus.host = zavijava.dia.fi.upm.es"
        "librairy.uri = librairy.linkeddata.es/resources" //librairy.org
})
public class TagAnnotatorTest {

    private static final Logger LOG = LoggerFactory.getLogger(TagAnnotatorTest.class);

    @Autowired
    TagAnnotator annotator;

    @Autowired
    ItemsDao itemsDao;

    @Autowired
    DomainsDao domainsDao;

    @Autowired
    AnnotationsDao annotationsDao;

    @Autowired
    URIGenerator uriGenerator;

    @Test
    public void annotate(){

        StringBuilder ner = new StringBuilder("    Federal_Aviation_Administration FAA FAA Joint_Commission_on_Accreditation_of_Hospitals 15_% Pat Chapin Pat Sandifer Pat Nguyen Pat Sandifer                            Federal_Aviation_Administration Federal_Aviation_Administration        Federal_Aviation_Administration    FAA     IFR             Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration  Federal_Aviation_Administration Federal_Aviation_Administration Federal_Aviation_Administration");
        StringBuilder compound = new StringBuilder(" client_system client_system     log_books  Chapin_Jr. U.S._Pat U.S._Pat et_al. U.S._Pat   web_server web_server     storage_facility remote_storage_facility       FIG._displays log_page log_page entry_page log_entry_page client_database client_database client_system client_system  _1     web_pages web_pages   network_server network_server  _20 New_records maintenance_work completed_maintenance_work  server_system system_   _20  501A_   _501A_  FIG._1B post_  post_  button_  post_button_   _  button_ _   Internet_sites Internet_sites  user_  new_user_  link_  user_link_  URL_  URL_  header_  URL_header_    FIG._b. FIG._b. security_  security_  information_  security_information_   information_  information_   box_  alert_box_  time_  information_  time_information_      database_entries various_database_entries mod_column The_mod_column view_  view_  button_  view_button_  507A_   _507A_   header_  The_header_   _  header_ _  FIG._7A identification_  706_  entry_  709_  information_            ");



        Map map = ImmutableMap.of("compound", compound, "ner", ner);

        String result = annotator.annotate(map, Language.EN);

        LOG.info("Tags: " + result);
    }

    @Test
    public void updateTags() throws DataNotFound {

        String domainUri = "http://librairy.linkeddata.es/resources/domains/patents";

        Optional<String> id = Optional.empty();
        Boolean finished = false;
        Integer windowSize = 500;
        Integer round = 1;
        AtomicInteger counter = new AtomicInteger(0);
        while(!finished){

            LOG.info(round++ + " round!  total docs: " + counter.get());
            List<Item> items = domainsDao.listDocuments(domainUri, windowSize, id);

            items.parallelStream().forEach( item -> {
                counter.incrementAndGet();
                try {
                    List<Annotation> annotations = annotationsDao.list(item.getUri(), false);

                    if (annotations.stream().filter(a -> a.getType().equalsIgnoreCase("tags")).count() < 1){
                        LOG.info("Annotating item: " + item.getUri());
                        Annotation ner = null;
                        ner = annotationsDao.get(item.getUri(), "ner");
                        Annotation compound = annotationsDao.get(item.getUri(), "compound");
                        Map map = ImmutableMap.of("compound", new StringBuilder(compound.getValue()), "ner", new StringBuilder(ner.getValue()));

                        // Annotate Tags
                        annotationsDao.saveOrUpdate(item.getUri(), "tags", annotator.annotate(map, Language.from(item.getLanguage())));
                    }

                } catch (DataNotFound dataNotFound) {
                    dataNotFound.printStackTrace();
                }

            });

            if (items.size() < windowSize) {
                LOG.info("Completed!");
                return;
            }

            String offset = URIGenerator.retrieveId(items.get(items.size()-1).getUri());
            LOG.info("New offset: " + offset);
            id = Optional.of(offset);

        }

    }

    @Test
    public void getAnnotations(){

        String uri = "http://librairy.linkeddata.es/resources/items/US7162549";
        List<Annotation> annotations = annotationsDao.list(uri, false);
        System.out.println(annotations);
        Boolean res = annotations.stream().filter(a -> a.getType().equalsIgnoreCase("tags")).count() < 1;
        System.out.println("Boolean: " + res);

    }

}
