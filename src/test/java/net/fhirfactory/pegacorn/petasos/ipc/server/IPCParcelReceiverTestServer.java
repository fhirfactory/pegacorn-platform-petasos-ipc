package net.fhirfactory.pegacorn.petasos.ipc.server;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.camel.BaseRouteBuilder;

@ApplicationScoped
public class IPCParcelReceiverTestServer extends BaseRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(IPCParcelReceiverTestServer.class);

    @Inject
    CamelContext camelctx;

    public void startServer(){
        LOG.info(".startServer(): Building an IPC Receiver route");
        IPCParcelReceiver ipcReceiver = new IPCParcelReceiver(camelctx, "localhost",13131,true);
        LOG.info(".startServer(): Route created, now adding it to he CamelContext!");
        try {
            camelctx.addRoutes(ipcReceiver);
        } catch(Exception ex){
            LOG.error(".startServer(): Could not start server --> {}", ex);
        }
    }

    @Override
    public void configure() throws Exception {
        startServer();

        fromWithStandardExceptionHandling("timer://myTimer?period=10000")
                .log(LoggingLevel.INFO, "IPCParcelReceiverTestServer is alive!")
                .end();
    }
}
