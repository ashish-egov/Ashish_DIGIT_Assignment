package digit.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.DeathRegistrationService;
import digit.util.ResponseInfoFactory;
import digit.web.models.*;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Tag;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2022-07-26T12:39:05.988+05:30")
@Slf4j
@ToString
@Controller
@RequestMapping("/death-services")
public class V1ApiController{

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private DeathRegistrationService deathRegistrationService;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    public V1ApiController(ObjectMapper objectMapper, HttpServletRequest request, DeathRegistrationService deathRegistrationService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.deathRegistrationService = deathRegistrationService;
    }

    @RequestMapping(value="/v1/registration/_create", method = RequestMethod.POST)
    public ResponseEntity<DeathRegistrationResponse> v1RegistrationCreatePost(@ApiParam(value = "Details for the new Death Registration Application(s) + RequestInfo meta data." ,required=true )  @Valid @RequestBody DeathRegistrationRequest deathRegistrationRequest) {
        List<DeathRegistrationApplication> applications = deathRegistrationService.registerDtRequest(deathRegistrationRequest);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(deathRegistrationRequest.getRequestInfo(), true);
        DeathRegistrationResponse response = DeathRegistrationResponse.builder().deathRegistrationApplications(applications).responseInfo(responseInfo).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value="/v1/registration/_search", method = RequestMethod.POST)
    public ResponseEntity<DeathRegistrationResponse> v1RegistrationSearchPost(@RequestBody RequestInfoWrapper requestInfoWrapper, @Valid @ModelAttribute DeathApplicationSearchCriteria deathApplicationSearchCriteria) {
        List<DeathRegistrationApplication> applications = deathRegistrationService.searchDtApplications(requestInfoWrapper.getRequestInfo(), deathApplicationSearchCriteria);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
        DeathRegistrationResponse response = DeathRegistrationResponse.builder().deathRegistrationApplications(applications).responseInfo(responseInfo).build();
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @RequestMapping(value="/v1/registration/_update", method = RequestMethod.POST)
    public ResponseEntity<DeathRegistrationResponse> v1RegistrationUpdatePost(@ApiParam(value = "Details for the new (s) + RequestInfo meta data." ,required=true )  @Valid @RequestBody DeathRegistrationRequest deathRegistrationRequest) {
        DeathRegistrationApplication application = deathRegistrationService.updateDtApplication(deathRegistrationRequest);

        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(deathRegistrationRequest.getRequestInfo(), true);
        DeathRegistrationResponse response = DeathRegistrationResponse.builder().deathRegistrationApplications(Collections.singletonList(application)).responseInfo(responseInfo).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
