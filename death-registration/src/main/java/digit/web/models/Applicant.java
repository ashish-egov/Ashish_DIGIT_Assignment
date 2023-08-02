package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Role;
import digit.web.models.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * Details of the user applying for birth registration
 */
@ApiModel(description = "Details of the user applying for birth registration")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2022-08-16T15:34:24.436+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Applicant   {
        @JsonProperty("id")
        private Integer id = null;

        @JsonProperty("uuid")
        private String uuid = null;

        @JsonProperty("userName")
        private String userName = null;

        @JsonProperty("name")
        private String name = null;

        @JsonProperty("gender")
        private String gender = null;

        @JsonProperty("type")
        private String type = null;

        @JsonProperty("mobileNumber")
        private String mobileNumber = null;

        @JsonProperty("emailId")
        private String emailId = null;

        @JsonProperty("altContactNumber")
        private String altContactNumber = null;

        @JsonProperty("active")
        private Boolean active = null;

        @JsonProperty("accountLocked")
        private Boolean accountLocked = null;

        @JsonProperty("roles")
        @Valid
        private List<Role> roles = null;

        @JsonProperty("tenantId")
        private String tenantId = null;


        public Applicant addRolesItem(Role rolesItem) {
                if (this.roles == null) {
                        this.roles = new ArrayList<>();
                }
                this.roles.add(rolesItem);
                return this;
        }

}