/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by cbadenes on 07/01/16.
 */
@Data
public class Token implements Serializable {

    String word = "";

    String pos = "n";

    boolean stopWord;
    boolean isEntity = false;

    private final Escaper escaper = Escapers.builder()
            .addEscape('\'',"")
            .addEscape('\"',"")
            .addEscape('('," ")
            .addEscape(')'," ")
            .addEscape('['," ")
            .addEscape(']'," ")
            .build();

    public boolean isValid(){
        return !stopWord
                && word.length()>2
                && pos.toLowerCase().startsWith("n");
    }

    public void setWord(String word){
        this.word = escaper.escape(word);
    }

}
