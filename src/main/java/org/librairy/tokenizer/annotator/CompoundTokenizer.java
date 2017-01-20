/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator;

import edu.stanford.nlp.pipeline.Annotation;
import org.librairy.tokenizer.annotator.stanford.StanfordCompoundTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Component
public class CompoundTokenizer implements Tokenizer{

    private static final Logger LOG = LoggerFactory.getLogger(CompoundTokenizer.class);

    @Autowired
    StanfordCompoundTokenizer tokenizer;

    public List<Token> tokenize(Annotation annotation){
        try {
            return tokenizer.tokenize(annotation);
        } catch (Exception e) {
            LOG.error("Error extracting tokens from annotation: " + annotation + " ...",e);
            return new ArrayList<>();
        }
    }

    @Override
    public String getMode() {
        return "compound";
    }
}
