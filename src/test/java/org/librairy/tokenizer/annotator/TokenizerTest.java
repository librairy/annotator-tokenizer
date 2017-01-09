package org.librairy.tokenizer.annotator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.boot.model.domain.resources.Item;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.storage.UDM;
import org.librairy.tokenizer.Config;
import org.librairy.tokenizer.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Dependency;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class TokenizerTest {

    private static final Logger LOG = LoggerFactory.getLogger(TokenizerTest.class);



    @Autowired
    TokenizerFactory tokenizerFactory;

    @Test
    public void phrase(){

        String text  = "The name of Messi is a sample for testing";
        String example = "The Semantic Web community is very related with other domains such as information extraction or knowledge representation .";

       // List<Token> tokens = tokenizer.tokenize(text, Language.EN);
        String tokenizerMode = "compound";
        
        
        Language language = Language.EN;
        List<String> tokens = tokenizerFactory.of(tokenizerMode).tokenize(example, language).stream().
                map(token -> token.getWord()).collect(Collectors.toList());

        LOG.info("Tokens: " + tokens);
        System.out.println("Tokens: " + tokens);

        
        
        
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
