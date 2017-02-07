/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator;

import edu.stanford.nlp.pipeline.Annotation;
import org.librairy.tokenizer.annotator.stanford.StanfordEntityTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntityTokenizer implements Tokenizer{

    private static final Logger LOG = LoggerFactory.getLogger(EntityTokenizer.class);

    @Autowired
    StanfordEntityTokenizer tokenizer;

    public List<Token> tokenize(Annotation annotation){
        try {
            LOG.debug("Tokenizing text by '" + getMode() + "'");
            return tokenizer.tokenize(annotation);
        } catch (Exception e) {
            LOG.error("Error extracting tokens from annotation: " + annotation + " ...",e);
            return new ArrayList<>();
        }
    }

    @Override
    public String getMode() {
        return "ner";
    }
}
