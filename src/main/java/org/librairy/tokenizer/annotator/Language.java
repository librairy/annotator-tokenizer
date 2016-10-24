/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator;

import com.google.common.base.Strings;

/**
 * Created on 01/05/16:
 *
 * @author cbadenes
 */
public enum Language {
    EN, ES;

    public static Language from(String value){
        if (Strings.isNullOrEmpty(value)) return EN;
        switch (value.toLowerCase()){
            case "en":
            case "eng":
                return EN;
            case "es":
            case "spa":
                return ES;
            default:
                return EN;
        }
    }
}
