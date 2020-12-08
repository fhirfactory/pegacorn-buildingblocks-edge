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

import net.fhirfactory.pegacorn.datasets.fhir.r4.base.entities.bundle.BundleContentHelper;
import org.hl7.fhir.r4.model.*;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public abstract class PegacornInternalFHIRClientServices extends PegacornInternalFHIRClientProxy {

    @Inject
    private BundleContentHelper bundleContentHelper;

    /**
     *
     * @param resourceReference
     * @return
     */
    public Resource findResourceByReference(Reference resourceReference){
        CodeableConcept identifierType = resourceReference.getIdentifier().getType();
        Coding identifierCode = identifierType.getCodingFirstRep();
        String identifierCodeValue = identifierCode.getCode();
//        String identifierSystem = identifierCode.getSystem();

        Resource response = findResourceByIdentifier(resourceReference.getType(), resourceReference.getIdentifier().getSystem(), identifierCodeValue, resourceReference.getIdentifier().getValue());
        return (response);
    }

    /**
     *
     * @param resourceType
     * @param identifier
     * @return
     */
    public Resource findResourceByIdentifier(ResourceType resourceType, Identifier identifier){
        CodeableConcept identifierType = identifier.getType();
        Coding identifierCode = identifierType.getCodingFirstRep();
        String identifierCodeValue = identifierCode.getCode();
        String identifierSystem = identifierCode.getSystem();
        String identifierValue = identifier.getValue();

        Resource response = findResourceByIdentifier(resourceType.toString(), identifierSystem, identifierCodeValue, identifierValue);
        return (response);
    }

    /**
     *
     * @param resourceType
     * @param identifier
     * @return
     */
    public Resource findResourceByIdentifier(String resourceType, Identifier identifier){
        String identifierValue = identifier.getValue();
        String identifierSystem = null;
        String identifierCode = null;
        if(identifier.hasType()) {
            CodeableConcept identifierType = identifier.getType();
            Coding identifierTypeCode = identifierType.getCodingFirstRep();
            identifierCode = identifierTypeCode.getCode();
            identifierSystem = identifierTypeCode.getSystem();
        } else {
            identifierSystem = identifier.getSystem();
        }
        Resource response = findResourceByIdentifier(resourceType, identifierSystem, identifierCode, identifierValue);
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
    public Resource findResourceByIdentifier(ResourceType resourceType, String identifierSystem, String identifierCode, String identifierValue){
        Resource response = findResourceByIdentifier(resourceType.toString(), identifierSystem, identifierCode, identifierValue);
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
    public Resource findResourceByIdentifier(String resourceType, String identifierSystem, String identifierCode, String identifierValue){
        getLogger().debug(".findResourceByIdentifier(): Entry, resourceType --> {}, identfierSystem --> {}, identifierCode --> {}, identifierValue -->{}", resourceType, identifierSystem, identifierCode, identifierValue);
        String urlEncodedString = null;
        if(identifierCode == null ) {
            String rawSearchString = identifierSystem + /* "|" + identifierCode + */ "|" + identifierValue;
            urlEncodedString = "identifier=" + URLEncoder.encode(rawSearchString, StandardCharsets.UTF_8);
        } else {
            String rawSearchString = identifierSystem + "|" + identifierCode + "|" + identifierValue;
            urlEncodedString = "identifier:of_type=" + URLEncoder.encode(rawSearchString, StandardCharsets.UTF_8);
        }
        String searchURL = resourceType + "?" + urlEncodedString;
        getLogger().info(".findResourceByIdentifier(): URL --> {}", searchURL);
        Bundle response = getClient().search()
                .byUrl(searchURL)
                .returnBundle(Bundle.class)
                .execute();
        Resource resource = bundleContentHelper.extractFirstRepOfType(response, resourceType);
        getLogger().debug(".findResourceByIdentifier(): Retrieved Resource --> {}", resource);
        return (resource);
    }
}
