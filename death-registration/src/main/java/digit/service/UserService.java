package digit.service;

import digit.config.DTRConfiguration;
import digit.util.UserUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserService {
    private UserUtil userUtils;

    private DTRConfiguration config;

    @Autowired
    public UserService(UserUtil userUtils, DTRConfiguration config) {
        this.userUtils = userUtils;
        this.config = config;
    }

    /**
     * Calls user service to enrich user from search or upsert user
     * @param request
     */
    public void callUserService(DeathRegistrationRequest request){
        request.getDeathRegistrationApplications().forEach(application -> {
            if(application.getApplicant()!=null) {
                if(!StringUtils.isEmpty(application.getApplicant().getId()))
                    enrichUser(application, request.getRequestInfo());
                else {
                    User user = createUserApplicant(application);
                    application.getApplicant().setId(upsertUser(user, request.getRequestInfo()));
                }
            }
        });
    }

    private User createUserApplicant(DeathRegistrationApplication application){
        Applicant applicant = application.getApplicant();
        User user = User.builder().userName(applicant.getUserName())
                .name(applicant.getName())
                .mobileNumber(applicant.getMobileNumber())
                .emailId(applicant.getEmailId())
                .altContactNumber(applicant.getAltContactNumber())
                .tenantId(applicant.getTenantId())
                .type(applicant.getType())
                .roles(applicant.getRoles())
                .build();
        return user;
    }
    private String upsertUser(User user, RequestInfo requestInfo){

        String tenantId = user.getTenantId();
        User userServiceResponse = null;

        // Search on mobile number as user name
        UserDetailResponse userDetailResponse = searchUser(userUtils.getStateLevelTenant(tenantId),null, user.getUserName(),null);
        if (!userDetailResponse.getUser().isEmpty()) {
            User userFromSearch = userDetailResponse.getUser().get(0);
            log.info(userFromSearch.toString());
            if(!user.getUserName().equalsIgnoreCase(userFromSearch.getUserName())){
                userServiceResponse = updateUser(requestInfo,user,userFromSearch);
            }
            else userServiceResponse = userDetailResponse.getUser().get(0);
        }
        else {
            userServiceResponse = createUser(requestInfo,tenantId,user);
        }

        // Enrich the accountId
        // user.setId(userServiceResponse.getUuid());
        return userServiceResponse.getUuid();
    }


    private void enrichUser(DeathRegistrationApplication application, RequestInfo requestInfo){
        String accountIdApplicant = application.getApplicant().getId();
        String tenantId = application.getTenantId();

        UserDetailResponse userDetailResponse = searchUser(userUtils.getStateLevelTenant(tenantId),accountIdApplicant,application.getApplicant().getUserName(),application.getApplicant().getUuid());
//        UserDetailResponse userDetailResponseMother = searchUser(userUtils.getStateLevelTenant(tenantId),accountIdMother,null);
        if(userDetailResponse.getUser().isEmpty())
            throw new CustomException("INVALID_ACCOUNTID","No user exist for the given accountId");

        else application.getApplicant().setId(userDetailResponse.getUser().get(0).getUuid());

    }

    /**
     * Creates the user from the given userInfo by calling user service
     * @param requestInfo
     * @param tenantId
     * @param userInfo
     * @return
     */
    private User createUser(RequestInfo requestInfo,String tenantId, User userInfo) {

        userUtils.addUserDefaultFields(userInfo.getUserName(),tenantId, userInfo);
        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserCreateEndpoint());

        CreateUserRequest user = new CreateUserRequest(requestInfo, userInfo);
        log.info(user.getUser().toString());
        UserDetailResponse userDetailResponse = userUtils.userCall(user, uri);

        return userDetailResponse.getUser().get(0);

    }

    /**
     * Updates the given user by calling user service
     * @param requestInfo
     * @param user
     * @param userFromSearch
     * @return
     */
    private User updateUser(RequestInfo requestInfo,User user,User userFromSearch) {

        userFromSearch.setUserName(user.getUserName());
        userFromSearch.setActive(true);

        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserUpdateEndpoint());


        UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userFromSearch), uri);

        return userDetailResponse.getUser().get(0);

    }

    /**
     * calls the user search API based on the given accountId and userName
     * @param stateLevelTenant
     * @param accountId
     * @param userName
     * @return
     */
    public UserDetailResponse searchUser(String stateLevelTenant, String accountId, String userName,String uuid){

        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType("EMPLOYEE");
        userSearchRequest.setTenantId(stateLevelTenant);

        if(StringUtils.isEmpty(accountId) && StringUtils.isEmpty(userName))
            return null;

        if(!StringUtils.isEmpty(accountId))
            userSearchRequest.setUuid(Collections.singletonList(uuid));

        if(!StringUtils.isEmpty(userName))
            userSearchRequest.setUserName(userName);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userUtils.userCall(userSearchRequest,uri);

    }

    /**
     * calls the user search API based on the given list of user uuids
     * @param uuids
     * @return
     */
    private Map<String,User> searchBulkUser(List<String> uuids){

        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType("CITIZEN");


        if(!CollectionUtils.isEmpty(uuids))
            userSearchRequest.setUuid(uuids);


        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest,uri);
        List<User> users = userDetailResponse.getUser();

        if(CollectionUtils.isEmpty(users))
            throw new CustomException("USER_NOT_FOUND","No user found for the uuids");

        Map<String,User> idToUserMap = users.stream().collect(Collectors.toMap(User::getUuid, Function.identity()));

        return idToUserMap;
    }

}