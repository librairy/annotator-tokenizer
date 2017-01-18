package org.librairy.tokenizer.annotator.stanford;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class StanfordAnnotatorES {

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

    private static final Logger LOG = LoggerFactory.getLogger(StanfordAnnotatorES.class);

    //adding extra terms to standard lucene listByExtension
    //adding extra terms to standard lucene listByExtension
    private static final String customStopWordList = "" +
            "alguna" +
            "algunas" +
            "alguno" +
            "algunos" +
            "algún" +
            "ambos" +
            "ampleamos" +
            "ante" +
            "antes" +
            "aquel" +
            "aquellas" +
            "aquellos" +
            "aqui" +
            "arriba" +
            "atras" +
            "bajo" +
            "bastante" +
            "bien" +
            "cada" +
            "cierta" +
            "ciertas" +
            "ciertos" +
            "como" +
            "con" +
            "conseguimos" +
            "conseguir" +
            "consigo" +
            "consigue" +
            "consiguen" +
            "consigues" +
            "cual" +
            "cuando" +
            "dentro" +
            "donde" +
            "dos" +
            "el" +
            "ellas" +
            "ellos" +
            "empleais" +
            "emplean" +
            "emplear" +
            "empleas" +
            "empleo" +
            "en" +
            "encima" +
            "entonces" +
            "entre" +
            "era" +
            "eramos" +
            "eran" +
            "eras" +
            "eres" +
            "es" +
            "esta" +
            "estaba" +
            "estado" +
            "estais" +
            "estamos" +
            "estan" +
            "estoy" +
            "fin" +
            "fue" +
            "fueron" +
            "fui" +
            "fuimos" +
            "gueno" +
            "ha" +
            "hace" +
            "haceis" +
            "hacemos" +
            "hacen" +
            "hacer" +
            "haces" +
            "hago" +
            "incluso" +
            "intenta" +
            "intentais" +
            "intentamos" +
            "intentan" +
            "intentar" +
            "intentas" +
            "intento" +
            "ir" +
            "la" +
            "largo" +
            "las" +
            "lo" +
            "los" +
            "mientras" +
            "mio" +
            "modo" +
            "muchos" +
            "muy" +
            "nos" +
            "nosotros" +
            "otro" +
            "para" +
            "pero" +
            "podeis" +
            "podemos" +
            "poder" +
            "podria" +
            "podriais" +
            "podriamos" +
            "podrian" +
            "podrias" +
            "por" +
            "por qué" +
            "porque" +
            "puede" +
            "pueden" +
            "puedo" +
            "quien" +
            "sabe" +
            "sabeis" +
            "sabemos" +
            "saben" +
            "saber" +
            "sabes" +
            "ser" +
            "si" +
            "siendo" +
            "sin" +
            "sobre" +
            "sois" +
            "solamente" +
            "solo" +
            "somos" +
            "soy" +
            "su" +
            "sus" +
            "también" +
            "teneis" +
            "tenemos" +
            "tener" +
            "tengo" +
            "tiempo" +
            "tiene" +
            "tienen" +
            "todo" +
            "trabaja" +
            "trabajais" +
            "trabajamos" +
            "trabajan" +
            "trabajar" +
            "trabajas" +
            "trabajo" +
            "tras" +
            "tuyo" +
            "ultimo" +
            "un" +
            "una" +
            "unas" +
            "uno" +
            "unos" +
            "usa" +
            "usais" +
            "usamos" +
            "usan" +
            "usar" +
            "usas" +
            "uso" +
            "va" +
            "vais" +
            "valor" +
            "vamos" +
            "van" +
            "vaya" +
            "verdad" +
            "verdadera\tcierto" +
            "verdadero" +
            "vosotras" +
            "vosotros" +
            "voy" +
            "yo";

    private final Escaper escaper = Escapers.builder()
            .addEscape('\'',"_")
            .addEscape('('," ")
            .addEscape(')'," ")
            .addEscape('['," ")
            .addEscape(']'," ")
            .build();

    private StanfordCoreNLP pipeline;

    @PostConstruct
    public void setup(){
        Properties props;
        props = new Properties();
        //props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, stopword"); //"tokenize, ssplit, pos,
        // lemma, ner, parse, dcoref"
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword, ner"); //"tokenize, ssplit, pos,

        // Custom sentence split
        props.setProperty("ssplit.boundaryTokenRegex", "[.]|[!?]+|[。]|[！？]+");

        // Custom tokenize
//        props.setProperty("tokenize.options","untokenizable=allDelete,normalizeOtherBrackets=false,normalizeParentheses=false");
        props.setProperty("tokenize.options","untokenizable=noneDelete,normalizeOtherBrackets=false," +
                "normalizeParentheses=false");

        // Spanish or English Model
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");


        // Custom stopwords
//        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.coreNlp.StopwordAnnotator");
        props.setProperty("customAnnotatorClass.stopword", StopWordAnnotatorWrapper.class.getCanonicalName());
        props.setProperty(StopWordAnnotatorWrapper.STOPWORDS_LIST, customStopWordList);

        // Parallel
        //props.put("threads", "8");
        pipeline = new StanfordCoreNLP(props);
    }

    public Annotation annotate(String text){
        // Create an empty Annotation just with the given text
        Annotation annotation = new Annotation(text);

        // run all Annotators on this text
        Instant start = Instant.now();
        pipeline.annotate(annotation);
        Instant end = Instant.now();
        LOG.debug("parsing elapsed time: " + Duration.between(start,end).toMillis() + "msecs");

        return annotation;
    }
}
