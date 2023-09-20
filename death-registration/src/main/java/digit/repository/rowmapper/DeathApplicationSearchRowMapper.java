package digit.repository.rowmapper;

import digit.web.models.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeathApplicationSearchRowMapper implements ResultSetExtractor<List<DeathRegistrationApplicationSearch>> {

    public List<DeathRegistrationApplicationSearch> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, DeathRegistrationApplicationSearch> deathRegistrationApplicationMap = new LinkedHashMap<>();

        while (rs.next()) {
            String uuid = rs.getString("bapplicationnumber");
            DeathRegistrationApplicationSearch deathRegistrationApplication = deathRegistrationApplicationMap.get(uuid);

            if (deathRegistrationApplication == null) {
                deathRegistrationApplication = createDeathRegistrationApplicationFromResultSet(rs);
            }

            addChildrenToProperty(rs, deathRegistrationApplication);
            deathRegistrationApplicationMap.put(uuid, deathRegistrationApplication);
        }
        return new ArrayList<>(deathRegistrationApplicationMap.values());
    }

    private DeathRegistrationApplicationSearch createDeathRegistrationApplicationFromResultSet(ResultSet rs) throws SQLException {
        Long lastModifiedTime = rs.getLong("blastModifiedTime");
        if (rs.wasNull()) {
            lastModifiedTime = null;
        }

        AuditDetails auditDetails = createAuditDetailsFromResultSet(rs,lastModifiedTime);

        return DeathRegistrationApplicationSearch.builder()
                .applicationNumber(rs.getString("bapplicationnumber"))
                .tenantId(rs.getString("dtenantid"))
                .id(rs.getString("bid"))
                .deceasedFirstName(rs.getString("bdeceasedfirstname"))
                .deceasedLastName(rs.getString("bdeceasedlastname"))
                .placeOfDeath(rs.getString("bplaceofdeath"))
                .timeOfDeath(rs.getInt("dtimeofdeath"))
                .applicantId(rs.getInt("dapplicantid"))
                .applicantUuid(rs.getString("dapplicantuuid"))
                .applicantType(rs.getString("dapplicanttype"))
                .auditDetails(auditDetails)
                .build();
    }

    private AuditDetails createAuditDetailsFromResultSet(ResultSet rs,Long lastModifiedTime) throws SQLException {
        return AuditDetails.builder()
                .createdBy(rs.getString("bcreatedBy"))
                .createdTime(rs.getLong("bcreatedTime"))
                .lastModifiedBy(rs.getString("blastModifiedBy"))
                .lastModifiedTime(lastModifiedTime)
                .build();
    }

    private void addChildrenToProperty(ResultSet rs, DeathRegistrationApplicationSearch deathRegistrationApplication)
            throws SQLException {
        addAddressToApplication(rs, deathRegistrationApplication);
    }

    private void addAddressToApplication(ResultSet rs, DeathRegistrationApplicationSearch deathRegistrationApplication) throws SQLException {
        Address address = Address.builder()
                .id(rs.getString("aid"))
                .tenantId(rs.getString("atenantid"))
//                .doorNo(rs.getString("adoorno"))
                .latitude(rs.getDouble("alatitude"))
                .longitude(rs.getDouble("alongitude"))
//                .buildingName(rs.getString("abuildingname"))
                .addressId(rs.getString("aaddressid"))
                .addressNumber(rs.getString("aaddressnumber"))
//                .type(rs.getString("atype"))
                .addressLine1(rs.getString("aaddressline1"))
                .addressLine2(rs.getString("aaddressline2"))
                .landmark(rs.getString("alandmark"))
//                .street(rs.getString("astreet"))
                .city(rs.getString("acity"))
                .pincode(rs.getString("apincode"))
                .detail("adetail")
                .registrationId("aregistrationid")
                .build();

        deathRegistrationApplication.setAddressOfDeceased(address);

    }

}
