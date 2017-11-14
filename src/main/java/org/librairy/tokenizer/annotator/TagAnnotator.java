package org.librairy.tokenizer.annotator;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.librairy.tokenizer.annotator.stanford.StanfordAnnotatorEN;
import org.librairy.tokenizer.annotator.stanford.StanfordAnnotatorES;
import org.librairy.tokenizer.utils.Tag;
import org.librairy.tokenizer.utils.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
//@Component
public class TagAnnotator {

    @Autowired
    CompoundTokenizer compoundTokenizer;

    @Autowired
    EntityTokenizer entityTokenizer;

    private static final Logger LOG = LoggerFactory.getLogger(TagAnnotator.class);

    public String annotate(Map<String,StringBuilder> tokens, Language language){

        double entityCorrection = 1.0;

        List<Tag> result = new ArrayList<>();
        if (tokens.containsKey(compoundTokenizer.getMode())){
            String[] compounds = getTags(tokens.get(compoundTokenizer.getMode()).toString(), language, 5).split(" ");
            List<Tag> compoundsRanked = TagUtils.rankAnnotations(compounds, "compound", 1.0);
            if (!compoundsRanked.isEmpty()) entityCorrection =0.8;
            result.addAll(compoundsRanked);
        }

        if (tokens.containsKey(entityTokenizer.getMode())){
            String[] entities = getTags(tokens.get(entityTokenizer.getMode()).toString(), language, 5).split(" ");
            List<Tag> entitiesRanked = TagUtils.rankAnnotations(entities, "ner", entityCorrection);
            result.addAll(entitiesRanked);
        }

        Collections.sort(result, (a,b) -> -a.getScore().compareTo(b.getScore()));

        return result.stream().limit(10).map( tag -> tag.getSurface()).collect(Collectors.joining(" "));

    }

    public String getTags(String tokens, Language language, Integer num){

        Map<String, List<String>> out = Arrays.stream(tokens.split(" ")).map(token -> adjust(token, language)).collect(Collectors.groupingBy(String::toString));

        return out.entrySet().stream().filter(a -> isValid(a.getKey(), language)).sorted((a, b) -> -Integer.valueOf(a.getValue().size()).compareTo(Integer.valueOf(b.getValue().size()))).limit(num).map(e -> e.getKey()).collect(Collectors.joining(" "));

    }


    private String adjust(String text, Language language){
        if (text.contains("_")){
            String[] w = text.split("_");
            if ((w!=null) && (w.length>0) &&  !isValid(w[0],language)) return StringUtils.substringAfter(text,"_");
        }
        return text;
    }

    private boolean isValid(String text, Language language){
        String refText = text.toLowerCase();

        if (!CharMatcher.JAVA_LETTER.matchesAnyOf(text)) return false;

        if (text.contains("_")){
            for (String word : refText.split("_")){
                if (isStopWord(word, language)) return false;
            }
        }
        return true;

    }

    private boolean isStopWord(String word, Language language){

        switch (language){
            case EN: return EnglishAnalyzer.getDefaultStopSet().contains(word) || StanfordAnnotatorEN.customStopWordList.contains(word);
            case ES: return SpanishAnalyzer.getDefaultStopSet().contains(word) || StanfordAnnotatorES.customStopWordList.contains(word);
            default: return false;
        }
    }

}
