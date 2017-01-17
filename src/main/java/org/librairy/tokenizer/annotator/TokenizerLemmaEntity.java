/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator;

import org.apache.commons.lang3.StringUtils;
import org.librairy.tokenizer.annotator.stanford.StanfordTokenizer;
import org.librairy.tokenizer.annotator.stanford.StanfordTokenizerEN;
import org.librairy.tokenizer.annotator.stanford.StanfordTokenizerES;
import org.librairy.tokenizer.annotator.stanford.StanfordTokenizerEntityEN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TokenizerLemmaEntity implements Tokenizer{

    private static final Logger LOG = LoggerFactory.getLogger(TokenizerLemmaEntity.class);

    private Map<Language, StanfordTokenizer> tokenizers;
    
    private int entityWeight =1;

    @PostConstruct
    public void setup(){

        tokenizers = new HashMap();

        tokenizers.put(Language.EN,new StanfordTokenizerEntityEN());
        //tokenizers.put(Language.ES,new StanfordTokenizerES());
    }


    public List<Token> tokenize(String text, Language language){
        try {

            List<Token> entityTokens = tokenizers.get(language).tokenize(text);
            StanfordTokenizerEN lemmaTokenizer = new StanfordTokenizerEN();
            
            List<Token> lemmaTokens = lemmaTokenizer.tokenize(text);
            
           //fusion
            for (Token te :entityTokens){
            	for (int i = 0;i<entityWeight; i++){
            		lemmaTokens.add(te);
            	}
            }
            
            return lemmaTokens;

        } catch (Exception e) {
            LOG.error("Error extracting tokens from text: " + StringUtils.substring(text,0,10) + " ...",e);
            return new ArrayList<>();
        }
    }

    @Override
    public String getMode() {
        return "lemma_ner";
    }
}
