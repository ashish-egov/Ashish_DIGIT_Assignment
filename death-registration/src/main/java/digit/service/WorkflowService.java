package digit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.DTRConfiguration;
import digit.repository.ServiceRequestRepository;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class WorkflowService {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private DTRConfiguration config;

    @Autowired
    private WorkflowService workflowService;

    public void updateWorkflowStatus(DeathRegistrationRequest deathRegistrationRequest) {
        deathRegistrationRequest.getDeathRegistrationApplications().forEach(application -> {
            if (application.getWorkflow() != null) {
                updateWorkflowStatusFromProcessInstance(application, deathRegistrationRequest);
            } else {
                updateWorkflowStatusFromCurrentWorkflow(application, deathRegistrationRequest);
            }
        });
    }

    private void updateWorkflowStatusFromProcessInstance(DeathRegistrationApplication application, DeathRegistrationRequest deathRegistrationRequest) {
        ProcessInstance processInstance = getProcessInstanceForDTR(application, deathRegistrationRequest.getRequestInfo());
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(deathRegistrationRequest.getRequestInfo(), Collections.singletonList(processInstance));
        application.getWorkflow().setWorkflowStatus(callWorkFlow(workflowRequest).getState());
        updateStatusBasedOnWorkflowStatus(application.getWorkflow());
    }

    private void updateWorkflowStatusFromCurrentWorkflow(DeathRegistrationApplication application, DeathRegistrationRequest deathRegistrationRequest) {
        ProcessInstance obj = workflowService.getCurrentWorkflow(deathRegistrationRequest.getRequestInfo(), application.getTenantId(), application.getApplicationNumber());
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


    public State callWorkFlow(ProcessInstanceRequest workflowReq) {

        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(config.getWfHost().concat(config.getWfTransitionPath()));
        Object optional = repository.fetchResult(url, workflowReq);
        response = mapper.convertValue(optional, ProcessInstanceResponse.class);
        return response.getProcessInstances().get(0).getState();
    }

    private ProcessInstance getProcessInstanceForDTR(DeathRegistrationApplication application, RequestInfo requestInfo) {
        Workflow workflow = application.getWorkflow();

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setBusinessId(application.getApplicationNumber());
        processInstance.setAction(workflow.getAction());
        processInstance.setModuleName("death-services");
        processInstance.setTenantId(application.getTenantId());
        processInstance.setBusinessService("DTR");
        processInstance.setDocuments(workflow.getDocuments());
        processInstance.setComment(workflow.getComments());

        if(!CollectionUtils.isEmpty(workflow.getAssignes())){
            List<User> users = new ArrayList<>();

            workflow.getAssignes().forEach(uuid -> {
                User user = new User();
                user.setUuid(uuid);
                users.add(user);
            });

            processInstance.setAssignes(users);
        }

        return processInstance;

    }

    public ProcessInstance getCurrentWorkflow(RequestInfo requestInfo, String tenantId, String applicationNumber) {

        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

        StringBuilder url = getSearchURLWithParams(tenantId, applicationNumber);

        Object res = repository.fetchResult(url, requestInfoWrapper);
        ProcessInstanceResponse response = null;

        try{
            response = mapper.convertValue(res, ProcessInstanceResponse.class);
        }
        catch (Exception e){
            throw new CustomException("PARSING_ERROR","Failed to parse workflow search response");
        }

        if(response!=null && response.getProcessInstances()!=null && !CollectionUtils.isEmpty(response.getProcessInstances()) && response.getProcessInstances().get(0)!=null)
        {
            return response.getProcessInstances().get(0);
        }


        return null;
    }

    private StringBuilder getSearchURLWithParams(String tenantId, String applicationNumber) {

        StringBuilder url = new StringBuilder(config.getWfHost());
        url.append(config.getWfProcessInstanceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&applicationNumber=");
        url.append(applicationNumber);
        return url;
    }

}