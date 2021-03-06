package org.librairy.tokenizer.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import edu.stanford.nlp.pipeline.Annotation;
import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Domain;
import org.librairy.boot.model.domain.resources.Part;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.dao.AnnotationsDao;
import org.librairy.boot.storage.dao.DomainsDao;
import org.librairy.boot.storage.dao.ParametersDao;
import org.librairy.boot.storage.dao.PartsDao;
import org.librairy.boot.storage.exception.DataNotFound;
import org.librairy.boot.storage.executor.ParallelExecutor;
import org.librairy.boot.storage.generator.URIGenerator;
import org.librairy.tokenizer.annotator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @Autowired
    PartsDao partsDao;

    @Autowired
    DomainsDao domainsDao;

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

            LOG.debug("Parsing '" + partUri + "' ...");


            String content = part.getContent();

            if (Strings.isNullOrEmpty(content)){
                LOG.info("No content found by uri: '" + partUri + "'");
                return;
            }

            Instant start = Instant.now();

            Matcher matcher = Pattern.compile(".{1,1000}(,|.$)").matcher(content);
            Map<String,StringBuilder> tokenMap = new HashMap<String,StringBuilder>();

            while (matcher.find()){

                String partialContent = matcher.group();
                LOG.debug("Annotating '" + partUri + "' ...");
                Instant startAnnotation = Instant.now();
                Annotation annotation = annotator.annotate(partialContent, Language.EN);
                Instant endAnnotation = Instant.now();
                LOG.debug("Annotated '" + partUri + "' in: " +
                        ChronoUnit.MINUTES.between(startAnnotation,endAnnotation) + "min " +
                        (ChronoUnit.SECONDS.between(startAnnotation,endAnnotation)%60) + "secs");

                tokenizers.stream().forEach(tokenizer -> {
                    try{
                        Instant startTokenizer = Instant.now();
                        List<Token> tokenList = tokenizer.tokenize(annotation);
                        Instant endTokenizer = Instant.now();
                        LOG.debug("Parsed '" + partUri + "' to " + tokenList.size() + " " + tokenizer.getMode() + " in: " +
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
                        LOG.error("Error tokenizing <" + tokenizer.getMode() + "> in " + partUri, e);
                    }
                });

            }

            // Annotate
            tokenMap.entrySet().stream().forEach(entry -> {

                org.librairy.boot.model.domain.resources.Annotation annotation = new org.librairy.boot.model.domain.resources.Annotation();
                annotation.setPurpose(entry.getKey());
                annotation.setType(entry.getKey());
                annotation.setResource(part.getUri());
                annotation.setCreator("tokenizer");
                annotation.setValue(ImmutableMap.of("content", entry.getValue().toString()));
                annotation.setScore(1.0);
                annotation.setFormat("text/plain");
                annotation.setLanguage("en");
                annotationsDao.save(annotation);
//                udm.save(annotation);
            });

            Instant end = Instant.now();
            LOG.info("Annotated '" + partUri + "'  in: " + ChronoUnit.MINUTES.between(start,end) + "min " + (ChronoUnit.SECONDS.between(start,end)%60) + "secs");

//            Integer windowSize = 100;
//            Optional<String> offset = Optional.empty();
//            Boolean finished = false;
//
//            while(!finished){
//                List<Domain> domains = partsDao.listDomains(part.getUri(), windowSize, offset);
//
//                for (Domain domain: domains){
//                    domainsDao.updateDomainTokens(domain.getUri(), part.getUri(), part.getSense());
//                }
//
//                if (domains.size() < windowSize) break;
//
//                offset = Optional.of(URIGenerator.retrieveId(domains.get(windowSize-1).getUri()));
//
//            }


        }catch (Exception e){
            LOG.error("Error on tokenizer", e);
        }
    }

}
