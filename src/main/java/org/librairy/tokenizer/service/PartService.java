package org.librairy.tokenizer.service;

import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Part;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.dao.ParametersDao;
import org.librairy.boot.storage.dao.PartsDao;
import org.librairy.boot.storage.exception.DataNotFound;
import org.librairy.boot.storage.executor.ParallelExecutor;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class PartService {

    private static final Logger LOG = LoggerFactory.getLogger(PartService.class);

    @Autowired
    UDM udm;

    @Autowired
    TokenizerFactory tokenizerFactory;

    @Autowired
    ParametersDao parametersDao;

    @Autowired
    PartsDao partsDao;

    @Autowired
    EventBus eventBus;

    private ParallelExecutor executor;

    @PostConstruct
    public void setup(){
        this.executor = new ParallelExecutor();
    }


    public void handleParallel(String domainUri, String partUri){
        executor.execute(() -> handle(domainUri, partUri));
    }

    public void handle(String domainUri,  String partUri){

        String tokenizerMode;
        try {
            tokenizerMode = parametersDao.get(domainUri, "tokenizer.mode");
        } catch (DataNotFound dataNotFound) {
            tokenizerMode = "lemmatization";
        }

        Optional<Resource> optResource = udm.read(Resource.Type.PART).byUri(partUri);

        if (!optResource.isPresent()){
            LOG.warn("No PART found by uri:  " + partUri);
            return;
        }
        try{
            Part part = optResource.get().asPart();

            LOG.info("Tokenizing " + partUri + "...");

            // TODO set language for Part
            Language language = Language.EN;
            List<String> tokens = tokenizerFactory.of(tokenizerMode).tokenize(part.getContent(), language).stream().
                    filter(token -> token.isValid()).
                    map(token -> token.getLemma()).collect(Collectors.toList());


            partsDao.saveOrUpdateTokens(domainUri, partUri,tokens.stream().collect(Collectors.joining(" ")) );
            LOG.info(tokens.size() + " tokens in " + part.getUri());

            // publish event
            Resource domain = new org.librairy.boot.model.domain.resources.Resource();
            domain.setUri(domainUri);
            eventBus.post(Event.from(domain), RoutingKey.of(Resource.Type.DOMAIN, Resource.State.UPDATED));


        }catch (Exception e){
            LOG.error("Error on tokenizer", e);
        }
    }

}
