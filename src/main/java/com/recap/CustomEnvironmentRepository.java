package com.recap;

import com.recap.util.SecurityUtil;
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

    @Autowired
    SecurityUtil securityUtil;

    @Override
    public Environment findOne(String application, String profile, String label) {
        Environment environment = new Environment(application, profile);
        JSONObject ji = new JSONObject();
        JSONObject jl = new JSONObject();
        JSONObject jq = new JSONObject();
        JSONObject responseJson = new JSONObject();
        Map<String, Object> responseMap = null;
        Map<String, Object> responseEncryptedEntriesMap = null;
        Map<String, Object> finalResponseMap = null;
        try {
            responseMap = getJsonObject(RecapConstants.SQL, RecapConstants.SQL_ENV, profile, false).toMap();
            responseEncryptedEntriesMap = getJsonObject(RecapConstants.SQL_FOR_ENCRYPTED, RecapConstants.SQL_ENV_FOR_ENCRYPTED, profile, true).toMap();
            //-------------------------------INSTITUTION SPECIFIC-------------------------------------------------
            List<String> institutions = jdbdTemplate.queryForList(RecapConstants.SQL_INSTITUTION, String.class);
            for (String institution : institutions) {
                List<Map<String, Object>> institutionConfig = getInstitutionData(institution);
                Map<String, String> institutionConfigMap = getConfigMap(institutionConfig);
                ji.put(institution, institutionConfigMap);
            }
            responseJson.put("institution", ji.toString());
            //-------------------------------IMS_LOCATION SPECIFIC-------------------------------------------------
            List<String> imsLocations = jdbdTemplate.queryForList(RecapConstants.SQL_IMS_LOCATION, String.class);
            for (String imsLocation : imsLocations) {
                List<Map<String, Object>> imsLocationConfig = getImsLocationData(imsLocation);
                Map<String, String> imsLocationConfigConfigMap = getConfigMap(imsLocationConfig);
                jl.put(imsLocation, imsLocationConfigConfigMap);
            }
            responseJson.put("ims_location", jl.toString());

            //-------------------------------INSTITUTION AND IMS_LOCATION SPECIFIC-------------------------------------------------
            for (String institution : institutions) {
                JSONObject jp = new JSONObject();
                for (String imsLocation : imsLocations) {
                    List<Map<String, Object>> institutionAndImsLocationConfig = getInstitutionAndImsLocationData(institution, imsLocation);
                    Map<String, String> institutionAndImsLocationConfigConfigMap = getConfigMap(institutionAndImsLocationConfig);
                    jp.put(imsLocation, institutionAndImsLocationConfigConfigMap);
                }
                jq.put(institution,jp);
            }
            System.out.println(jq);
            responseJson.put("institution_and_ims_location_group", jq.toString());

            //Building the final ResponseJson as map response
            finalResponseMap = responseJson.toMap();
            finalResponseMap.putAll(responseMap);
            finalResponseMap.putAll(responseEncryptedEntriesMap);
            environment.add(new PropertySource("mapPropertySource", finalResponseMap));
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return environment;
    }

    public JSONObject getJsonObject(String sql, String sqlEnv, String profile, boolean isEncrypted) {
        JSONObject responseJson = new JSONObject();
        List<Map<String, Object>> configList = null;
        try {
            logger.info("Profile --> " + profile);
            configList = jdbdTemplate.queryForList(sql);
            Map<String, String> configMap = getConfigMap(configList);
            if (isEncrypted) {
                configMap.forEach((key, value) -> responseJson.put((String) key.trim(), securityUtil.getDecryptedValue(value)));
            } else {
                configMap.forEach((key, value) -> responseJson.put((String) key.trim(), value.trim()));
            }
            if (!profile.equalsIgnoreCase("default")) {
                configList = jdbdTemplate.queryForList(sqlEnv, profile);
                Map<String, String> configEnvMap = getConfigMap(configList);
                if (isEncrypted) {
                    configEnvMap.forEach((key, value) -> responseJson.put((String) key.trim(), securityUtil.getDecryptedValue(value)));
                } else {
                    configEnvMap.forEach((key, value) -> responseJson.put((String) key.trim(), value.trim()));
                }
            }
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return responseJson;
    }

    /**
     * This method builds the institution and ims location config map.
     *
     * @param imsLocation the ims location
     * @return result
     */
    public List<Map<String, Object>> getInstitutionAndImsLocationData(String institution, String imsLocation) {
        List<Map<String, Object>> result = null;
        String arr[] = {institution, imsLocation};
        try {
            result = getResult(imsLocation, result, RecapConstants.SQL_INSTITUTION_AND_IMS_LOCATION_RECORDS, RecapConstants.SQL_INSTITUTION_AND_IMS_LOCATION_RECORDS_FOR_ENCRYPTED, arr);
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return result;
    }

    /**
     * This method builds the ims location config map.
     *
     * @param imsLocation the ims location
     * @return result
     */
    public List<Map<String, Object>> getImsLocationData(String imsLocation) {
        List<Map<String, Object>> result = null;
        try {
            result = getResult(imsLocation, result, RecapConstants.SQL_IMS_LOCATION_RECORDS, RecapConstants.SQL_IMS_LOCATION_RECORDS_FOR_ENCRYPTED, null);
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
        List<Map<String, Object>> result = null;
        try {
            result = getResult(institution, result, RecapConstants.SQL_INSTITUTION_RECORDS, RecapConstants.SQL_INSTITUTION_RECORDS_FOR_ENCRYPTED, null);
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return result;
    }

    private List<Map<String, Object>> getResult(String institutionOrlocation, List<Map<String, Object>> result, String sql, String sqlEncrypted, String arr[]) {
        List<Map<String, Object>> resultDecryptList;
        List<Map<String, Object>> resultDecryptListFinal;
        try {
            if (arr != null) {
                result = jdbdTemplate.queryForList(sql, arr[0], arr[1]);
                resultDecryptList = jdbdTemplate.queryForList(sqlEncrypted, arr[0], arr[1]);

                if (resultDecryptList.size() > 0) {
                    resultDecryptListFinal = resultDecryptList.stream().map(m -> m.entrySet().stream()
                            .collect(Collectors.toMap(p -> p.getKey(), p -> {
                                        return getObject(p);
                                    }
                            )))
                            .collect(Collectors.toList());
                    result.addAll(resultDecryptListFinal);
                }
            } else {
                result = jdbdTemplate.queryForList(sql, institutionOrlocation);
                resultDecryptList = jdbdTemplate.queryForList(sqlEncrypted, institutionOrlocation);

                if (resultDecryptList.size() > 0) {
                    resultDecryptListFinal = resultDecryptList.stream().map(m -> m.entrySet().stream()
                            .collect(Collectors.toMap(p -> p.getKey(), p -> {
                                        return getObject(p);
                                    }
                            )))
                            .collect(Collectors.toList());
                    result.addAll(resultDecryptListFinal);
                }
            }
        } catch (Exception e) {
            logger.error("error--> {}", e);
        }
        return result;
    }

    private Object getObject(Map.Entry<String, Object> p) {
        if (p.getKey().equals("p_value")) {
            Object decryptedValue = securityUtil.getDecryptedValue((String) p.getValue());
            return decryptedValue;
        }
        return p.getValue();
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