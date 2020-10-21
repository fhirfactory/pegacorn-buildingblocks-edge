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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.deployment.topology.manager.DeploymentTopologyIM;


public abstract class LadonAnswerService extends PegacornHapiFhirProxy {

    protected abstract Logger getLogger();

    String ladonAnswerEndpoint;

    @Inject
    DeploymentTopologyIM topologyProxy;

    protected abstract String specifyLadonService();


    @PostConstruct
    public void initialise(){
        this.ladonAnswerEndpoint = buildLadonAnswerEndpoint();
        newRestfulGenericClient(getLadonAnswerEndpoint());
    }

    
    protected String buildLadonAnswerEndpoint(){
        String endpointString = specifyLadonService();
        return(endpointString);
    }

    public String getLadonAnswerEndpoint(){
        // this.ladonAnswerEndpoint
        String endpoint = "http://10.10.10.20"
        return(
                //t);
    }
}
