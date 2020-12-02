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

package net.fhirfactory.pegacorn.petasos.ipc.server;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.component.netty.ServerInitializerFactory;
import org.apache.camel.spi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.camel.BaseRouteBuilder;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.GenericMOAWUPTemplate;
import net.fhirfactory.pegacorn.petasos.ipc.codecs.IPCPacketDecoderInitializerFactory;
import net.fhirfactory.pegacorn.petasos.ipc.model.IPCPacketFramingConstants;

public class IPCParcelReceiver extends BaseRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(IPCParcelReceiver.class);
    private String serverHostName;
    private Integer serverHostPort;
    private Boolean linkKeepAlive;

    private final static String NETTY_TRANSPORT_TYPE = "tcp";

    IPCPacketFramingConstants framingConstants = new IPCPacketFramingConstants();

    public IPCParcelReceiver(CamelContext camelCTX, String hostName, Integer port, Boolean keepAlive) {
        super(camelCTX);
        this.serverHostName = hostName;
        this.serverHostPort = port;
        this.linkKeepAlive = keepAlive;
        Registry registry = camelCTX.getRegistry();
        ServerInitializerFactory ipcReceiverFactory = new IPCPacketDecoderInitializerFactory();
        registry.bind("ipcReceiverFactory", ipcReceiverFactory);
    }

    @Override
    public void configure() throws Exception {

        fromWithStandardExceptionHandling(ingresFeed())
                .routeId("TestRoute")
                .log(LoggingLevel.DEBUG, "content --> ${body}")
                .transform(simple("${bodyAs(String)}"))
                .to(ExchangePattern.InOnly, "direct:testingSystem")
                .process(new Processor() {
                             @Override
                            public void process(Exchange exchange) throws Exception {
                                 String inputString = (String) exchange.getIn().getBody();
                                 LOG.info("(in the processor) --> {}", framingConstants.getIpcPacketFrameEnd());
                                 String outputString = "Right Back At Ya! -->" + inputString + framingConstants.getIpcPacketFrameEnd() + "\n";
                                 exchange.getIn().setBody(outputString);
                             }
                         }
                );
        fromWithStandardExceptionHandling("direct:testingSystem")
                .log(LoggingLevel.DEBUG, "Secondary Feed: content --> ${body}");
    }

    private String ingresFeed() {
        String nettyFromString = "netty:"
                + NETTY_TRANSPORT_TYPE + ":"
                + "//" + getServerHostName() + ":"
                + getServerHostPort().toString()
                + "?serverInitializerFactory=#ipcReceiverFactory&receiveBufferSize=" + GenericMOAWUPTemplate.IPC_PACKET_MAXIMUM_FRAME_SIZE;
        return (nettyFromString);
    }

    protected String getServerHostName() {
        return serverHostName;
    }

    protected Integer getServerHostPort() {
        return serverHostPort;
    }

    protected Boolean getLinkKeepAlive() {
        return linkKeepAlive;
    }

}
