/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator.stanford;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Dependency;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import org.librairy.tokenizer.annotator.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by cbadenes on 07/01/16.
 */
public class StanfordTokenizerCompoundEN implements StanfordTokenizer {

    /**
     *
     CC Coordinating conjunction
     CD Cardinal number
     DT Determiner
     EX Existential there
     FW Foreign word
     IN Preposition or subordinating conjunction
     JJ Adjective
     JJR Adjective, comparative
     JJS Adjective, superlative
     LS List item marker
     MD Modal
     NN Noun, singular or mass
     NNS Noun, plural
     NNP Proper noun, singular
     NNPS Proper noun, plural
     PDT Predeterminer
     POS Possessive ending
     PRP Personal pronoun
     PRP$ Possessive pronoun
     RB Adverb
     RBR Adverb, comparative
     RBS Adverb, superlative
     RP Particle
     SYM Symbol
     TO to
     UH Interjection
     VB Verb, base form
     VBD Verb, past tense
     VBG Verb, gerund or present participle
     VBN Verb, past participle
     VBP Verb, non­3rd person singular present
     VBZ Verb, 3rd person singular present
     WDT Whdeterminer
     WP Whpronoun
     WP$ Possessive whpronoun
     WRB Whadverb
     */

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

    private StanfordCoreNLP pipeline;
    private LexicalizedParser lp;
    public StanfordTokenizerCompoundEN(){
        Properties props;
        props = new Properties();
        //props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, stopword"); //"tokenize, ssplit, pos,
        // lemma, ner, parse, dcoref"
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword"); //"tokenize, ssplit, pos, depparse

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
        
        lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });
    }


    public List<Token> tokenize(String text)
    {
        // List of tokens
        List<Token> tokens = new ArrayList<>();

        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);
        
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
   
        	  List<CoreLabel> sentenceTokens = sentence.get(TokensAnnotation.class);

        	// List<CoreLabel> rawWords = Sentence.toCoreLabelList(sentence);
             Tree parse = lp.apply(sentenceTokens); 
             TreebankLanguagePack tlp = new PennTreebankLanguagePack();
             GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
             GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
             List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
             //System.out.println(tdl);
            // TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
             //tp.printTree(parse);
             
             //System.out.println();
             //System.out.println();

             TypedDependency prevD = null;
             
             for (TypedDependency d : tdl){
            	 if (d.reln().getShortName().equals("compound")){
            		 
                     Token token = new Token();
                     token.setWord(d.dep().word() + "_" +d.gov().word());
                     token.setStopWord(false);
                     tokens.add(token);
                     if(prevD != null){
                    	 if(prevD.gov().word().equals(d.gov().word())){
                             Token tokenTri = new Token();
                             tokenTri.setWord(prevD.dep().word() +"_"+d.dep().word() + "_" +d.gov().word());
                             tokenTri.setStopWord(false);
                             tokens.add(tokenTri);
                    	 }
                     }
                     
            	 }
            	  prevD = d;


             }
             //parse.pennPrint();
             //System.out.println();

             //TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            // GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            // GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            // List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
            // System.out.println(tdl);
            // TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
             //tp.printTree(parse);
        }
        return tokens;
    }





}
