package org.librairy.tokenizer.eventbus;

import org.librairy.model.Event;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.modules.BindingKey;
import org.librairy.model.modules.EventBus;
import org.librairy.model.modules.EventBusSubscriber;
import org.librairy.model.modules.RoutingKey;
import org.librairy.storage.UDM;
import org.librairy.tokenizer.service.PartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class PartCreatedEventHandler implements EventBusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(PartCreatedEventHandler.class);

    @Autowired
    PartService service;

    @Autowired
    protected EventBus eventBus;

    @PostConstruct
    public void init(){
        RoutingKey routingKey = RoutingKey.of(Resource.Type.PART, Resource.State.CREATED);
        LOG.info("Trying to register as subscriber of '" + routingKey + "' events ..");
        eventBus.subscribe(this, BindingKey.of(routingKey, "tokenizer-part"));
        LOG.info("registered successfully");
    }


    @Override
    public void handle(Event event) {
        LOG.debug("New Part event received: " + event);
        try{

            Resource resource = event.to(Resource.class);
            service.handleParallel(resource);

        } catch (RuntimeException e){
            LOG.warn(e.getMessage());
        }catch (Exception e){
            LOG.error("Error adding new source: " + event, e);
        }
    }
}