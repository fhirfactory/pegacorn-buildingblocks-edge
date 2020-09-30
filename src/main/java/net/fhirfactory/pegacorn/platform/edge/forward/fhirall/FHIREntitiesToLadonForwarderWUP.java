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

import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.platform.edge.forward.common.EdgeIPCForwarderWUPTemplate;

import java.util.HashSet;
import java.util.Set;

public abstract class FHIREntitiesToLadonForwarderWUP extends EdgeIPCForwarderWUPTemplate {

    @Override
    public Set<TopicToken> specifySubscriptionTopics() {
        HashSet<TopicToken> myTopicSet = new HashSet<TopicToken>();
        String[] entityNames = {
                "Organization", "OrganizationAffiliation", "HealthcareService", "Endpoint", "Location", "Substance", "BiologicallyDerivedProduct", "Device", "DeviceMetric",
                "Patient", "Practitioner", "PractitionerRole", "RelatedPerson", "Person", "Group",
                "Encounter", "EpisodeOfCare", "Flag", "List", "Library",
                "Task", "Appointment", "AppointmentResponse", "Schedule", "Slot", "Verification",
                "CarePlan", "CareTeam", "Goal", "ServiceRequest", "NutritionOrder", "VisionPrescription", "RiskAssessment", "RequestGroup",
                "Observation", "Media", "DiagnosticReport", "Specimen", "BodyStructure", "ImagingStudy", "QuestionnaireResponse", "MolecularSequence",
                "MedicationRequest", "MedicationAdministration", "MedicationDispense", "MedicationStatement", "Medication", "MedicationKnowledge", "Immunization", "ImmunizationEvaluation", "ImmunizationRecommendation",
                "AllergyIntolerance", "AdverseEvent", "Condition", "Procedure", "FamilyMemberHistory", "ClinicalImpression", "DetectedIssue",
                "Claim", "ClaimResponse", "Invoice",
                "Account", "ChargeItem", "ChargeItemDefinition", "Contract", "ExplanationOfBenefit","InsurancePlan",
                "Coverage","CoverageEligibilityRequest","CoverageEligibilityResponse","EnrollmentRequest","EnrollmentResponse",
                "Composition", "DocumentManifest", "DocumentReference", "CatalogEntry",
                "Basic", "Binary", "Bundle", "Linkage", "MessageHeader", "OperationOutcome", "Parameters", "Subscription",
                "CodeSystem","ValueSet","ConceptMap","NamingSystem","TerminologyCapabilities"
        };
        String version = "4.0.1";
        for (String entityName : entityNames) {
            TopicToken topicId = getFHIRTopicIDBuilder().createTopicToken(entityName, version);
            myTopicSet.add(topicId);
        }
        return (myTopicSet);
    }
}
