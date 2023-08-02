package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Address;
import digit.web.models.Applicant;
import digit.web.models.AuditDetails;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * A Object holds the basic data for a Death Registration Application
 */
@ApiModel(description = "A Object holds the basic data for a Death Registration Application")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-07-27T18:46:24.495+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeathRegistrationApplicationSearch {
    @Valid
    @JsonProperty("workflow")
    private Workflow workflow = null;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("applicationNumber")
    private String applicationNumber = null;

    @JsonProperty("deceasedFirstName")
    private String deceasedFirstName = null;

    @JsonProperty("deceasedLastName")
    private String deceasedLastName = null;

    @JsonProperty("placeOfDeath")
    private String placeOfDeath = null;

    @JsonProperty("timeOfDeath")
    private Integer timeOfDeath = null;

    @JsonProperty("addressOfDeceased")
    private Address addressOfDeceased = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;
}
