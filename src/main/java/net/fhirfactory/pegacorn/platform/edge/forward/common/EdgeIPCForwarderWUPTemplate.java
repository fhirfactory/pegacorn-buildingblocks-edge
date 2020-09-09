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


import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import net.fhirfactory.pegacorn.petasos.wup.archetypes.InteractAPICamelRestClientGatewayWUP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EdgeIPCForwarderWUPTemplate extends InteractAPICamelRestClientGatewayWUP {
    private static final Logger LOG = LoggerFactory.getLogger(EdgeIPCForwarderWUPTemplate.class);

    @Override
    public void configure() throws Exception {

    }

    @Override
    protected String specifyEgressEndpointRESTProviderComponent() {
        return("netty-http");
    }

    @Override
    protected String specifyEgressEndpointPayloadEncapsulationType() {
        return("http");
    }

    @Override
    protected String specifyEgressEndpointScheme() {
        return("https");
    }

    @Override
    protected String specifyEgressEndpointContextPath() {
        return("/pegacorn/ipc/edge/");
    }

    @Override
    protected boolean isRemote(){
        return(true);
    }

    @Override
    protected String specifyEgressEndpointName() {
        return(deriveTargetEndpointDetails());
    }

    @Override
    protected String specifyEgressEndpointVersion() {
        return(specifyTargetEndpointVersion());
    }

    @Override
    protected String specifyEgressEndpoint(){
        LOG.debug(".specifyEgressEndpoint(): Entry");
        String egressEndPoint;
        egressEndPoint = specifyEgressEndpointRESTProviderComponent();
        egressEndPoint = egressEndPoint + ":";
        egressEndPoint = egressEndPoint + this.specifyEgressEndpointPayloadEncapsulationType();
        egressEndPoint = egressEndPoint + this.specifyEgressEndpointScheme();
        egressEndPoint = egressEndPoint + this.deriveTargetEndpointDetails();
        egressEndPoint = egressEndPoint + specifyEgressEndpointContextPath();
        LOG.debug(".specifyIngresEndpoint(): Exit, egressEndPoint --> {}", egressEndPoint);
        return(egressEndPoint);
    }

    protected abstract String specifyTargetSubsystem();
    protected abstract String specifyTargetSubsystemVersion();
    protected abstract String specifyTargetEndpointName();
    protected abstract String specifyTargetEndpointVersion();

    private String deriveTargetEndpointDetails(){
        LOG.debug(".deriveTargetEndpointDetails(): Entry");
        NodeElement targetSubsystem = getTopologyServer().getNode(specifyTargetEndpointName(), NodeElementTypeEnum.SUBSYSTEM, specifyTargetSubsystemVersion());
        NodeElement targetNode;
        switch(targetSubsystem.getResilienceMode()){
            case RESILIENCE_MODE_MULTISITE:
            case RESILIENCE_MODE_CLUSTERED:{
                targetNode = getTopologyServer().getNode(specifyTargetEndpointName(), NodeElementTypeEnum.SERVICE, specifyTargetSubsystemVersion());
                break;
            }
            case RESILIENCE_MODE_STANDALONE:
            default:{
                targetNode = getTopologyServer().getNode(specifyTargetEndpointName(), NodeElementTypeEnum.PROCESSING_PLANT, specifyTargetSubsystemVersion());
            }
        }
        EndpointElement endpoint = getTopologyServer().getEndpoint(targetNode, specifyTargetEndpointName(), specifyTargetEndpointVersion());
        String endpointDetails = endpoint.getHostname() + ":" + endpoint.getExposedPort();
        LOG.debug(".deriveTargetEndpointDetails(): Exit");
        return(endpointDetails);
    }

    @Override
    protected String specifyWUPWorkshop(){
        return("Edge");
    }

}
