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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.slf4j.Logger;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import net.fhirfactory.pegacorn.util.FhirUtil;
import net.fhirfactory.pegacorn.util.PegacornProperties;

public abstract class PegacornHapiFhirProxy {
    public static final String API_KEY_HEADER_NAME = "x-api-key";
    private IGenericClient client;

    protected abstract Logger getLogger();
    
    /**
     * @return the name of the PegacornProperties to lookup to get the value of the API Key.  Subclasses can override
     *         if they don't want to use the default value of API_KEY
     */
    protected String getApiKeyPropertyName() {
        return "API_KEY";
    }

    protected IGenericClient newRestfulGenericClient(String theServerBase) {
        getLogger().info(".newRestfulGenericClient(): Entry, theServerBase --> {}", theServerBase);
        FhirContext contextR4 = FhirUtil.getInstance().getFhirContext();
        contextR4.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        client = contextR4.newRestfulGenericClient(theServerBase);

//        String apiKey = PegacornProperties.getMandatoryProperty(getApiKeyPropertyName());
        // From https://hapifhir.io/hapi-fhir/docs/interceptors/built_in_client_interceptors.html#misc-add-headers-to-request
//        AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
//        interceptor.addHeaderValue(API_KEY_HEADER_NAME, apiKey);
//        client.registerInterceptor(interceptor);
        
        return client;
    }

    public IGenericClient getClient(){
        return client;
    }
}
