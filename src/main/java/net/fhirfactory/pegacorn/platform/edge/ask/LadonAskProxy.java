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
package net.fhirfactory.pegacorn.platform.edge.ask;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.deployment.properties.SystemWideProperties;
import org.slf4j.Logger;

import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;

public abstract class LadonAskProxy extends PegacornHapiFhirProxy {

    protected abstract Logger getLogger();

    String ladonAnswerEndpoint;

    @Inject
    DeploymentTopologyIM topologyProxy;

    @Inject
    SystemWideProperties systemWideProperties;

    protected abstract String specifyLadonService();
    protected abstract String specifyLadonProcessingPlant();
    protected abstract String specifyLadonSubsystemName();
    protected abstract String specifyLadonSubsystemVersion();
    protected abstract String specifyLadonAskEndpointName();

    @PostConstruct
    public void initialise(){
        this.ladonAnswerEndpoint = buildLadonAnswerEndpoint();
        newRestfulGenericClient(deriveTargetEndpointDetails());
    }

    
    protected String buildLadonAnswerEndpoint(){
        String endpointString = specifyLadonService();
        return(endpointString);
    }

    protected String deriveTargetEndpointDetails(){
        getLogger().debug(".deriveTargetEndpointDetails(): Entry");
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem Name --> {}, Target Subsystem Version --> {}", specifyLadonSubsystemName(), specifyLadonSubsystemVersion());
        NodeElement targetSubsystem = topologyProxy.getNode(specifyLadonSubsystemName(), NodeElementTypeEnum.SUBSYSTEM, specifyLadonSubsystemVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem (NodeElement) --> {}", targetSubsystem);
        NodeElement targetNode;

        switch(targetSubsystem.getResilienceMode()){
            case RESILIENCE_MODE_MULTISITE:
            case RESILIENCE_MODE_KUBERNETES_MULTISITE:
            case RESILIENCE_MODE_KUBERNETES_CLUSTERED:
            case RESILIENCE_MODE_KUBERNETES_STANDALONE: {
                targetNode = topologyProxy.getNode(specifyLadonService(), NodeElementTypeEnum.SERVICE, specifyLadonSubsystemVersion());
                break;
            }
            case RESILIENCE_MODE_CLUSTERED:
            case RESILIENCE_MODE_STANDALONE:
            default:{
                targetNode = topologyProxy.getNode(specifyLadonProcessingPlant(), NodeElementTypeEnum.PROCESSING_PLANT, specifyLadonSubsystemVersion());
            }
        }
        getLogger().trace(".deriveTargetEndpointDetails(): targetNode --> {}", targetNode);
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpointName --> {}, targetEndpointVersion --> {}", specifyLadonAskEndpointName(), specifyLadonSubsystemVersion());
        EndpointElement endpoint = topologyProxy.getEndpoint(targetNode, specifyLadonAskEndpointName(), specifyLadonSubsystemVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpoint (EndpointElement) --> {}", endpoint);
        String http_type = null;
        if(endpoint.isUtilisingSecurity()) {
            http_type = "https";
        } else {
            http_type = "http";
        }
        String endpointDetails = http_type + "://" + endpoint.getHostname() + ":" + endpoint.getExposedPort() + systemWideProperties.getPegacornInternalFhirResourceR4Path();
        getLogger().info(".deriveTargetEndpointDetails(): Exit, endpointDetails --> {}", endpointDetails);
        return(endpointDetails);
    }
}
