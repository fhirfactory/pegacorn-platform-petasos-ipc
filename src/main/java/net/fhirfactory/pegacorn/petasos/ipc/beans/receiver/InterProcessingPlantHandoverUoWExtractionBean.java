/*
 * The MIT License
 *
 * Copyright 2020 Mark A. Hunter (ACT Health).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.fhirfactory.pegacorn.petasos.ipc.beans.receiver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverPacket;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark A. Hunter
 */
@ApplicationScoped
public class InterProcessingPlantHandoverUoWExtractionBean {

    private static final Logger LOG = LoggerFactory.getLogger(InterProcessingPlantHandoverResponseGenerationBean.class);

    public UoW extractUoW(InterProcessingPlantHandoverPacket thePacket, Exchange camelExchange, String sourceSubsystem) {
        LOG.debug(".extractUoW(): Entry, thePacket --> {}", thePacket);
        UoW theUoW = thePacket.getPayloadPacket();
        UoWPayload outputPayload = new UoWPayload();
        outputPayload.setPayload(theUoW.getIngresContent().getPayload());
        TopicToken topicId = theUoW.getPayloadTopicID();
        LOG.trace(".extractUoW(): Original Topic Id --> {}", topicId);
        topicId.removeDescriminator();
        LOG.trace(".extractUoW(): Topic Id with Descriminator Removed --> {}", topicId);
        topicId.addDescriminator("Source", sourceSubsystem);
        LOG.trace(".extractUoW(): Topic Id with new Descriminator --> {}", topicId);
        outputPayload.setPayloadTopicID(topicId);
        theUoW.getEgressContent().addPayloadElement(outputPayload);
        theUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
        LOG.debug(".ipcReceiverActivityStart(): exit, extracted UoW --> {}", theUoW);
        return (theUoW);
    }
}
