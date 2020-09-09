package net.fhirfactory.pegacorn.petasos.ipc;

import org.apache.camel.builder.RouteBuilder;
import net.fhirfactory.pegacorn.petasos.ipc.generated.Message;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.netty.NettyTransceiver;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.specific.SpecificRequestor;


public class IPCServer extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel("mock:exception-handler"));

        //In Only
        from("avro:netty:localhost:" + 12121 + "/put?protocolClassName=net.fhirfactory.pegacorn.petasos.ipc.generated.Message")
                .log("Incoming message --> ${body}");

    }
}
