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
package net.fhirfactory.pegacorn.platform.edge.forward;

import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.platform.edge.forward.common.EdgeIPCForwarderWUPTemplate;


import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class BaseEntitiesForwarderWUP extends EdgeIPCForwarderWUPTemplate {

    private static final String WUP_NAME = "FHIR-Base-Entities-Forwarder";
    private static final String WUP_VERSION = "1.0.0";

    @Override
    public Set<TopicToken> specifySubscriptionTopics() {
        HashSet<TopicToken> myTopicSet = new HashSet<TopicToken>();
        String[] entityNames = {
                "Organization",
                "OrganizationAffiliation",
                "HealthcareService",
                "Endpoint",
                "Location",
                "Substance",
                "BiologicallyDerivedProduct",
                "Device",
                "DeviceMetric" };
        String version = "4.0.1";
        for(String entityName: entityNames){
            TopicToken topicId = getFHIRTopicIDBuilder().createTopicToken(entityName, version);
            myTopicSet.add(topicId);
        }
        return(myTopicSet);
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
    protected String specifyEgressTopologyEndpointName() {
        return (getSubsystemComponentNamesService().getEdgeForwardBaseEntitiesEndpointName());
    }

    @Override
    protected String specifyTargetSubsystem() {
        return (getSubsystemComponentNamesService().getLadonSubsystemDefault());
    }

    @Override
    protected String specifyTargetSubsystemVersion() {
        return ("1.0.0");
    }

    @Override
    protected String specifyTargetEndpointName() {
        return (getSubsystemComponentNamesService().getEdgeReceiveBaseEntitiesEndpointName());
    }

    @Override
    protected String specifyTargetEndpointVersion() {
        return ("1.0.0");
    }
}
