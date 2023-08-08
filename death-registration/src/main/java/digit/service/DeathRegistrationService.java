package digit.service;


import digit.enrichment.DeathApplicationEnrichment;
import digit.kafka.Producer;
import digit.repository.DeathRegistrationRepository;
import digit.validators.DeathApplicationValidator;
import digit.web.models.*;
//import digit.web.models.FatherApplicant;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DeathRegistrationService {

    @Autowired
    private DeathApplicationValidator validator;

    @Autowired
    private DeathApplicationEnrichment enrichmentUtil;

    @Autowired
    private UserService userService;
//
    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private DeathRegistrationRepository deathRegistrationRepository;

    @Autowired
    private Producer producer;

    public List<DeathRegistrationApplication> registerDtRequest(DeathRegistrationRequest deathRegistrationRequest) {
        // Validate applications
        validator.validateDeathApplication(deathRegistrationRequest);

        // Enrich applications
        enrichmentUtil.enrichDeathApplication(deathRegistrationRequest);

//        // Enrich/Upsert user in upon death registration
        userService.callUserService(deathRegistrationRequest);
//
        // Initiate workflow for the new application
        workflowService.updateWorkflowStatus(deathRegistrationRequest);

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // Adding a 1-second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        deathRegistrationRequest.getDeathRegistrationApplications().forEach(application -> {
            ProcessInstance obj=workflowService.getCurrentWorkflow(deathRegistrationRequest.getRequestInfo(), application.getTenantId(), application.getApplicationNumber());
            application.setWorkflow(Workflow.builder().status(obj.getState().getState()).build());
        });

        // Push the application to the topic for persister to listen and persist
        producer.push("save-dt-application", deathRegistrationRequest);

        // Return the response back to user
        return deathRegistrationRequest.getDeathRegistrationApplications();
    }

    public List<DeathRegistrationApplicationSearch> searchDtApplications(RequestInfo requestInfo, DeathApplicationSearchCriteria deathApplicationSearchCriteria) {
        // Fetch applications from database according to the given search criteria
        List<DeathRegistrationApplicationSearch> applications = deathRegistrationRepository.getApplicationsSearch(deathApplicationSearchCriteria);

        // If no applications are found matching the given criteria, return an empty list
        if(CollectionUtils.isEmpty(applications))
            return new ArrayList<>();

        // Enrich mother and father of applicant objects
        applications.forEach(application -> {
            enrichmentUtil.enrichApplicantOnSearch(application,deathApplicationSearchCriteria);
//            enrichmentUtil.enrichMotherApplicantOnSearch(application);
        });

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // Adding a 1-second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        //WORKFLOW INTEGRATION
        applications.forEach(application -> {
            ProcessInstance obj=workflowService.getCurrentWorkflow(requestInfo, application.getTenantId(), application.getApplicationNumber());
            application.setWorkflow(Workflow.builder().status(obj.getState().getState()).build());
        });

        // Otherwise return the found applications
        return applications;
    }

    public DeathRegistrationApplication updateDtApplication(DeathRegistrationRequest deathRegistrationRequest) {
        // Validate whether the application that is being requested for update indeed exists
        DeathRegistrationApplication existingApplication = validator.validateApplicationExistence(deathRegistrationRequest.getDeathRegistrationApplications().get(0));

        // Enrich application upon update
        enrichmentUtil.enrichDeathApplicationUponUpdate(deathRegistrationRequest);

        workflowService.updateWorkflowStatus(deathRegistrationRequest);

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // Adding a 1-second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        deathRegistrationRequest.getDeathRegistrationApplications().forEach(application -> {
            ProcessInstance obj=workflowService.getCurrentWorkflow(deathRegistrationRequest.getRequestInfo(), application.getTenantId(), application.getApplicationNumber());
            application.setWorkflow(Workflow.builder().status(obj.getState().getState()).build());
        });

        // Just like create request, update request will be handled asynchronously by the persister
        producer.push("update-dt-application", deathRegistrationRequest);

        return deathRegistrationRequest.getDeathRegistrationApplications().get(0);
    }
}
