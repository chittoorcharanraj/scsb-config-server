package com.recap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by rathin maheswaran on 10/09/2020.
 * <p>
 * This class is a Custom Environment Repository used to build custom config map from the database
 * will be sent to all the SCSB clients.
 */
public class CustomEnvironmentRepository implements EnvironmentRepository, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CustomEnvironmentRepository.class);

    @Autowired
    JdbcTemplate jdbdTemplate;

    @Override
    public Environment findOne(String application, String profile, String label) {
        Environment environment = new Environment(application, profile);
        List<Map<String, Object>> configList = null;
        JSONObject ji = new JSONObject();
        JSONObject jl = new JSONObject();
        JSONObject responseJson = new JSONObject();
        Map<String, Object> response = null;
        try {
            String sql = "select p_key, p_value from scsb_properties_t where institution_code IS NULL and active='Y' and profile IS NULL";
            configList = jdbdTemplate.queryForList(sql);
            Map<String, String> configMap = getConfigMap(configList);
            configMap.forEach((key, value) -> responseJson.put((String) key.trim(), value.trim()));
            if (!profile.equalsIgnoreCase("default")) {
                String sqlEnv = "select p_key, p_value from scsb_properties_t where institution_code IS NULL and active='Y' and profile='" + profile + "'";
                configList = jdbdTemplate.queryForList(sqlEnv);
                Map<String, String> configEnvMap = getConfigMap(configList);
                configEnvMap.forEach((key, value) -> responseJson.put((String) key.trim(), value.trim()));
            }
            String institutionSql = "select distinct institution_code from scsb_properties_t where institution_code IS NOT NULL and active='Y'";
            List<String> institutions = jdbdTemplate.queryForList(institutionSql, String.class);
            for (String institution : institutions) {
                List<Map<String, Object>> institutionConfig = getInstitutionData(institution);
                logger.info("Institution -> {} -> {} ", institution, institutionConfig);

                Map<String, String> institutionConfigMap = getConfigMap(institutionConfig);
                ji.put(institution, institutionConfigMap);
            }
            responseJson.put("institution", ji.toString());
            String imsLocationSql = "select distinct ims_location_code from scsb_properties_t where ims_location_code IS NOT NULL and active='Y'";
            List<String> imsLocations = jdbdTemplate.queryForList(imsLocationSql, String.class);

            for (String imsLocation : imsLocations) {
                List<Map<String, Object>> imsLocationConfig = getImsLocationData(imsLocation);
                logger.info("ImsLocation -> {} -> {} " + imsLocation + " -> " + imsLocationConfig);

                Map<String, String> imsLocationConfigConfigMap = getConfigMap(imsLocationConfig);
                jl.put(imsLocation, imsLocationConfigConfigMap);
            }
            responseJson.put("ims_location", jl.toString());
            logger.info("Final Config Json", responseJson);
            response = responseJson.toMap();

            environment.add(new PropertySource("mapPropertySource", response));
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return environment;
    }

    /**
     * This method builds the ims location config map.
     *
     * @param imsLocation the ims location
     * @return result
     */
    public List<Map<String, Object>> getImsLocationData(String imsLocation) {
        String sql = "select p_key, p_value from scsb_properties_t where institution_code IS NULL AND ims_location_code=? and active='Y'";
        List<Map<String, Object>> result = null;
        try {
            result = jdbdTemplate.queryForList(sql, imsLocation);
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return result;
    }

    /**
     * This method builds the institution config map.
     *
     * @param institution the institution
     * @return result
     */
    public List<Map<String, Object>> getInstitutionData(String institution) {
        String sql = "select p_key, p_value from scsb_properties_t where institution_code=? and active='Y'";
        List<Map<String, Object>> result = null;
        try {
            result = jdbdTemplate.queryForList(sql, institution);
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return result;
    }

    /**
     * This method converts the list of map to a single map
     *
     * @param configList the configList
     * @return result
     */
    public Map<String, String> getConfigMap(List<Map<String, Object>> configList) {
        Map<String, String> result = new HashMap<>();
        try {
            result = configList.stream()
                    .collect(Collectors.toMap(s -> ((String) s.get("p_key")).trim(), s -> ((String) s.get("p_value")).trim()));
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return result;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}