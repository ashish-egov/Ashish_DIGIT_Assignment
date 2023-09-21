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
public class DeathApplicationRowMapper implements ResultSetExtractor<List<DeathRegistrationApplication>> {
    public List<DeathRegistrationApplication> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, DeathRegistrationApplication> deathRegistrationApplicationMap = new LinkedHashMap<>();

        while (rs.next()) {
            String uuid = rs.getString("bapplicationnumber");
            DeathRegistrationApplication deathRegistrationApplication = deathRegistrationApplicationMap.get(uuid);

            if (deathRegistrationApplication == null) {
                deathRegistrationApplication = createDeathRegistrationApplication(rs);
            }

            addChildrenToProperty(rs, deathRegistrationApplication);
            deathRegistrationApplicationMap.put(uuid, deathRegistrationApplication);
        }
        return new ArrayList<>(deathRegistrationApplicationMap.values());
    }

    private DeathRegistrationApplication createDeathRegistrationApplication(ResultSet rs) throws SQLException {
        Long lastModifiedTime = rs.getLong("blastModifiedTime");
        if (rs.wasNull()) {
            lastModifiedTime = null;
        }

        AuditDetails auditDetails = createAuditDetails(
                rs.getString("bcreatedBy"),
                rs.getLong("bcreatedTime"),
                rs.getString("blastModifiedBy"),
                lastModifiedTime
        );

        return DeathRegistrationApplication.builder()
                .applicationNumber(rs.getString("bapplicationnumber"))
                .tenantId(rs.getString("dtenantid"))
                .id(rs.getString("bid"))
                .deceasedFirstName(rs.getString("bdeceasedfirstname"))
                .deceasedLastName(rs.getString("bdeceasedlastname"))
                .placeOfDeath(rs.getString("bplaceofdeath"))
                .timeOfDeath(rs.getInt("dtimeofdeath"))
                .auditDetails(auditDetails)
                .build();
    }

    private AuditDetails createAuditDetails(String createdBy, Long createdTime, String lastModifiedBy, Long lastModifiedTime) {
        return AuditDetails.builder()
                .createdBy(createdBy)
                .createdTime(createdTime)
                .lastModifiedBy(lastModifiedBy)
                .lastModifiedTime(lastModifiedTime)
                .build();
    }


    private void addChildrenToProperty(ResultSet rs, DeathRegistrationApplication deathRegistrationApplication)
            throws SQLException {
        addAddressToApplication(rs, deathRegistrationApplication);
    }

    private void addAddressToApplication(ResultSet rs, DeathRegistrationApplication deathRegistrationApplication) throws SQLException {
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
