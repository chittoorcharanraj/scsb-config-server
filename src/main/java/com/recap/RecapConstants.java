package com.recap;

public class RecapConstants {

    public static final String SQL = "select p_key, p_value from scsb_properties_t where institution_code IS NULL and ims_location_code is NULL and active='Y' and profile IS NULL and is_encrypted ='N'";
    public static final String SQL_ENV = "select p_key, p_value from scsb_properties_t where institution_code IS NULL and ims_location_code is NULL and active='Y' and profile= ? and is_encrypted ='N'";
    public static final String SQL_FOR_ENCRYPTED = "select p_key, p_value from scsb_properties_t where institution_code IS NULL and ims_location_code is NULL and active='Y' and profile IS NULL and is_encrypted ='Y'";
    public static final String SQL_ENV_FOR_ENCRYPTED = "select p_key, p_value from scsb_properties_t where institution_code IS NULL and ims_location_code is NULL and active='Y' and profile= ? and is_encrypted ='Y'";
    public static final String SQL_INSTITUTION = "select distinct institution_code from scsb_properties_t where institution_code IS NOT NULL and active='Y'";
    public static final String SQL_IMS_LOCATION = "select distinct ims_location_code from scsb_properties_t where ims_location_code IS NOT NULL and active='Y'";
    public static final String SQL_INSTITUTION_RECORDS = "select p_key, p_value from scsb_properties_t where institution_code=? and ims_location_code IS NULL and active='Y' and is_encrypted ='N'";
    public static final String SQL_INSTITUTION_RECORDS_FOR_ENCRYPTED = "select p_key, p_value from scsb_properties_t where institution_code=? and ims_location_code IS NULL and active='Y' and is_encrypted = 'Y'";
    public static final String SQL_IMS_LOCATION_RECORDS = "select p_key, p_value from scsb_properties_t where institution_code IS NULL AND ims_location_code=? and active='Y' and is_encrypted ='N'";
    public static final String SQL_IMS_LOCATION_RECORDS_FOR_ENCRYPTED = "select p_key, p_value from scsb_properties_t where institution_code IS NULL AND ims_location_code=? and active='Y' and is_encrypted = 'Y'";
    public static final String SQL_INSTITUTION_AND_IMS_LOCATION_RECORDS = "select p_key, p_value from scsb_properties_t where institution_code=? AND ims_location_code=? and active='Y' and is_encrypted ='N'";
    public static final String SQL_INSTITUTION_AND_IMS_LOCATION_RECORDS_FOR_ENCRYPTED = "select p_key, p_value from scsb_properties_t where institution_code=? AND ims_location_code=? and active='Y' and is_encrypted = 'Y'";
}
