package org.librairy.tokenizer.annotator;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Dependency;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.tokenizer.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class TokenizerTest {

    private static final Logger LOG = LoggerFactory.getLogger(TokenizerTest.class);


    @Autowired
    Annotator annotator;

    @Autowired
    TokenizerFactory tokenizerFactory;

    @Test
    public void phrase() throws IOException {

        String example = new String(Files.readAllBytes(Paths.get("src/test/resources/book.txt")));
        
        // List<Token> tokens = tokenizer.tokenize(text, Language.EN);
        String tokenizerMode = "ner";
        
        
        Language language = Language.EN;

        Annotation annotation = annotator.annotate(example, language);

		Tokenizer tokenizer = tokenizerFactory.of(tokenizerMode);
		System.out.println("The tokenizer is " + tokenizer.getMode());
		long startTime = System.nanoTime();
        List<String> tokens = tokenizer.tokenize(annotation).stream().
                map(token -> token.getWord()).collect(Collectors.toList());
        long estimatedTime = System.nanoTime() - startTime;

        
        LOG.info("Tokens: " + tokens);
        LOG.info("Elapsed time: " + estimatedTime);

        Hashtable<String, Integer> tokensCount = new Hashtable<String, Integer>();
        for (String token : tokens){
        	if (tokensCount.containsKey(token)){
        		tokensCount.put(token, tokensCount.get(token)+1);
        		
        	}
        	else tokensCount.put(token, 0);
        }
        
        ArrayList<Map.Entry<?, Integer>> l = new ArrayList<Map.Entry<?, Integer>>(tokensCount.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<?, Integer>>(){

          public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
             return o2.getValue().compareTo(o1.getValue());
         }});

        for (Map.Entry<?, Integer> word : l){
        	System.out.println(word.getKey() + "  " + word.getValue());
        }
        
        
        
        /*LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });
        String example = "The Semantic Web community is very related with other domains such as information extraction or knowledge representation .";
        String[] sent = example.split(" ");
        List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
        Tree parse = lp.apply(rawWords);
        parse.pennPrint();
        System.out.println();

        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);
       // TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
        //tp.printTree(parse);
        
        System.out.println();
        System.out.println();

        for (TypedDependency d : tdl){
        	System.out.println(d.reln().getShortName() + "   " +d.dep().word()+ "  "+d.gov().word() );

        }
*/    }

}
