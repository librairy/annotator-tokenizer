package org.librairy.tokenizer.annotator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class TokenizerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TokenizerFactory.class);

    @Autowired
    List<Tokenizer> tokenizers;

    public Tokenizer of(String mode){
        for (Tokenizer tokenizer : tokenizers){
            if (tokenizer.getMode().equalsIgnoreCase(mode)) return tokenizer;
        }
        Tokenizer tokenizer = tokenizers.get(0);
        LOG.warn("Tokenizer not found by mode '" + mode + "'. Using '"+tokenizer.getMode()+"' tokenizer");
        return tokenizer;
    }


}
