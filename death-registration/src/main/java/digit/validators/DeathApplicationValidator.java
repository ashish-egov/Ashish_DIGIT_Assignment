package digit.validators;

import digit.repository.DeathRegistrationRepository;
import digit.web.models.DeathApplicationSearchCriteria;
import digit.web.models.DeathRegistrationApplication;
import digit.web.models.DeathRegistrationRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class DeathApplicationValidator {

    @Autowired
    private DeathRegistrationRepository repository;

    public void validateDeathApplication(DeathRegistrationRequest deathRegistrationRequest) {
        deathRegistrationRequest.getDeathRegistrationApplications().forEach(application -> {
            if(ObjectUtils.isEmpty(application.getTenantId()))
                throw new CustomException("EG_DT_APP_ERR", "tenantId is mandatory for creating death registration applications");
        });
    }

    public DeathRegistrationApplication validateApplicationExistence(DeathRegistrationApplication deathRegistrationApplication) {
        try {
            List<DeathRegistrationApplication> applications = repository.getApplications(DeathApplicationSearchCriteria.builder().applicationNumber(deathRegistrationApplication.getApplicationNumber()).build());
            if (applications.isEmpty()) {
                throw new RuntimeException("No such application exists.");
            }
            return applications.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error while checking application existence: " + e.getMessage());
        }
    }

}