/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator.stanford;

import com.google.common.base.Strings;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.librairy.tokenizer.annotator.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by cbadenes on 07/01/16.
 */
//@Component
public class StanfordCompoundTokenizer {

    private static final Logger LOG = LoggerFactory.getLogger(StanfordCompoundTokenizer.class);

    private int maxNumChars = 100;

//    private StanfordCoreNLP pipeline;
    private LexicalizedParser lp;

    @PostConstruct
    public void setup(){
//        Properties props;
//        props = new Properties();
//        //props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, stopword"); //"tokenize, ssplit, pos,
//        // lemma, ner, parse, dcoref"
//        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword"); //"tokenize, ssplit, pos, depparse
//
//        // Custom sentence split
//        props.setProperty("ssplit.boundaryTokenRegex", "[.]|[!?]+|[。]|[！？]+");
//
//        // Custom tokenize
//        //props.setProperty("tokenize.options","untokenizable=allDelete,normalizeOtherBrackets=false," +
////                "normalizeParentheses=false");
//        props.setProperty("tokenize.options","untokenizable=noneDelete,normalizeOtherBrackets=false," +
//                "normalizeParentheses=false");
//
//        // Custom stopwords
////        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.coreNlp.StopwordAnnotator");
//        props.setProperty("customAnnotatorClass.stopword", StopWordAnnotatorWrapper.class.getCanonicalName());
//        props.setProperty(StopWordAnnotatorWrapper.STOPWORDS_LIST, customStopWordList);
//
//        // Parallel
//        props.put("threads", "12");
//        pipeline = new StanfordCoreNLP(props);
        
        lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });
    }


    public List<Token> tokenize(Annotation annotation)
    {
        // List of tokens
        ConcurrentLinkedQueue<Token> tokens = new ConcurrentLinkedQueue<Token>();
        
        // Iterate over all of the sentences found
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        Integer size = sentences.size();
        AtomicInteger counter = new AtomicInteger(0);
        
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();


        sentences.parallelStream().filter(sentence -> sentence.get(CoreAnnotations.TokensAnnotation.class).toString().length() < maxNumChars).
        forEach(sentence -> {
            LOG.debug("Sentence: " + counter.getAndIncrement() + " from " + size);
            List<CoreLabel> sentenceTokens = sentence.get(TokensAnnotation.class);

            // List<CoreLabel> rawWords = Sentence.toCoreLabelList(sentence);
            Tree parse = lp.apply(sentenceTokens);
            GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            List<TypedDependency> tdl = gs.typedDependenciesCCprocessed(GrammaticalStructure.Extras.NONE);
            //System.out.println(tdl);
            // TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
            //tp.printTree(parse);

            //System.out.println();
            //System.out.println();

            TypedDependency prevD = null;

            for (TypedDependency d : tdl){
                if (d.reln().getShortName().equals("compound")){
	                	if (isValidGram(d.dep().word()) && isValidGram(d.gov().word())) {
	                    Token token = new Token();
                        String word = (isValidWord(d.dep().word()))? d.dep().word() + "_" +d.gov().word() : d.gov().word();
                            if (Strings.isNullOrEmpty(word)) LOG.warn("Null or empty word!! " + word);
	                    token.setWord(word);
	                    token.setStopWord(false);
                        if (!Strings.isNullOrEmpty(word)) tokens.add(token);
	                    if(prevD != null){
	                        IndexedWord preGov = prevD.gov();
	                        IndexedWord dGov = d.gov();
	                        if(preGov!= null && dGov != null && !Strings.isNullOrEmpty(preGov.word()) && !Strings.isNullOrEmpty(dGov.word()) && preGov.word().equals(dGov.word())){
	                            Token tokenTri = new Token();
                                String exWord = "";
                                if (!isValidWord(prevD.dep().word())){
                                    exWord = d.dep().word() + "_" +d.gov().word();
                                }else{
                                    exWord = prevD.dep().word() +"_"+d.dep().word() + "_" +d.gov().word();
                                }
	                            tokenTri.setWord(exWord);
	                            tokenTri.setStopWord(false);
                                if (Strings.isNullOrEmpty(exWord)) LOG.warn("Null or empty word!! " + exWord);
                                if (!Strings.isNullOrEmpty(exWord))  tokens.add(tokenTri);
	                        }
	                    }
                	}
                }
                prevD = d;
            }
        });

        return StreamSupport.stream(tokens.spliterator(), false).collect(Collectors.toList());
    }


	private boolean isValidGram(String word) {
		
		//single downcased letter
		if (word.length() ==1){
			if (Pattern.matches("[a-z]", word)){
				return false;
			}
		}
		
		return true;
	}


    private boolean isValidWord(String word){
        if (Strings.isNullOrEmpty(word)) return false;

        if (Arrays.asList(StanfordAnnotatorEN.customStopWordList.split(",")).contains(word)) return false;

        if (EnglishAnalyzer.getDefaultStopSet().contains(word)) return false;

        return true;

    }




}
