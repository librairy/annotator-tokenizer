/*
x * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator.stanford;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import org.librairy.tokenizer.annotator.Token;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cbadenes on 07/01/16.
 */
@Component
public class StanfordLemmaTokenizer{




    public List<Token> tokenize(Annotation annotation)
    {
        // Iterate over all of the sentences found
        return annotation.get(CoreAnnotations.SentencesAnnotation.class)
                .parallelStream()
                .flatMap(sentence -> sentence.get(CoreAnnotations.TokensAnnotation.class).stream())
                .map(coreLabel -> {
                    Token token = new Token();
                    token.setPos(coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class).toLowerCase());
                    token.setWord(coreLabel.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase());
                    //token.setStopWord(coreLabel.get(StopWordAnnotatorWrapper.class).first);
                    token.setStopWord(false);
                    return token;
                })
                .collect(Collectors.toList());
    }


}
