package org.librairy.tokenizer.service;

import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.resources.Item;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.dao.AnnotationsDao;
import org.librairy.boot.storage.dao.ItemsDao;
import org.librairy.boot.storage.dao.ParametersDao;
import org.librairy.boot.storage.exception.DataNotFound;
import org.librairy.boot.storage.executor.ParallelExecutor;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.Tokenizer;
import org.librairy.tokenizer.annotator.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    AnnotationsDao annotationsDao;

    private ParallelExecutor executor;

    @PostConstruct
    public void setup(){
        this.executor = new ParallelExecutor();
    }


    public void handleParallel(String itemUri){
        executor.execute(() -> handle(itemUri));
    }

    public void handle(String itemUri){

        try{
            Optional<Resource> optResource = udm.read(Resource.Type.ITEM).byUri(itemUri);

            if (!optResource.isPresent()){
                LOG.warn("No ITEM found by uri:  " + itemUri);
                return;
            }

            Item item = optResource.get().asItem();

            tokenizers.parallelStream().forEach(tokenizer -> {
                Stream<String> tokens = tokenizer.tokenize(item.getContent(),Language.from(item.getLanguage()))
                        .stream()
                        .filter(token -> token.isValid())
                        .map(token -> token.getLemma())
                        ;

                LOG.info("Parsed '" + itemUri + "' to " + tokens.count() + " " + tokenizer.getMode());
                annotationsDao.saveOrUpdate(item.getUri(), tokenizer.getMode(), tokens.collect(Collectors.joining(",")));
            });


        }catch (Exception e){
            LOG.warn("Unexpected error",e);
        }
    }

}
