package org.librairy.tokenizer.service;

import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Item;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.dao.ItemsDao;
import org.librairy.boot.storage.dao.ParametersDao;
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
public class ItemService {

    private static final Logger LOG = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    UDM udm;

    @Autowired
    TokenizerFactory tokenizerFactory;

    @Autowired
    ParametersDao parametersDao;

    @Autowired
    ItemsDao itemsDao;

    @Autowired
    EventBus eventBus;

    private ParallelExecutor executor;

    @PostConstruct
    public void setup(){
        this.executor = new ParallelExecutor();
    }


    public void handleParallel(String domainUri, String itemUri){
        executor.execute(() -> handle(domainUri, itemUri));
    }

    public void handle(String domainUri,  String itemUri){

        String tokenizerMode;
        try {
            tokenizerMode = parametersDao.get(domainUri, "tokenizer.mode");
        } catch (DataNotFound dataNotFound) {
            tokenizerMode = "lemmatization";
        }

        Optional<Resource> optResource = udm.read(Resource.Type.ITEM).byUri(itemUri);

        if (!optResource.isPresent()){
            LOG.warn("No ITEM found by uri:  " + itemUri);
            return;
        }

        Item item = optResource.get().asItem();
        LOG.info("Tokenizing " + itemUri + "...");
        List<String> tokens = tokenizerFactory.of(tokenizerMode).tokenize(item.getContent(), Language.from(item.getLanguage
                ())).stream().
                filter(token -> token.isValid()).
                map(token -> token.getLemma()).
                collect(Collectors.toList());


        itemsDao.saveOrUpdateTokens(domainUri, itemUri, tokens.stream().collect(Collectors.joining(" ")));
        LOG.info(tokens.size() + " tokens in: " + item.getUri());

        // publish event
        Resource domain = new org.librairy.boot.model.domain.resources.Resource();
        domain.setUri(domainUri);
        eventBus.post(Event.from(domain), RoutingKey.of(Resource.Type.DOMAIN, Resource.State.UPDATED));
    }

}
