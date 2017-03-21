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
import org.librairy.tokenizer.annotator.CompoundTokenizer;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class StanfordCompoundTokenizerTest {

    private static final Logger LOG = LoggerFactory.getLogger(StanfordCompoundTokenizerTest.class);


    @Autowired
    Annotator annotator;

    @Autowired
    StanfordEntityTokenizer entityTokenizer;

    @Autowired
    UDM udm;

    @Autowired
    AnnotationsDao annotationsDao;

    @Autowired
    CompoundTokenizer tokenizer;

    @Test
    public void getTokens() throws IOException {

        String uri = "http://librairy.org/items/OP7dGJ-cvNfVJ";

        String type = "compound";

        Optional<Resource> optResource = udm.read(Resource.Type.ITEM).byUri(uri);

        if (!optResource.isPresent()){
            LOG.warn("No ITEM found by uri:  " + uri);
            return;
        }

        Item item = optResource.get().asItem();

        String content = item.getContent();

        Matcher matcher = Pattern.compile(".{1,5000}(,|.$)").matcher(content);
        Map<String,StringBuilder> tokenMap = new HashMap<String,StringBuilder>();
        while (matcher.find()){

            String partialContent = matcher.group();
            Annotation annotation = annotator.annotate(partialContent, Language.from(item.getLanguage()));

            try{
                Instant startTokenizer = Instant.now();
                List<Token> tokenList = tokenizer.tokenize(annotation);
                Instant endTokenizer = Instant.now();
                String tokens = tokenList
                        .stream()
                        .filter(token -> token.isValid())
                        .map(token -> token.getWord())
                        .collect(Collectors.joining(" "))
                        ;

                StringBuilder accList = tokenMap.get(tokenizer.getMode());
                if (accList == null){
                    accList = new StringBuilder();
                    tokenMap.put(tokenizer.getMode(), accList);
                }
                accList.append(" ").append(tokens);
                tokenMap.put(tokenizer.getMode(), accList);

            }catch (Exception e){
                LOG.error("Error tokenizing <" + tokenizer.getMode() + "> in " + uri, e);
            }

        }

        Map<String, List<String>> tokens = Arrays.stream(tokenMap.get(tokenizer.getMode()).toString().split(" ")).collect(Collectors.groupingBy(String::toString));

        for (Map.Entry<String,List<String>> entry: tokens.entrySet()){
            LOG.info("Token: " + entry.getKey() + " - " + entry.getValue().size());
        }


    }


}
