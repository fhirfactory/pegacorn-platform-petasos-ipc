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
package net.fhirfactory.pegacorn.petasos.ipc.beans.receiver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.petasos.ipc.model.IPCPacketFramingConstants;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverPacket;
import net.fhirfactory.pegacorn.petasos.ipc.model.InterProcessingPlantHandoverResponsePacket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class InterProcessingPlantHandoverResponseEncoderBean {
    private static final Logger LOG = LoggerFactory.getLogger(InterProcessingPlantHandoverResponseEncoderBean.class);
    

    @Inject
    private IPCPacketFramingConstants framingConstants;

    public String responseEncoder(InterProcessingPlantHandoverResponsePacket responsePacket) throws JsonProcessingException {
        ObjectMapper jsonMapper = new ObjectMapper();
        String outputPayload = jsonMapper.writeValueAsString(responsePacket);
        outputPayload = outputPayload + framingConstants.getIpcPacketFrameEnd();
        LOG.debug(".responseEncoder(): Response Message --> {}", outputPayload);
        return (outputPayload);
    }
}
