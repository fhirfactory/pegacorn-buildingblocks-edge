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
package net.fhirfactory.pegacorn.platform.edge.forward.fhirall;

import java.util.HashSet;
import java.util.Set;

import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.platform.edge.forward.common.EdgeIPCForwarderWUPTemplate;

public abstract class DefaultFHIRBundleToLadonForwarder extends EdgeIPCForwarderWUPTemplate {

    @Override
    public Set<TopicToken> specifySubscriptionTopics() {
        getLogger().debug(".specifySubscriptionTopics(): Entry");
        HashSet<TopicToken> myTopicSet = new HashSet<TopicToken>();
        TopicToken topicId = getFHIRTopicIDBuilder().createTopicToken("Bundle", "4.0.1");
        topicId.addDescriminator("Destination", specifyTargetService());
        myTopicSet.add(topicId);
        getLogger().debug(".specifySubscriptionTopics(): Exit, added TopicToken --> {}", topicId);
        return (myTopicSet);
    }

}
