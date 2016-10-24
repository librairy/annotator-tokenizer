/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.tokenizer.annotator;

import java.util.List;

/**
 * Created on 07/04/16:
 *
 * @author cbadenes
 */
public interface Tokenizer {

    List<Token> tokenize(String text, Language language);

}
