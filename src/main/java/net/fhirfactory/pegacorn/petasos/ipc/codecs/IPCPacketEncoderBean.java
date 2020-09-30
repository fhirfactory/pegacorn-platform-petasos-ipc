package net.fhirfactory.pegacorn.petasos.ipc.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverPacket;
import net.fhirfactory.pegacorn.petasos.ipc.model.IPCPacketFramingConstants;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class IPCPacketEncoderBean {

    @Inject
    private IPCPacketFramingConstants framingConstants;

    public String ipcPacketEncapsulatedPayload(UoW incomingUoW) throws JsonProcessingException {
        ObjectMapper jsonMapper = new ObjectMapper();
        String outputPayload = jsonMapper.writeValueAsString(incomingUoW);
        InterProcessingPlantHandoverPacket interProcessingPlantHandoverPacket = new InterProcessingPlantHandoverPacket();

        outputPayload = outputPayload + framingConstants.getIpcPacketFrameEnd();
        return(outputPayload);
    }
}
