package gov.irs.automationtest.fw;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

public class ConfUtils {
	
	private static Properties commonConf = null;
	private static Properties envConf = null;
	public static String env;
	private static String RESOURCE_PATH="src" + File.separator + "main" + File.separator + "resources";
	
	public static Properties loadConf() {
		String confResourcesPath = "/config/common.properties";
		commonConf = new Properties();
		
		try {
			PropertiesFileUtils.loadPropsFromResource(commonConf, confResourcesPath);
			commonConf = LoadSystemProperties(commonConf);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return commonConf;
		
	}
	
	public static Properties loadEnvConf() {
		if (System.getProperty("env")==null) {
			env = commonConf.getProperty("env");
		} else {
			env = System.getProperty("env");
		}
		String confResourcesPath = "/config/"+env+".properties";
		envConf = new Properties();
		
		try {
			PropertiesFileUtils.loadPropsFromResource(envConf, confResourcesPath);
			envConf = LoadSystemProperties(envConf);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return envConf;
		
	}

	private static Properties LoadSystemProperties(Properties commonConf2) {
		// TODO Auto-generated method stub
		Properties systemProperties = System.getProperties();
		Enumeration<?> e = systemProperties.propertyNames();
		String key, value;
		while (e.hasMoreElements()) {
			key = (String) e.nextElement();
			value = systemProperties.getProperty(key);
			commonConf2.setProperty(key, value);
		}
		return commonConf2;
	}
	
	public static Properties getProperties() {
		if (commonConf == null) {
			loadConf();
		}
		return commonConf;
	}
	
	public static String getProperty(String key) {
		if (commonConf == null) {
			loadConf();
		}
		return commonConf.getProperty(key);
	}
	
	public static void setProperty(String key, String value) {
		if (commonConf == null) {
			loadConf();
		}
		commonConf.setProperty(key,value);
	}
	
	public static String getEnvProperty(String key) {
		if (envConf == null) {
			loadEnvConf();
		}
		return envConf.getProperty(key);
	}
	
	public static void setEnvProperty(String key, String value) {
		if (envConf == null) {
			loadEnvConf();
		}
		envConf.setProperty(key,value);
	}

}
