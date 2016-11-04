package org.librairy.tokenizer.service;

import org.librairy.model.domain.resources.Part;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.executor.ParallelExecutor;
import org.librairy.tokenizer.annotator.Language;
import org.librairy.tokenizer.annotator.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

        Optional<Resource> optResource = udm.read(Resource.Type.PART).byUri(resource.getUri());

        if (!optResource.isPresent()){
            LOG.warn("No PART found by uri:  " + resource.getUri());
            return;
        }


        Part part = optResource.get().asPart();

        // TODO set language for Part
        Language language = Language.EN;
        Stream<String> tokens = tokenizer.tokenize(part.getContent(), language).stream().
                filter(token -> token.isValid()).
                map(token -> token.getLemma());
        part.setTokens(tokens.collect(Collectors.joining(" ")));
        LOG.info(tokens.count() + " tokens in " + part.getUri());
        udm.update(part);
    }

}
