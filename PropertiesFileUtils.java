package gov.irs.automationtest.fw;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFileUtils {
	
	public Properties propertiesFile;
	
	public PropertiesFileUtils(String filePath) {
		propertiesFile = new Properties();
		File inputFile = new File(filePath);
		
		try {
			FileInputStream fis = new FileInputStream(inputFile);
			propertiesFile.load(fis);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void loadPropsFromResource(Properties properties, String configPath) throws IOException {
		
		InputStream inputStream = PropertiesFileUtils.class.getResourceAsStream(configPath);
		properties.load(inputStream);
		
	}
	

}
