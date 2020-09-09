package net.fhirfactory.pegacorn.platform.edge.receive;

import net.fhirfactory.pegacorn.petasos.model.ipc.IPCTransportPacket;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.InteractAPICamelRestPOSTGatewayWUP;
import net.fhirfactory.pegacorn.petasos.wup.helper.IngresActivityBeginRegistration;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FromLadonReceiverWUP extends InteractAPICamelRestPOSTGatewayWUP {
    private static final Logger LOG = LoggerFactory.getLogger(FromLadonReceiverWUP.class);
    private static final String WUP_NAME = "Bundles-From-Ladon-Receiver";
    private static final String WUP_VERSION = "1.0.0";


    @Override
    public void configure() throws Exception {
        LOG.info("EdgeIPCReceiverWUPTemplate :: WUPIngresPoint/ingresFeed --> {}", this.ingresFeed());
        LOG.info("EdgeIPCReceiverWUPTemplate :: WUPEgressPoint/egressFeed --> {}", this.egressFeed());

        restConfiguration()
                .component(this.specifyIngresEndpointRESTProviderComponent())
                .host(this.getIngresTopologyEndpointElement().getHostname())
                .port(this.getIngresTopologyEndpointElement().getExposedPort())
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(constant(""));

        rest(this.specifyIngresEndpointContextPath())
                .post().type(IPCTransportPacket.class)
                .route()
                .routeId(getWupInstanceName())
                .bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange," + this.getWupTopologyNodeElement().extractNodeKey() + ")")
                .to(ExchangePattern.InOnly, this.egressFeed())
                .transform().simple("{}");
    }

    @Override
    protected String specifyIngresEndpointRESTProviderComponent() {
        return("netty-http");
    }

    @Override
    protected String specifyIngresEndpointContextPath() {
        return("/pegacorn/ipc/edge/");
    }

    @Override
    protected String specifyIngresEndpoint() {
        return(specifyIngresTopologyEndpointName());
    }

    @Override
    protected String specifyIngresEndpointPayloadEncapsulationType() {
        return ("JSON");
    }

    @Override
    protected String specifyIngresEndpointScheme() {
        return ("http");
    }

    @Override
    public String specifyWUPInstanceName() {
        return (WUP_NAME);
    }

    @Override
    public String specifyWUPVersion() {
        return (WUP_VERSION);
    }

    @Override
    protected String specifyIngresTopologyEndpointName() {
        return (this.getSubsystemComponentNamesService().getEdgeReceiveBaseEntitiesEndpointName());
    }

    @Override
    protected String specifyIngresEndpointVersion() {
        return ("1.0.0");
    }

    @Override
    protected String specifyWUPWorkshop(){
        return("Edge");
    }
}
