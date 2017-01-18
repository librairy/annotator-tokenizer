/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator;

import edu.stanford.nlp.pipeline.Annotation;
import org.librairy.tokenizer.annotator.stanford.StanfordEntityTokenizer;
import org.librairy.tokenizer.annotator.stanford.StanfordLemmaTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Component
public class LemmaEntityTokenizer implements Tokenizer{

    private static final Logger LOG = LoggerFactory.getLogger(LemmaEntityTokenizer.class);

    private int entityWeight =1;

    @Autowired
    StanfordLemmaTokenizer lemmaTokenizer;

    @Autowired
    StanfordEntityTokenizer entityTokenizer;


    public List<Token> tokenize(Annotation annotation){
        try {
            LOG.debug("Tokenizing text by '" + getMode() + "'");

            List<Token> entityTokens = entityTokenizer.tokenize(annotation);
            List<Token> lemmaTokens = lemmaTokenizer.tokenize(annotation);
            
           //fusion
            for (Token te :entityTokens){
            	for (int i = 0;i<entityWeight; i++){
            		lemmaTokens.add(te);
            	}
            }
            
            return lemmaTokens;

        } catch (Exception e) {
            LOG.error("Error extracting tokens from annotation: " + annotation + " ...",e);
            return new ArrayList<>();
        }
    }

    @Override
    public String getMode() {
        return "lemma_ner";
    }
}
