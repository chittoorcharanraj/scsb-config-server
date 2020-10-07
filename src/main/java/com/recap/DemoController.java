package com.recap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

	@Autowired
	JdbcTemplate jdbdTemplate;

	@GetMapping(path = "/db")
	public Map<String, Object> getDataFromDb() {
		List<Map<String, Object>> configList = null;
		JSONObject ji = new JSONObject();
		JSONObject jl = new JSONObject();
		JSONObject responseJson = new JSONObject();
		Map<String, Object> response = null;
		try {
			String sql = "select prop.p_key, prop.p_value from scsb_properties_t prop where institution_code IS NULL";
			configList = jdbdTemplate.queryForList(sql);
			Map<String, String> configMap = getConfigMap(configList);
			configMap.forEach((key, value) -> responseJson.put((String) key, value));
			String institutionSql = "select distinct institution_code from scsb_properties_t where institution_code IS NOT NULL";
			List<String> institutions = jdbdTemplate.queryForList(institutionSql, String.class);

			for (String institution : institutions) {
				List<Map<String, Object>> institutionConfig = getInstitutionData(institution);
				System.out.println("Institution ->" + institution + " -> " + institutionConfig);

				Map<String, String> institutionConfigMap = getConfigMap(institutionConfig);
				ji.put(institution, institutionConfigMap);
			}
			responseJson.put("institution", ji.toString());
			String imsLocationSql = "select distinct ims_location_code from scsb_properties_t where ims_location_code IS NOT NULL";
			List<String> imsLocations = jdbdTemplate.queryForList(imsLocationSql, String.class);

			for (String imsLocation : imsLocations) {
				List<Map<String, Object>> imsLocationConfig = getImsLocationData(imsLocation);
				System.out.println("ImsLocation ->" + imsLocation + " -> " + imsLocationConfig);

				Map<String, String> imsLocationConfigConfigMap = getConfigMap(imsLocationConfig);
				jl.put(imsLocation, imsLocationConfigConfigMap);
			}
			responseJson.put("ims_location", jl.toString());
			System.out.println(responseJson);
			response = responseJson.toMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public List<Map<String, Object>> getImsLocationData(String imsLocation) {
		String sql = "select prop.p_key, prop.p_value from scsb_properties_t prop where institution_code IS NULL AND ims_location_code=?";
		List<Map<String, Object>> result = jdbdTemplate.queryForList(sql, imsLocation);
		return result;
	}

	public List<Map<String, Object>> getInstitutionData(String institution) {
		String sql = "select prop.p_key, prop.p_value from scsb_properties_t prop where institution_code=?";
		List<Map<String, Object>> result = jdbdTemplate.queryForList(sql, institution);
		return result;
	}

	public Map<String, String> getConfigMap(List<Map<String, Object>> configList) {
		Map<String, String> result = new HashMap<>();
		result = configList.stream()
				.collect(Collectors.toMap(s -> (String) s.get("p_key"), s -> (String) s.get("p_value")));
		return result;
	}
}
