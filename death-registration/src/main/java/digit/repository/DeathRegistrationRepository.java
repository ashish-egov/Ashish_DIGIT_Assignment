package digit.repository;

import digit.repository.querybuilder.DeathApplicationQueryBuilder;
import digit.repository.rowmapper.DeathApplicationRowMapper;
import digit.web.models.DeathApplicationSearchCriteria;
import digit.web.models.DeathRegistrationApplication;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Repository
public class DeathRegistrationRepository {

    @Autowired
    private DeathApplicationQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DeathApplicationRowMapper rowMapper;

    public List<DeathRegistrationApplication>getApplications(DeathApplicationSearchCriteria searchCriteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getDeathApplicationSearchQuery(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
    }
}