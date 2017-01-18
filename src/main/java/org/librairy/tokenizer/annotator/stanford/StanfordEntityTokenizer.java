/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator.stanford;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.librairy.tokenizer.annotator.Token;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by cbadenes on 07/01/16.
 */
@Component
public class StanfordEntityTokenizer {


    public List<Token> tokenize(Annotation annotation)
    {
        // List of tokens
        ConcurrentLinkedQueue<Token> tokens = new ConcurrentLinkedQueue<Token>();

        // Iterate over all of the sentences found
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        sentences.parallelStream().forEach(sentence -> {

            // Iterate over all tokens in a sentence
            String previousEntity = "";
            String previousType = "O";



            for (CoreLabel coreLabel: sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                String currentType = coreLabel.get(NamedEntityTagAnnotation.class);

                if (!currentType.equals("O")){
                    if (previousType.equals(currentType)){
                        previousEntity = previousEntity + "_" + coreLabel.get(CoreAnnotations.TextAnnotation.class);
                    }
                    else{
                        previousEntity = coreLabel.get(CoreAnnotations.TextAnnotation.class);
                        previousType = coreLabel.get(NamedEntityTagAnnotation.class);
                    }
                }
                else{
                    if (!previousType.equals("O")){
                        Token token = new Token();
                        token.setWord(previousEntity);
//                         token.setLemma(previousType);
                        token.setEntity(true);
                        tokens.add(token);

                        previousEntity = "";
                        previousType = "O";
                    }


                }
            }
            if (!previousType.equals("O")){
                Token token = new Token();
                token.setWord(previousEntity);
//                token.setLemma(previousType);
                token.setEntity(true);
                tokens.add(token);

            }
        });

        return StreamSupport.stream(tokens.spliterator(), false).collect(Collectors.toList());
    }


}
