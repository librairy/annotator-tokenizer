package org.librairy.tokenizer.service;

import com.google.common.base.Strings;
import edu.stanford.nlp.pipeline.Annotation;
import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Part;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.dao.AnnotationsDao;
import org.librairy.boot.storage.dao.ParametersDao;
import org.librairy.boot.storage.dao.PartsDao;
import org.librairy.boot.storage.exception.DataNotFound;
import org.librairy.boot.storage.executor.ParallelExecutor;
import org.librairy.tokenizer.annotator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class PartService {

    private static final Logger LOG = LoggerFactory.getLogger(PartService.class);

    @Autowired
    UDM udm;

    @Autowired
    List<Tokenizer> tokenizers;

    @Autowired
    AnnotationsDao annotationsDao;

    @Autowired
    Annotator annotator;

    @Autowired
    Worker worker;

    public void handleParallel(String partUri){
        worker.run(() -> handle(partUri));
    }

    public void handle(String partUri){

        Optional<Resource> optResource = udm.read(Resource.Type.PART).byUri(partUri);

        if (!optResource.isPresent()){
            LOG.warn("No PART found by uri:  " + partUri);
            return;
        }
        try{
            Part part = optResource.get().asPart();

            LOG.info("Parsing '" + partUri + "' ...");


            String content = part.getContent();

            if (Strings.isNullOrEmpty(content)){
                LOG.info("No content found by uri: '" + partUri + "'");
                return;
            }

            //TODO handle language in PARTs
            Instant startAnnotation = Instant.now();
            Annotation annotation = annotator.annotate(content, Language.EN);
            Instant endAnnotation = Instant.now();
            LOG.info("Annotated '" + partUri + "' in: " +
                    ChronoUnit.MINUTES.between(startAnnotation,endAnnotation) + "min " +
                    (ChronoUnit.SECONDS.between(startAnnotation,endAnnotation)%60) + "secs");

            tokenizers.stream().forEach(tokenizer -> {
               try{
                   Instant start = Instant.now();
                   List<Token> tokenList = tokenizer.tokenize(annotation);
                   Instant end = Instant.now();
                   String tokens = tokenList
                            .stream()
                            .filter(token -> token.isValid())
                            .map(token -> token.getWord())
                            .collect(Collectors.joining(" "))
                            ;

                   LOG.info("Parsed '" + partUri + "' to " + tokenList.size() + " " + tokenizer.getMode() + " in: " +
                           ChronoUnit.MINUTES.between(start,end) + "min " + (ChronoUnit.SECONDS.between(start,end)%60) + "secs");
                    annotationsDao.saveOrUpdate(part.getUri(), tokenizer.getMode(), tokens);
               }catch (Exception e){
                LOG.error("Error tokenizing <" + tokenizer.getMode() + "> in " + partUri, e);
                }
            });

        }catch (Exception e){
            LOG.error("Error on tokenizer", e);
        }
    }

}
