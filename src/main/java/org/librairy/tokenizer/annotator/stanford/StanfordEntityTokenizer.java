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

	
    private StanfordCoreNLP pipeline;

    
    //adding extra terms to standard lucene listByExtension
    private static final String customStopWordList = "" +
            ".,a,also,an,and,any,are,as,at," +
            "be,become,both,bring,but,by," +
            "can,come," +
            "do," +
            "e.g.,example,extend,enough,enhance," +
            "for,from," +
            "give,get,greatly," +
            "have,highly,high," +
            "if,i.e.,in,into,is,it,its," +
            "keyword,keywords," +
            "more,most,my," +
            "no,not," +
            "of,on,or,only,onto" +
            "paper,provide," +
            "same,show,such," +
            "take,that,than,the,their,then,there,thereby,these,they,this,to,tool," +
            "use,up,"+
            "was,we,where,which,widely,will,with,yet";
    
	public StanfordEntityTokenizer(){
        Properties props;
        props = new Properties();
        //props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, stopword"); //"tokenize, ssplit, pos,
        // lemma, ner, parse, dcoref"
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword, ner"); //"tokenize, ssplit, pos, depparse

        // Custom sentence split
        props.setProperty("ssplit.boundaryTokenRegex", "[.]|[!?]+|[。]|[！？]+");

        // Custom tokenize
        //props.setProperty("tokenize.options","untokenizable=allDelete,normalizeOtherBrackets=false," +
//                "normalizeParentheses=false");
        props.setProperty("tokenize.options","untokenizable=noneDelete,normalizeOtherBrackets=false," +
                "normalizeParentheses=false");

        // Custom stopwords
//        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.coreNlp.StopwordAnnotator");
        props.setProperty("customAnnotatorClass.stopword", StopWordAnnotatorWrapper.class.getCanonicalName());
        props.setProperty(StopWordAnnotatorWrapper.STOPWORDS_LIST, customStopWordList);

        // Parallel
        //props.put("threads", "8");
        pipeline = new StanfordCoreNLP(props);
	}

    public List<Token> tokenize(Annotation annotation)
    {
    	
        // List of tokens
        ConcurrentLinkedQueue<Token> tokens = new ConcurrentLinkedQueue<Token>();

        pipeline.annotate(annotation);

        
        // Iterate over all of the sentences found
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        sentences.parallelStream().forEach(sentence -> {

            // Iterate over all tokens in a sentence
            String previousEntity = "";
            String previousType = "O";

            List<CoreLabel> tokensUnfilter = sentence.get(CoreAnnotations.TokensAnnotation.class);
            System.out.println("Number of Tokens: " +tokensUnfilter.size());
            for (CoreLabel coreLabel: sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                String currentType = coreLabel.get(NamedEntityTagAnnotation.class);
                System.out.println(currentType + "  " + coreLabel.get(CoreAnnotations.TextAnnotation.class));
                
                if (currentType !=null){
	                if (!currentType.equals("O") && isValidEntity(currentType)){
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

	private boolean isValidEntity(String currentType) {
		if (currentType.equals("O") || currentType.equals("DATE") || currentType.equals("NUMBER")|| currentType.equals("ORDINAL") || currentType.equals("LOCATION")  || currentType.equals("SET")  || currentType.equals("LOCATION")  || currentType.equals("DURATION"))
			return false;
		return true;
	}


}
