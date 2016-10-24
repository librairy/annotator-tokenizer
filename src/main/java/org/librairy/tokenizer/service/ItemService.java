package org.librairy.tokenizer.service;

import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.executor.ParallelExecutor;
import org.librairy.storage.generator.URIGenerator;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
    Tokenizer tokenizer;

    private ParallelExecutor executor;

    @PostConstruct
    public void setup(){
        this.executor = new ParallelExecutor();
    }


    public void handleParallel(Resource resource){
        executor.execute(() -> handle(resource));
    }

    public void handle(Resource resource){

        Optional<Resource> optResource = udm.read(Resource.Type.ITEM).byUri(resource.getUri());

        if (!optResource.isPresent()){
            LOG.warn("No ITEM found by uri:  " + resource.getUri());
            return;
        }

        Item item = optResource.get().asItem();

        String tokens = tokenizer.tokenize(item.getContent(), Language.from(item.getLanguage())).stream().
                filter(token -> token.isValid()).
                map(token -> token.getLemma()).
                collect(Collectors.joining(" "));
        item.setTokens(tokens);
        udm.update(item);
        LOG.info("Item " + item.getUri() +  " tokenized");
    }

}
