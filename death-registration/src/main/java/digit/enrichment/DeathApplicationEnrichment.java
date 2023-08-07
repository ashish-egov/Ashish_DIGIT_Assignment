package digit.enrichment;

import digit.service.UserService;
import digit.util.IdgenUtil;
import digit.util.UserUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Component
@Slf4j
public class DeathApplicationEnrichment {

    @Autowired
    private IdgenUtil idgenUtil;

    @Autowired
    private UserUtil userUtils;

    @Autowired
    private UserService userService;

    public void enrichDeathApplication(DeathRegistrationRequest deathRegistrationRequest) {
        List<String> deathRegistrationIdList = idgenUtil.getIdList(deathRegistrationRequest.getRequestInfo(), deathRegistrationRequest.getDeathRegistrationApplications().get(0).getTenantId(), "dtr.registrationid", "", deathRegistrationRequest.getDeathRegistrationApplications().size());
        Integer index = 0;
        for(DeathRegistrationApplication application : deathRegistrationRequest.getDeathRegistrationApplications()){
            // Enrich audit details
            AuditDetails auditDetails = AuditDetails.builder().createdBy(deathRegistrationRequest.getRequestInfo().getUserInfo().getUuid()).createdTime(System.currentTimeMillis()).lastModifiedBy(deathRegistrationRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();
            application.setAuditDetails(auditDetails);

            // Enrich UUID
            application.setId(UUID.randomUUID().toString());

            // Enrich registration Id
            application.getAddressOfDeceased().setRegistrationId(application.getId());

            // Enrich address UUID
            application.getAddressOfDeceased().setId(UUID.randomUUID().toString());

            //Enrich application number from IDgen
            application.setApplicationNumber(deathRegistrationIdList.get(index++));

        }
    }

    public void enrichDeathApplicationUponUpdate(DeathRegistrationRequest deathRegistrationRequest) {
//         Enrich lastModifiedTime and lastModifiedBy in case of update
        deathRegistrationRequest.getDeathRegistrationApplications().get(0).getAuditDetails().setLastModifiedTime(System.currentTimeMillis());
        deathRegistrationRequest.getDeathRegistrationApplications().get(0).getAuditDetails().setLastModifiedBy(deathRegistrationRequest.getRequestInfo().getUserInfo().getUuid());
    }

    public void enrichApplicantOnSearch(DeathRegistrationApplicationSearch application, DeathApplicationSearchCriteria deathApplicationSearchCriteria){
        UserDetailResponse userDetailResponse=userService.searchUser(userUtils.getStateLevelTenant(deathApplicationSearchCriteria.getTenantId()) , application.getApplicantId(), null,application.getApplicantUuid(),application.getApplicantType());
        application.setApplicant(convertUserToApplicant(userDetailResponse.getUser().get(0)));
    }

    public Applicant convertUserToApplicant(User user) {
        Applicant applicant = new Applicant();
        applicant.setId(user.getId());
        applicant.setUuid(user.getUuid());
        applicant.setUserName(user.getUserName());
        applicant.setName(user.getName());
        applicant.setGender(user.getGender());
        applicant.setMobileNumber(user.getMobileNumber());
        applicant.setEmailId(user.getEmailId());
        applicant.setAltContactNumber(user.getAltContactNumber());
        applicant.setActive(user.getActive());
        applicant.setAccountLocked(user.getAccountLocked());
        applicant.setTenantId(user.getTenantId());
        applicant.setType(user.getType());

        // Copy roles only if present in User and Applicant classes
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<Role> rolesToCopy = new ArrayList<>();
            for (Role userRole : user.getRoles()) {
                Role role = new Role();
                role.setCode(userRole.getCode());
                role.setName(userRole.getName());
                role.setTenantId(userRole.getTenantId());
                rolesToCopy.add(role);
            }
            applicant.setRoles(rolesToCopy);
        }

        // Additional fields in the Applicant class that are not present in the User class
        // will be ignored (since they are not set).

        return applicant;
    }

}