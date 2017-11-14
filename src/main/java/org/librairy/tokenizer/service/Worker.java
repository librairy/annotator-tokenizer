package org.librairy.tokenizer.service;

import org.librairy.boot.storage.executor.ParallelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class Worker {

    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

    private ExecutorService executor;

//    private ParallelExecutor executor;

    @Value("#{environment['LIBRAIRY_TOKENIZER_MAX_PARALLEL']?:${librairy.tokenizer.max.parallel}}")
    Integer maxThreads;

    @PostConstruct
    public void setup(){

//        this.executor = new ParallelExecutor(2);

        this.executor = Executors.newFixedThreadPool(maxThreads);

    }

    public void run(Runnable task){
        this.executor.execute(task);
    }
}
