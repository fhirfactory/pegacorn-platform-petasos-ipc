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
package net.fhirfactory.pegacorn.petasos.ipc.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import net.fhirfactory.pegacorn.petasos.ipc.model.IPCPacketFramingConstants;
import org.apache.camel.component.netty.NettyConsumer;
import org.apache.camel.component.netty.ServerInitializerFactory;
import org.apache.camel.component.netty.codec.DelimiterBasedFrameDecoder;
import org.apache.camel.component.netty.handlers.ServerChannelHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class IPCPacketDecoderInitializerFactory extends ServerInitializerFactory {
    private NettyConsumer consumer;

    @Inject
    private IPCPacketFramingConstants framingConstants;

    @Override
    public ServerInitializerFactory createPipelineFactory(NettyConsumer consumer) {
        this.consumer = consumer;
        return(this);
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline ipcReceiverPipeline = channel.pipeline();

        //TODO clean up CDI as this is called directly by netty and explicitly in the code where CDI is not used
        if (framingConstants == null) {
            framingConstants = new IPCPacketFramingConstants();
        }
        
        // Define Delimeters
        ByteBuf ipcFrameEnd = Unpooled.copiedBuffer(framingConstants.getIpcPacketFrameEnd(),CharsetUtil.UTF_8);
        ByteBuf[] delimiterSet = new ByteBuf[1];
        delimiterSet[0] = ipcFrameEnd;
        // Define the encoders/decoders to be used
        StringEncoder string2ByteBufEncoder = new StringEncoder(CharsetUtil.UTF_8);
        DelimiterBasedFrameDecoder ipcFrameCapture = new DelimiterBasedFrameDecoder(
                framingConstants.getIpcPacketMaximumFrameSize(),
                true,
                delimiterSet);
        StringDecoder byteBuf2StringDecoder = new StringDecoder(CharsetUtil.UTF_8);
        //Now, Define the Pipeline
        ipcReceiverPipeline.addLast("string2ByteBuf", string2ByteBufEncoder);
        ipcReceiverPipeline.addLast("ipcFrameCapture",ipcFrameCapture);
        ipcReceiverPipeline.addLast("byteBuf2String", byteBuf2StringDecoder);
        // here we add the default Camel ServerChannelHandler for the consumer, to allow Camel to route the message etc.
        ipcReceiverPipeline.addLast("handler", new ServerChannelHandler(consumer));
    }
}
