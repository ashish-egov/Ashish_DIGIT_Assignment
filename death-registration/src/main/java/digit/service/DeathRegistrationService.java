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

        // Push the application to the topic for persister to listen and persist
        producer.push("save-dt-application", deathRegistrationRequest);

        // Return the response back to user
        return deathRegistrationRequest.getDeathRegistrationApplications();
    }

    public List<DeathRegistrationApplicationSearch> searchDtApplications(RequestInfo requestInfo, DeathApplicationSearchCriteria deathApplicationSearchCriteria) {
        // Fetch applications from database according to the given search criteria
        List<DeathRegistrationApplicationSearch> applications = deathRegistrationRepository.getApplicationsSearch(deathApplicationSearchCriteria);

        // If no applications are found matching the given criteria, return an empty list
        if (CollectionUtils.isEmpty(applications)) {
            return new ArrayList<>();
        }

        // Enrich mother and father of applicant objects
        applications.forEach(application -> {
            enrichmentUtil.enrichApplicantOnSearch(application, deathApplicationSearchCriteria);
        });

        //WORKFLOW INTEGRATION
        applications.forEach(application -> {
            updateWorkflowData(requestInfo, application);
        });

        // Otherwise return the found applications
        return applications;
    }

    private void updateWorkflowData(RequestInfo requestInfo, DeathRegistrationApplicationSearch application) {
        ProcessInstance obj = workflowService.getCurrentWorkflow(requestInfo, application.getTenantId(), application.getApplicationNumber());
        application.setWorkflow(Workflow.builder().workflowStatus(obj.getState().getState()).build());
        application.getWorkflow().setComments(obj.getComment());
        application.getWorkflow().setAction(obj.getAction());
        application.getWorkflow().setDocuments(obj.getDocuments());
        updateStatusBasedOnWorkflowStatus(application.getWorkflow());

        List<User> assignees = obj.getAssignes();
        List<String> uuidStrings = new ArrayList<>();

        if (assignees != null && !assignees.isEmpty()) {
            for (User user : assignees) {
                uuidStrings.add(user.getUuid());
            }
        }

        application.getWorkflow().setAssignes(uuidStrings);
    }



    public void updateStatusBasedOnWorkflowStatus(Workflow workflow) {
        String workflowStatus = workflow.getWorkflowStatus();
        if (workflowStatus.equals("APPLIED")) {
            workflow.setStatus("INW");
        } else if (workflowStatus.equals("REJECTED")) {
            workflow.setStatus("INACTIVE");
        } else {
            workflow.setStatus("ACTIVE");
        }
    }

    public DeathRegistrationApplication updateDtApplication(DeathRegistrationRequest deathRegistrationRequest) {
        // Validate whether the application that is being requested for update indeed exists
        DeathRegistrationApplication existingApplication = validator.validateApplicationExistence(deathRegistrationRequest.getDeathRegistrationApplications().get(0));
        // Enrich application upon update
        enrichmentUtil.enrichDeathApplicationUponUpdate(deathRegistrationRequest);

        workflowService.updateWorkflowStatus(deathRegistrationRequest);

        // Just like create request, update request will be handled asynchronously by the persisted
        producer.push("update-dt-application", deathRegistrationRequest);

        return deathRegistrationRequest.getDeathRegistrationApplications().get(0);
    }
}
