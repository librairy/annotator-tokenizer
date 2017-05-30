package org.librairy.tokenizer;

import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class DeployTest {

    private static final Logger LOG = LoggerFactory.getLogger(DeployTest.class);

    @Test
    public void run() throws InterruptedException {

        LOG.info("Sleepping...");
        Thread.sleep(Integer.MAX_VALUE);
        LOG.info("Wake Up!");
    }
}
