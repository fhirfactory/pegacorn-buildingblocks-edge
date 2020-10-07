/*
 * Copyright (c) 2020 Mark A. Hunter (ACT Health)
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
package net.fhirfactory.pegacorn.platform.edge.forward.common;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.petasos.ipc.beans.sender.InterProcessingPlantHandoverFinisherBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.sender.InterProcessingPlantHandoverPacketEncoderBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.sender.InterProcessingPlantHandoverPacketGenerationBean;
import net.fhirfactory.pegacorn.petasos.ipc.codecs.IPCDelimiterBasedDecoderFactory;
import net.fhirfactory.pegacorn.petasos.ipc.codecs.IPCStringBasedDecoderFactory;
import net.fhirfactory.pegacorn.petasos.ipc.codecs.IPCStringBasedEncoderFactory;
import net.fhirfactory.pegacorn.petasos.model.processingplant.DefaultWorkshopSetEnum;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.EdgeEgressMessagingGatewayWUP;
import net.fhirfactory.pegacorn.petasos.ipc.beans.sender.InterProcessingPlantHandoverPacketResponseDecoder;
import net.fhirfactory.pegacorn.petasos.ipc.model.IPCPacketFramingConstants;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.netty.codec.DelimiterBasedFrameDecoder;
import org.apache.camel.spi.Registry;

public abstract class EdgeIPCForwarderWUPTemplate extends EdgeEgressMessagingGatewayWUP {
    
    @Inject
    CamelContext camelCTX;
    
    @Override
    protected void executePostInitialisationActivities(){
        IPCDelimiterBasedDecoderFactory ipcDelimiterDecoderFactory = new IPCDelimiterBasedDecoderFactory();
        IPCStringBasedDecoderFactory ipcStringDecoderFactory = new IPCStringBasedDecoderFactory();
        IPCStringBasedEncoderFactory ipcStringEncoderFactory = new IPCStringBasedEncoderFactory();

        Registry registry = camelCTX.getRegistry();

        registry.bind("ipcFrameDecoder", ipcDelimiterDecoderFactory);
        registry.bind("ipcStringDecoder", ipcStringDecoderFactory);

        List<ChannelHandler> decoders = new ArrayList<ChannelHandler>();
        decoders.add(ipcDelimiterDecoderFactory);
        decoders.add(ipcStringDecoderFactory);
        registry.bind("decoders", decoders);

        registry.bind("ipcStringEncoder", ipcStringEncoderFactory);
        List<ChannelHandler> encoders = new ArrayList<>();
        encoders.add(ipcStringEncoderFactory);
        registry.bind("encoders", encoders);
    }

    @Override
    public void configure() throws Exception {
        getLogger().info("EdgeIPCForwarderWUPTemplate :: WUPIngresPoint/ingresFeed --> {}", this.ingresFeed());
        getLogger().info("EdgeIPCForwarderWUPTemplate :: WUPEgressPoint/egressFeed --> {}", this.egressFeed());

        from(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .log(LoggingLevel.INFO, "Incoming Raw Message --> ${body}")
                .bean(InterProcessingPlantHandoverPacketGenerationBean.class, "constructInterProcessingPlantHandoverPacket(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .bean(InterProcessingPlantHandoverPacketEncoderBean.class, "handoverPacketEncode")
                .to(specifyEgressEndpoint())
                .transform(simple("${bodyAs(String)}"))
                .log(LoggingLevel.INFO, "Response Raw Message --> ${body}")
                .bean(InterProcessingPlantHandoverPacketResponseDecoder.class, "contextualiseInterProcessingPlantHandoverResponsePacket(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .bean(InterProcessingPlantHandoverFinisherBean.class, "ipcSenderNotifyActivityFinished(*, Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")");
    }

    protected abstract String specifyTargetSubsystem();
    protected abstract String specifyTargetSubsystemVersion();
    protected abstract String specifyTargetEndpointName();
    protected abstract String specifyTargetEndpointVersion();
    protected abstract String specifyTargetService();
    protected abstract String specifyTargetProcessingPlant();

    @Override
    protected String deriveTargetEndpointDetails(){
        getLogger().debug(".deriveTargetEndpointDetails(): Entry");
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem Name --> {}, Target Subsystem Version --> {}", specifyTargetSubsystem(), specifyTargetSubsystemVersion());
        NodeElement targetSubsystem = getTopologyServer().getNode(specifyTargetSubsystem(), NodeElementTypeEnum.SUBSYSTEM, specifyTargetSubsystemVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem (NodeElement) --> {}", targetSubsystem);
        NodeElement targetNode;
        switch(targetSubsystem.getResilienceMode()){
            case RESILIENCE_MODE_MULTISITE:
            case RESILIENCE_MODE_CLUSTERED:{
                targetNode = getTopologyServer().getNode(specifyTargetService(), NodeElementTypeEnum.SERVICE, specifyTargetSubsystemVersion());
                break;
            }
            case RESILIENCE_MODE_STANDALONE:
            default:{
                targetNode = getTopologyServer().getNode(specifyTargetProcessingPlant(), NodeElementTypeEnum.PROCESSING_PLANT, specifyTargetSubsystemVersion());
            }
        }
        getLogger().trace(".deriveTargetEndpointDetails(): targetNode --> {}", targetNode);
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpointName --> {}, targetEndpointVersion --> {}", specifyTargetEndpointName(), specifyTargetEndpointVersion());
        EndpointElement endpoint = getTopologyServer().getEndpoint(targetNode, specifyTargetEndpointName(), specifyTargetEndpointVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpoint (EndpointElement) --> {}", endpoint);
        String endpointDetails = endpoint.getHostname() + ":" + endpoint.getExposedPort();
        getLogger().debug(".deriveTargetEndpointDetails(): Exit, endpointDetails --> {}", endpointDetails);
        return(endpointDetails);
    }

    @Override
    protected String specifyWUPWorkshop(){
        return(DefaultWorkshopSetEnum.EDGE_WORKSHOP.getWorkshop());
    }

    @Override
    protected String specifyEndpointComponentDefinition() {
        return ("netty");
    }

    @Override
    protected String specifyEndpointProtocolLeadout() {
        return ("?allowDefaultCodec=false&decoders=#ipcFrameDecoder&encoders=#ipcStringEncoder&keepAlive=false");
    }

    @Override
    protected String specifyEndpointProtocolLeadIn() {
        return ("://");
    }

    @Override
    protected String specifyEndpointProtocol() {
        return ("tcp");
    }

}
