package org.librairy.tokenizer.annotator;

import edu.stanford.nlp.pipeline.Annotation;
import org.librairy.tokenizer.annotator.stanford.StanfordAnnotatorEN;
import org.librairy.tokenizer.annotator.stanford.StanfordAnnotatorES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class Annotator {

    private static final Logger LOG = LoggerFactory.getLogger(Annotator.class);

    @Autowired
    StanfordAnnotatorEN annotatorEN;

//    @Autowired
//    StanfordAnnotatorES annotatorES;

    public Annotation annotate(String text, Language lang){
        Annotation annotation;
        switch(lang){
            case EN: annotation =  annotatorEN.annotate(text);
                break;
//            case ES: annotation = annotatorES.annotate(text);
//                break;
            default: throw new RuntimeException("No language handled: " + lang);
        }
        return annotation;
    }

}
