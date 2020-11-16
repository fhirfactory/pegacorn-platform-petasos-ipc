/*
 * The MIT License
 *
 * Copyright 2020 mhunter.
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
package net.fhirfactory.pegacorn.petasos.ipc.beans.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming.PetasosPathwayExchangePropertyNames;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverContextualResponse;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverPacket;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverResponsePacket;
import net.fhirfactory.pegacorn.petasos.model.processingplant.ProcessingPlantServicesInterface;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mhunter
 */
@ApplicationScoped
public class InterProcessingPlantHandoverPacketResponseDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(InterProcessingPlantHandoverPacketGenerationBean.class);

    @Inject
    DeploymentTopologyIM topologyProxy;

    @Inject
    PetasosPathwayExchangePropertyNames exchangePropertyNames;

    @Inject
    ProcessingPlantServicesInterface processingPlantServices;

    public InterProcessingPlantHandoverContextualResponse contextualiseInterProcessingPlantHandoverResponsePacket(String responseMessage, Exchange camelExchange, String wupInstanceKey) {
        LOG.debug(".contextualiseInterProcessingPlantHandoverResponsePacket(): Entry, responseMessage (String) --> {}, wupInstanceKey (String) --> {}", responseMessage, wupInstanceKey);
        LOG.trace(".contextualiseInterProcessingPlantHandoverResponsePacket(): Convert incoming message string to an InterProcessingPlantHandoverResponsePacket");
        InterProcessingPlantHandoverResponsePacket responsePacket;
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            responsePacket = jsonMapper.readValue(responseMessage, InterProcessingPlantHandoverResponsePacket.class);
            LOG.trace(".contextualiseInterProcessingPlantHandoverResponsePacket(): Converted incoming message to InterProcessingPlantHandoverResponsePacket");
        } catch (JsonProcessingException jsonException) {
            responsePacket = new InterProcessingPlantHandoverResponsePacket();
            LOG.trace(".contextualiseInterProcessingPlantHandoverResponsePacket(): Could not convert incoming response message");
        }
        LOG.trace(".contextualiseInterProcessingPlantHandoverResponsePacket(): Attempting to retrieve UoW from the Exchange");
        String uowPropertyKey = exchangePropertyNames.getExchangeUoWPropertyName(wupInstanceKey);
        UoW theUoW = camelExchange.getProperty(uowPropertyKey, UoW.class);
        LOG.trace(".contextualiseInterProcessingPlantHandoverResponsePacket(): Retrieved UoW --> {}", theUoW);
        LOG.trace(".contextualiseInterProcessingPlantHandoverResponsePacket(): Creating the Response message");
        InterProcessingPlantHandoverContextualResponse contextualisedResponsePacket = new InterProcessingPlantHandoverContextualResponse();
        contextualisedResponsePacket.setResponsePacket(responsePacket);
        contextualisedResponsePacket.setTheUoW(theUoW);
        LOG.debug(".contextualiseInterProcessingPlantHandoverResponsePacket(): Exit, contextualisedResponsePacket (InterProcessingPlantHandoverContextualResponse) --> {}", contextualisedResponsePacket);
        return (contextualisedResponsePacket);
    }
}
