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
package net.fhirfactory.pegacorn.platform.hapifhir.clients;

import net.fhirfactory.pegacorn.deployment.names.PegacornLadonComponentNames;
import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;
import net.fhirfactory.pegacorn.petasos.model.topology.EndpointElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElement;
import net.fhirfactory.pegacorn.petasos.model.topology.NodeElementTypeEnum;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class JPAServerSecureAccessor extends JPAServerSecureProxy {

    @Inject
    DeploymentTopologyIM deploymentTopologyIM;

    @Inject
    PegacornLadonComponentNames pegacornComponentNames;

    public JPAServerSecureAccessor(){
        super();
    }

    protected abstract String specifyTopologySubsystemService();
    protected abstract String specifyTopologySubsystemProcessingPlant();
    protected abstract String specifyTopologySubsystemName();

    protected abstract String specifyTopologySubsystemVersion();
    protected abstract String specifyTopologyJPAServerEndpointName();

    private final String PEGACORN_FHIR_RESOURCE_PATH = "/fhir/r4/";

    @PostConstruct
    public void initialise(){
        newRestfulGenericClient(deriveTargetEndpointDetails());
    }

    protected String deriveTargetEndpointDetails(){
        getLogger().debug(".deriveTargetEndpointDetails(): Entry");
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem Name --> {}, Target Subsystem Version --> {}", specifyTopologySubsystemName(), specifyTopologySubsystemVersion());
        NodeElement targetSubsystem = deploymentTopologyIM.getNode(specifyTopologySubsystemName(), NodeElementTypeEnum.SUBSYSTEM, specifyTopologySubsystemVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): Target Subsystem (NodeElement) --> {}", targetSubsystem);
        NodeElement targetNode;

        switch(targetSubsystem.getResilienceMode()){
            case RESILIENCE_MODE_MULTISITE:
            case RESILIENCE_MODE_KUBERNETES_MULTISITE:
            case RESILIENCE_MODE_KUBERNETES_CLUSTERED:
            case RESILIENCE_MODE_KUBERNETES_STANDALONE: {
                targetNode = deploymentTopologyIM.getNode(specifyTopologySubsystemService(), NodeElementTypeEnum.SERVICE, specifyTopologySubsystemVersion());
                break;
            }
            case RESILIENCE_MODE_CLUSTERED:
            case RESILIENCE_MODE_STANDALONE:
            default:{
                targetNode = deploymentTopologyIM.getNode(specifyTopologySubsystemProcessingPlant(), NodeElementTypeEnum.PROCESSING_PLANT, specifyTopologySubsystemVersion());
            }
        }
        getLogger().trace(".deriveTargetEndpointDetails(): targetNode --> {}", targetNode);
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpointName --> {}, targetEndpointVersion --> {}", specifyTopologyJPAServerEndpointName(), specifyTopologySubsystemVersion());
        EndpointElement endpoint = deploymentTopologyIM.getEndpoint(targetNode, specifyTopologyJPAServerEndpointName(), specifyTopologySubsystemVersion());
        getLogger().trace(".deriveTargetEndpointDetails(): targetEndpoint (EndpointElement) --> {}", endpoint);
        //TODO make the https conditional based on the deployment topology
        String endpointDetails = "http://"+ endpoint.getHostname() + ":" + endpoint.getExposedPort() + PEGACORN_FHIR_RESOURCE_PATH;
        getLogger().info(".deriveTargetEndpointDetails(): Exit, endpointDetails --> {}", endpointDetails);
        return(endpointDetails);
    }

    public DeploymentTopologyIM getDeploymentTopologyIM() {
        return deploymentTopologyIM;
    }

    public PegacornLadonComponentNames getPegacornComponentNames() {
        return pegacornComponentNames;
    }
}