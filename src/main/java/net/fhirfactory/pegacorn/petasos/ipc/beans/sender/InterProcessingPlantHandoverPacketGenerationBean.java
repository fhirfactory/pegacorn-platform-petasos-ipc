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

import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.core.moa.pathway.naming.PetasosPathwayExchangePropertyNames;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverPacket;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverResponsePacket;
import net.fhirfactory.pegacorn.petasos.model.processingplant.ProcessingPlantServicesInterface;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.wup.WUPJobCard;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;

@ApplicationScoped
public class InterProcessingPlantHandoverPacketGenerationBean {
    private static final Logger LOG = LoggerFactory.getLogger(InterProcessingPlantHandoverPacketGenerationBean.class);

    @Inject
    DeploymentTopologyIM topologyProxy;

    @Inject
    PetasosPathwayExchangePropertyNames exchangePropertyNames;

    @Inject
    ProcessingPlantServicesInterface processingPlantServices;

    public InterProcessingPlantHandoverPacket generateInterProcessingPlantHandoverPacket(UoW theUoW, Exchange camelExchange, String wupInstanceKey){
        LOG.debug(".generateInterProcessingPlantHandoverPacket(): Entry, theUoW (UoW) --> {}, wupInstanceKey (String) --> {}", theUoW, wupInstanceKey);
        LOG.trace(".generateInterProcessingPlantHandoverPacket(): Attempting to retrieve NodeElement");
        NodeElement node = topologyProxy.getNodeByKey(wupInstanceKey);
        LOG.trace(".generateInterProcessingPlantHandoverPacket(): Node Element retrieved --> {}", node);
        LOG.trace(".generateInterProcessingPlantHandoverPacket(): Extracting Job Card and Status Element from Exchange");
        String jobcardPropertyKey = exchangePropertyNames.getExchangeJobCardPropertyName(wupInstanceKey); // this value should match the one in WUPIngresConduit.java/WUPEgressConduit.java
        String parcelStatusPropertyKey = exchangePropertyNames.getExchangeStatusElementPropertyName(wupInstanceKey); // this value should match the one in WUPIngresConduit.java/WUPEgressConduit.java
        WUPJobCard jobCard = camelExchange.getProperty(jobcardPropertyKey, WUPJobCard.class); // <-- Note the "WUPJobCard" property name, make sure this is aligned with the code in the WUPEgressConduit.java file
        ParcelStatusElement statusElement = camelExchange.getProperty(parcelStatusPropertyKey, ParcelStatusElement.class); // <-- Note the "ParcelStatusElement" property name, make sure this is aligned with the code in the WUPEgressConduit.java file
        LOG.trace(".generateInterProcessingPlantHandoverPacket(): Creating the Response message");
        InterProcessingPlantHandoverPacket forwardingPacket = new InterProcessingPlantHandoverPacket();
        forwardingPacket.setActivityID(jobCard.getActivityID());
        String processingPlantName = processingPlantServices.getProcessingPlantNodeElement().extractNodeKey();
        forwardingPacket.setMessageIdentifier(processingPlantName + "-" + Date.from(Instant.now()).toString());
        forwardingPacket.setSendDate(LocalDateTime.now());
        forwardingPacket.setPayloadPacket(theUoW);
        LOG.debug(".generateInterProcessingPlantHandoverPacket(): Exit, forwardingPacket (InterProcessingPlantHandoverPacket) --> {}", forwardingPacket);
        return(forwardingPacket);
    }
}
