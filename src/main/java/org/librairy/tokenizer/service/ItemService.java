package org.librairy.tokenizer.service;

import com.google.common.base.Strings;
import edu.stanford.nlp.pipeline.Annotation;
import org.librairy.boot.model.domain.resources.Item;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.dao.AnnotationsDao;
import org.librairy.boot.storage.executor.ParallelExecutor;
import org.librairy.tokenizer.annotator.Annotator;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.Token;
import org.librairy.tokenizer.annotator.Tokenizer;
import org.librairy.tokenizer.annotator.stanford.StanfordAnnotatorEN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class ItemService {

    private static final Logger LOG = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    UDM udm;

    @Autowired
    List<Tokenizer> tokenizers;

    @Autowired
    Annotator annotator;

    @Autowired
    AnnotationsDao annotationsDao;

    @Autowired
    Worker worker;



    public void handleParallel(String itemUri){
        worker.run(() -> handle(itemUri));
    }

    public void handle(String itemUri){

        try{
            Optional<Resource> optResource = udm.read(Resource.Type.ITEM).byUri(itemUri);

            if (!optResource.isPresent()){
                LOG.warn("No ITEM found by uri:  " + itemUri);
                return;
            }

            Item item = optResource.get().asItem();

            String content = item.getContent();

            if (Strings.isNullOrEmpty(content)){
                LOG.info("No content found by uri: '" + itemUri + "'");
                return;
            }


            Instant start = Instant.now();

            Matcher matcher = Pattern.compile(".{1,1000}(,|.$)").matcher(content);
            Map<String,StringBuilder> tokenMap = new HashMap<String,StringBuilder>();
            while (matcher.find()){

                String partialContent = matcher.group();
                LOG.debug("Annotating '" + itemUri + "' ...");
                Instant startAnnotation = Instant.now();
                Annotation annotation = annotator.annotate(partialContent, Language.from(item.getLanguage()));
                Instant endAnnotation = Instant.now();
                LOG.debug("Annotated '" + itemUri + "' in: " +
                        ChronoUnit.MINUTES.between(startAnnotation,endAnnotation) + "min " +
                        (ChronoUnit.SECONDS.between(startAnnotation,endAnnotation)%60) + "secs");

                tokenizers.stream().forEach(tokenizer -> {
                    try{
                        Instant startTokenizer = Instant.now();
                        List<Token> tokenList = tokenizer.tokenize(annotation);
                        Instant endTokenizer = Instant.now();
                        LOG.debug("Parsed '" + itemUri + "' to " + tokenList.size() + " " + tokenizer.getMode() + " in: " +
                                ChronoUnit.MINUTES.between(startTokenizer,endTokenizer) + "min " + (ChronoUnit.SECONDS.between(startTokenizer,endTokenizer)%60) + "secs");
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
                        LOG.error("Error tokenizing <" + tokenizer.getMode() + "> in " + itemUri, e);
                    }
                });

            }

            // Annotate
            tokenMap.entrySet().stream().forEach(entry -> {
                annotationsDao.saveOrUpdate(item.getUri(), entry.getKey(), entry.getValue().toString());
            });


            Instant end = Instant.now();
            LOG.info("Annotated '" + itemUri + "'  in: " + ChronoUnit.MINUTES.between(start,end) + "min " + (ChronoUnit.SECONDS.between(start,end)%60) + "secs");

        }catch (Exception e){
            LOG.warn("Unexpected error",e);
        }
    }

}
