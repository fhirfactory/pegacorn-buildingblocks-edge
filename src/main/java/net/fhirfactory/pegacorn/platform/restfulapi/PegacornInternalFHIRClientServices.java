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

import org.hl7.fhir.r4.model.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public abstract class PegacornInternalFHIRClientServices extends PegacornInternalFHIRClientProxy {

    /**
     *
     * @param resourceReference
     * @return
     */
    public Bundle findResourceByReference(Reference resourceReference){
        CodeableConcept identifierType = resourceReference.getIdentifier().getType();
        Coding identifierCode = identifierType.getCodingFirstRep();
        String identifierCodeValue = identifierCode.getCode();
//        String identifierSystem = identifierCode.getSystem();

        Bundle response = findResourceByIdentifier(resourceReference.getType(), resourceReference.getIdentifier().getSystem(), identifierCodeValue, resourceReference.getIdentifier().getValue());
        return (response);
    }

    /**
     *
     * @param resourceType
     * @param identifier
     * @return
     */
    public Bundle findResourceByIdentifier(ResourceType resourceType, Identifier identifier){
        CodeableConcept identifierType = identifier.getType();
        Coding identifierCode = identifierType.getCodingFirstRep();
        String identifierCodeValue = identifierCode.getCode();
//        String identifierSystem = identifierCode.getSystem();

        Bundle response = findResourceByIdentifier(resourceType.getPath(), identifier.getSystem(), identifierCodeValue, identifier.getValue());
        return (response);
    }

    /**
     *
     * @param resourceType
     * @param identifier
     * @return
     */
    public Bundle findResourceByIdentifier(String resourceType, Identifier identifier){
        CodeableConcept identifierType = identifier.getType();
        Coding identifierCode = identifierType.getCodingFirstRep();
        String identifierCodeValue = identifierCode.getCode();
//        String identifierSystem = identifierCode.getSystem();

        Bundle response = findResourceByIdentifier(resourceType, identifier.getSystem(), identifierCodeValue, identifier.getValue());
        return (response);
    }

    /**
     *
     * @param resourceType
     * @param identifierSystem
     * @param identifierCode
     * @param identifierValue
     * @return
     */
    public Bundle findResourceByIdentifier(ResourceType resourceType, String identifierSystem, String identifierCode, String identifierValue){
        String searchURL = resourceType.getPath() + "?identifier:of_type=" + identifierSystem + "|" + identifierCode + "|" + identifierValue;
        Bundle response = findResourceByIdentifier(resourceType.getPath(), identifierSystem, identifierCode, identifierValue);
        return (response);
    }

    /**
     *
     * @param resourceType
     * @param identifierSystem
     * @param identifierCode
     * @param identifierValue
     * @return
     */
    public Bundle findResourceByIdentifier(String resourceType, String identifierSystem, String identifierCode, String identifierValue){
        String rawSearchString = identifierSystem + /* "|" + identifierCode + */ "|" + identifierValue;
        String urlEncodedString = "identifier=" + URLEncoder.encode(rawSearchString, StandardCharsets.UTF_8);
        String searchURL = resourceType + "?" + urlEncodedString;
        getLogger().info(".findResourceByIdentifier(): URL --> {}", searchURL);
        Bundle response = getClient().search()
                .byUrl(searchURL)
                .returnBundle(Bundle.class)
                .execute();
        getLogger().info(".findResourceByIdentifier(): Response Bundle.total --> {}", response.getTotal());
        return (response);
    }
}
