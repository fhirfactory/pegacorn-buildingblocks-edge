package net.fhirfactory.pegacorn.platform.edge.receive.common;

import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverPacketDecoderBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverRegistrationBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverResponseEncoderBean;
import net.fhirfactory.pegacorn.petasos.ipc.beans.receiver.InterProcessingPlantHandoverResponseGenerationBean;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.EdgeIngresMessagingGatewayWUP;
import org.apache.camel.ExchangePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FHIREntitiesFromLadonReceiverWUP extends EdgeIngresMessagingGatewayWUP {
    private static final Logger LOG = LoggerFactory.getLogger(FHIREntitiesFromLadonReceiverWUP.class);

    @Override
    public void configure() throws Exception {
        LOG.info("EdgeIPCReceiverWUPTemplate :: WUPIngresPoint/ingresFeed --> {}", this.ingresFeed());
        LOG.info("EdgeIPCReceiverWUPTemplate :: WUPEgressPoint/egressFeed --> {}", this.egressFeed());

        if (this.getIngresTopologyEndpointElement() == null) {
            LOG.error("EdgeIPCReceiverWUPTemplate::configure(): Guru Software Meditation Error --> No Ingres Point Specified to consider!!!");
        }
        LOG.debug("EdgeIPCReceiverWUPTemplate::configure(): http provider --> {}", this.specifyEndpointComponentDefinition());
        LOG.debug("EdgeIPCReceiverWUPTemplate::configure(): hostname --> {}", this.getIngresTopologyEndpointElement().getHostname());
        LOG.debug("EdgeIPCReceiverWUPTemplate::configure(): port --> {}", this.getIngresTopologyEndpointElement().getExposedPort());

        from(this.ingresFeed())
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
        return ("?serverInitializerFactory=#ipcReceiverFactory");
    }

    @Override
    protected String specifyEndpointProtocolLeadIn() {
        return ("://");
    }

    @Override
    protected String specifyEndpointProtocol() {
        return ("tcp");
    }

    /*
        @Override
        protected String specifyIngresEndpointPayloadEncapsulationType() {
            return ("JSON");
        }
    */

    @Override
    protected String specifyWUPWorkshop() {
        return ("Edge");
    }
}
