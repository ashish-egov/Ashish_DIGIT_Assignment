package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.DeathRegistrationApplication;
import org.egov.common.contract.response.ResponseInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * Contract class to send response. Array of  items are used in case of search results or response for create, whereas single  item is used for update
 */
@ApiModel(description = "Contract class to send response. Array of  items are used in case of search results or response for create, whereas single  item is used for update")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-07-27T18:46:24.495+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeathRegistrationResponse   {
        @JsonProperty("ResponseInfo")
        private ResponseInfo responseInfo = null;

        @JsonProperty("DeathRegistrationApplications")
        @Valid
        private List<DeathRegistrationApplication> deathRegistrationApplications = null;


        public DeathRegistrationResponse addDeathRegistrationApplicationsItem(DeathRegistrationApplication deathRegistrationApplicationsItem) {
            if (this.deathRegistrationApplications == null) {
            this.deathRegistrationApplications = new ArrayList<>();
            }
        this.deathRegistrationApplications.add(deathRegistrationApplicationsItem);
        return this;
        }

}

