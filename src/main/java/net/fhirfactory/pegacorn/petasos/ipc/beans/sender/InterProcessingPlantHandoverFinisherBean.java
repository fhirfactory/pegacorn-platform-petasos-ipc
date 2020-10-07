/*
 * Copyright (c) 2020 Mark A. Hunter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.petasos.ipc.beans.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.common.model.FDN;
import net.fhirfactory.pegacorn.common.model.RDN;
import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.core.moa.brokers.PetasosMOAServicesBroker;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming.PetasosPathwayExchangePropertyNames;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverPacket;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverResponsePacket;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverContextualResponse;
import net.fhirfactory.pegacorn.petasos.model.pathway.ActivityID;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicTypeEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementFunctionToken;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementIdentifier;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPActivityStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPIdentifier;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class InterProcessingPlantHandoverFinisherBean {
    private static final Logger LOG = LoggerFactory.getLogger(InterProcessingPlantHandoverFinisherBean.class);

    @Inject
    DeploymentTopologyIM topologyProxy;

    @Inject
    PetasosMOAServicesBroker servicesBroker;

    @Inject
    PetasosPathwayExchangePropertyNames exchangePropertyNames;

    public UoW ipcSenderNotifyActivityFinished(InterProcessingPlantHandoverContextualResponse handoverPacket, Exchange camelExchange, String wupInstanceKey) throws JsonProcessingException {
        LOG.debug(".ipcSenderNotifyActivityFinished(): Entry, handoverPacket (InterProcessingPlantHandoverContextualResponse) --> {}, wupInstanceKey (String) --> {}", handoverPacket, wupInstanceKey);
        LOG.trace(".ipcSenderNotifyActivityFinished(): Get Job Card and Status Element from Exchange for extraction by the WUP Egress Conduit");
        String jobcardPropertyKey = exchangePropertyNames.getExchangeJobCardPropertyName(wupInstanceKey); // this value should match the one in WUPIngresConduit.java/WUPEgressConduit.java
        String parcelStatusPropertyKey = exchangePropertyNames.getExchangeStatusElementPropertyName(wupInstanceKey); // this value should match the one in WUPIngresConduit.java/WUPEgressConduit.java
        WUPJobCard activityJobCard = camelExchange.getProperty(jobcardPropertyKey, WUPJobCard.class); // <-- Note the "WUPJobCard" property name, make sure this is aligned with the code in the WUPEgressConduit.java file
        ParcelStatusElement statusElement = camelExchange.getProperty(parcelStatusPropertyKey, ParcelStatusElement.class); // <-- Note the "ParcelStatusElement" property name, make sure this is aligned with the code in the WUPEgressConduit.java file
        LOG.trace(".ipcSenderNotifyActivityFinished(): Extract the UoW");
        UoW theUoW = handoverPacket.getTheUoW();
        LOG.trace(".ipcSenderNotifyActivityFinished(): Extracted UoW --> {}", theUoW);
        InterProcessingPlantHandoverResponsePacket responsePacket = handoverPacket.getResponsePacket();
        switch(responsePacket.getStatus()){
            case PACKET_RECEIVED_AND_DECODED:
                LOG.trace(".ipcSenderNotifyActivityFinished(): PACKET_RECEIVED_AND_DECODED");
                theUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
                break;
            case PACKET_RECEIVED_BUT_FAILED_DECODING:
                LOG.trace(".ipcSenderNotifyActivityFinished(): PACKET_RECEIVED_BUT_FAILED_DECODING");
                theUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
                theUoW.setFailureDescription("Message encoding/decoding failure!");
                break;
            case PACKET_RECEIVE_TIMED_OUT:
                LOG.trace(".ipcSenderNotifyActivityFinished(): PACKET_RECEIVE_TIMED_OUT");
            default:
                theUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
                theUoW.setFailureDescription("Message delivery failure!");
        }
        ObjectMapper jsonMapper = new ObjectMapper();
        String egressPayloadData = jsonMapper.writeValueAsString(responsePacket);
        UoWPayload egressPayload = new UoWPayload();
        egressPayload.setPayload(egressPayloadData);
        FDN egressPayloadTopicFDN = new FDN();
        egressPayloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_DEFINER.getTopicType(), "Pegacorn"));
        egressPayloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_CATEGORY.getTopicType(), "IPC"));
        egressPayloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_SUBCATEGORY.getTopicType(), "Model"));
        egressPayloadTopicFDN.appendRDN(new RDN(TopicTypeEnum.DATASET_RESOURCE.getTopicType(), "InterProcessingPlantHandoverResponsePacket"));
        TopicToken egressPayloadTopic = new TopicToken();
        egressPayloadTopic.setIdentifier(egressPayloadTopicFDN.getToken());
        egressPayloadTopic.setVersion("1.0.0");
        egressPayload.setPayloadTopicID(egressPayloadTopic);
        theUoW.getEgressContent().addPayloadElement(egressPayload);
        switch(theUoW.getProcessingOutcome()){
            case UOW_OUTCOME_SUCCESS:
                servicesBroker.notifyFinishOfWorkUnitActivity(activityJobCard, theUoW);
                break;
            case UOW_OUTCOME_NOTSTARTED:
            case UOW_OUTCOME_INCOMPLETE:
            case UOW_OUTCOME_FAILED:
                servicesBroker.notifyFailureOfWorkUnitActivity(activityJobCard, theUoW);
        }
        LOG.debug(".ipcSenderNotifyActivityFinished(): exit, theUoW (UoW) --> {}", theUoW);
        return(theUoW);
    }
}
