package net.fhirfactory.pegacorn.platform.edge.receive.common;

import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverPacketDecoderBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverRegistrationBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverResponseEncoderBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverResponseGenerationBean;
import net.fhirfactory.pegacorn.petasos.ipc.codecs.IPCPacketDecoderInitializerFactory;
import net.fhirfactory.pegacorn.petasos.model.processingplant.DefaultWorkshopSetEnum;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.EdgeIngresMessagingGatewayWUP;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.component.netty.ServerInitializerFactory;
import org.apache.camel.spi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public abstract class DefaultFHIRBundleFromLadonReceiver extends EdgeIngresMessagingGatewayWUP {

    @Inject
    CamelContext camelCTX;

    @Override
    protected void executePostInitialisationActivities(){
        Registry registry = camelCTX.getRegistry();
        ServerInitializerFactory ipcReceiverFactory = new IPCPacketDecoderInitializerFactory();
        registry.bind("ipcReceiverFactory", ipcReceiverFactory);
    }

    @Override
    public void configure() throws Exception {
        getLogger().info("EdgeIPCReceiverWUPTemplate :: WUPIngresPoint/ingresFeed --> {}", this.ingresFeed());
        getLogger().info("EdgeIPCReceiverWUPTemplate :: WUPEgressPoint/egressFeed --> {}", this.egressFeed());

        if (this.getIngresTopologyEndpointElement() == null) {
            getLogger().error("EdgeIPCReceiverWUPTemplate::configure(): Guru Software Meditation Error --> No Ingres Point Specified to consider!!!");
        }
        getLogger().debug("EdgeIPCReceiverWUPTemplate::configure(): http provider --> {}", this.specifyEndpointComponentDefinition());
        getLogger().debug("EdgeIPCReceiverWUPTemplate::configure(): hostname --> {}", this.getIngresTopologyEndpointElement().getHostname());
        getLogger().debug("EdgeIPCReceiverWUPTemplate::configure(): port --> {}", this.getIngresTopologyEndpointElement().getExposedPort());

        from(this.ingresFeed())
                .routeId(getWupInstanceName()+"-Main")
                .transform(simple("${bodyAs(String)}"))
                .bean(InterProcessingPlantHandoverPacketDecoderBean.class, "handoverPacketDecode(*)")
                .bean(InterProcessingPlantHandoverRegistrationBean.class, "ipcReceiverActivityStart(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .to(ExchangePattern.InOnly, this.egressFeed())
                .bean(InterProcessingPlantHandoverResponseGenerationBean.class, "generateInterProcessingPlantHandoverResponse(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .bean(InterProcessingPlantHandoverResponseEncoderBean.class, "responseEncoder(*)");
    }

    @Override
    protected String specifyEndpointComponentDefinition() {
        return ("netty");
    }

    @Override
    protected String specifyEndpointProtocolLeadout() {
        return ("?serverInitializerFactory=#ipcReceiverFactory&sync=true");
    }

    @Override
    protected String specifyEndpointProtocolLeadIn() {
        return ("://");
    }

    @Override
    protected String specifyEndpointProtocol() {
        return ("tcp");
    }

    @Override
    protected String specifyWUPWorkshop() {
        return (DefaultWorkshopSetEnum.EDGE_WORKSHOP.getWorkshop());
    }
}
