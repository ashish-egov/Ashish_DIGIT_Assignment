package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Role;
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
 * This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.
 */
@ApiModel(description = "This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-07-27T18:46:24.495+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User   {
        @JsonProperty("tenantId")
        private String tenantId = null;

        @JsonProperty("id")
        private Integer id = null;

        @JsonProperty("uuid")
        private String uuid = null;

        @JsonProperty("userName")
        private String userName = null;

        @JsonProperty("name")
        private String name = null;

        @JsonProperty("userType")
        private String userType = null;

        @JsonProperty("gender")
        private String gender = null;

        @JsonProperty("password")
        private String password = null;

        @JsonProperty("salutation")
        private String salutation = null;

        @JsonProperty("pan")
        private String pan = null;

        @JsonProperty("aadhaarNumber")
        private String aadhaarNumber = null;

        @JsonProperty("mobileNumber")
        private String mobileNumber = null;

        @JsonProperty("altContactNumber")
        private String altContactNumber=null;

        @JsonProperty("type")
        private String type = null;

        @JsonProperty("emailId")
        private String emailId = null;

        @JsonProperty("roles")
        @Valid
        private List<Role> roles = new ArrayList<>();

        @JsonProperty("permanentAddress")
        private String permanentAddress = null;

        @JsonProperty("permanentCity")
        private String permanentCity = null;

        @JsonProperty("permanentPincode")
        private String permanentPincode = null;

        @JsonProperty("correspondencePincode")
        private String correspondencePincode = null;

        @JsonProperty("active")
        private Boolean active = null;

        @JsonProperty("locale")
        private String locale = null;

        @JsonProperty("signature")
        private String signature = null;

        @JsonProperty("accountLocked")
        private Boolean accountLocked = null;

        @JsonProperty("fatherOrSpouseName")
        private String fatherOrSpouseName = null;

        @JsonProperty("bloodGroup")
        private String bloodGroup = null;

        @JsonProperty("identificationMark")
        private String identificationMark = null;

        @JsonProperty("photo")
        private String photo = null;


        public User addRolesItem(Role rolesItem) {
        this.roles.add(rolesItem);
        return this;
        }

}

