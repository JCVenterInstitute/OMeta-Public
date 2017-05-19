package org.jcvi.ometa.stateless_session_bean;

import org.apache.log4j.Logger;
import org.jcvi.ometa.utils.JsonProducer;

import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

/**
 * Created by mkuscuog on 10/10/2016.
 */
@Singleton
@LocalBean
public class JsonProducerBean {
    private Logger logger = Logger.getLogger(JsonProducerBean.class);

    //Runs 7 AM everyday (Server Time)
    @Schedule(hour="7")
    public void runJsonProducer() {
        logger.info("[JsonProducerBean] JsonProducerBean process is starting.");

        JsonProducer jsonProducerBean = new JsonProducer();
        jsonProducerBean.run();

        logger.info("[JsonProducerBean] JsonProducerBean process is done.");
    }
}
