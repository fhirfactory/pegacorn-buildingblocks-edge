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
package net.fhirfactory.pegacorn.platform.restfulapi;

import net.fhirfactory.pegacorn.deployment.properties.SystemWideProperties;
import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class PegacornInternalFHIRClientProxy extends HAPIServerSecureProxy {

    protected abstract Logger getLogger();

    String ladonAnswerEndpoint;

    @Inject
    DeploymentTopologyIM topologyProxy;

    @Inject
    SystemWideProperties systemWideProperties;

    protected abstract String specifyFHIRServerSubsystemService();
    protected abstract String specifyFHIRServerProcessingPlant();
    protected abstract String specifyFHIRServerSubsystemName();
    protected abstract String specifyFHIRServerSubsystemVersion();
    protected abstract String specifyFHIRServerServerEndpointName();

    @PostConstruct
    public void initialise(){
        this.ladonAnswerEndpoint = buildLadonAnswerEndpoint();
        newRestfulGenericClient(deriveTargetEndpointDetails());
    }

    
    protected String buildLadonAnswerEndpoint(){
        String endpointString = specifyFHIRServerSubsystemService();
        return(endpointString);
    }

    protected String deriveTargetEndpointDetails(){
        getLogger().debug(".deriveTargetEndpointDetails(): Entry");
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem Name --> {}, Target Subsystem Version --> {}", specifyFHIRServerSubsystemName(), specifyFHIRServerSubsystemVersion());
        NodeElement targetSubsystem = topologyProxy.getNode(specifyFHIRServerSubsystemName(), NodeElementTypeEnum.SUBSYSTEM, specifyFHIRServerSubsystemVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem (NodeElement) --> {}", targetSubsystem);
        NodeElement targetNode;

        switch(targetSubsystem.getResilienceMode()){
            case RESILIENCE_MODE_MULTISITE:
            case RESILIENCE_MODE_KUBERNETES_MULTISITE:
            case RESILIENCE_MODE_KUBERNETES_CLUSTERED:
            case RESILIENCE_MODE_KUBERNETES_STANDALONE: {
                targetNode = topologyProxy.getNode(specifyFHIRServerSubsystemService(), NodeElementTypeEnum.SERVICE, specifyFHIRServerSubsystemVersion());
                break;
            }
            case RESILIENCE_MODE_CLUSTERED:
            case RESILIENCE_MODE_STANDALONE:
            default:{
                targetNode = topologyProxy.getNode(specifyFHIRServerProcessingPlant(), NodeElementTypeEnum.PROCESSING_PLANT, specifyFHIRServerSubsystemVersion());
            }
        }
        getLogger().trace(".deriveTargetEndpointDetails(): targetNode --> {}", targetNode);
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpointName --> {}, targetEndpointVersion --> {}", specifyFHIRServerServerEndpointName(), specifyFHIRServerSubsystemVersion());
        EndpointElement endpoint = topologyProxy.getEndpoint(targetNode, specifyFHIRServerServerEndpointName(), specifyFHIRServerSubsystemVersion());
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
