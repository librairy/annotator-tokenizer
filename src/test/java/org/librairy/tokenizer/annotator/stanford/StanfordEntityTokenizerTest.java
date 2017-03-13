package org.librairy.tokenizer.annotator.stanford;


import edu.stanford.nlp.pipeline.Annotation;
import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.boot.model.domain.resources.Item;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.dao.AnnotationsDao;
import org.librairy.tokenizer.Config;
import org.librairy.tokenizer.annotator.Annotator;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.Token;
import org.librairy.tokenizer.annotator.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class StanfordEntityTokenizerTest {

    private static final Logger LOG = LoggerFactory.getLogger(StanfordEntityTokenizerTest.class);


    @Autowired
    Annotator annotator;

    @Autowired
    StanfordEntityTokenizer entityTokenizer;

    @Autowired
    UDM udm;

    @Autowired
    AnnotationsDao annotationsDao;

    @Test
    public void content() throws IOException {

        String uri = "http://librairy.org/items/OP7dGJ-cvNfVJ";

        String type = "ner";

        Optional<Resource> optResource = udm.read(Resource.Type.ITEM).byUri(uri);

        if (!optResource.isPresent()){
            LOG.warn("No ITEM found by uri:  " + uri);
            return;
        }

        Item item = optResource.get().asItem();

        String content = item.getContent();

        Instant startAnnotation = Instant.now();


        List<Token> finalTokens = new ArrayList<>();

        Matcher matcher = Pattern.compile(".{1,1000}(,|.$)").matcher(content);
        List<String> list = new ArrayList<>();
        while (matcher.find()){

            String partialContent = matcher.group();

            Annotation annotation = annotator.annotate(partialContent, Language.EN);

            finalTokens.addAll(entityTokenizer.tokenize(annotation));


        }

        finalTokens.forEach(t -> LOG.info("Token: " + t.getWord()));


        try{

            String tokens = finalTokens
                    .stream()
                    .filter(token -> token.isValid())
                    .map(token -> token.getWord())
                    .collect(Collectors.joining(" "))
                    ;

            LOG.info(tokens);

            annotationsDao.saveOrUpdate(uri,type,tokens);
            LOG.info("Saved successfully!");

            Instant endAnnotation = Instant.now();
            LOG.info("Annotated '" + uri + "' in: " +
                    ChronoUnit.MINUTES.between(startAnnotation,endAnnotation) + "min " +
                    (ChronoUnit.SECONDS.between(startAnnotation,endAnnotation)%60) + "secs");

        }catch (Exception e){
            LOG.error("Error saving content", e);
        }

    }


}
