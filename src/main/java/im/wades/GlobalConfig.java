package im.wades;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class GlobalConfig {
	private static Properties configFile = new Properties();
	
	static {
		File file = new File("config.properties");
		
		if (file.isFile()) {
			try {
				FileInputStream is = new FileInputStream(file);
				try {
					configFile.load(is);
				} finally {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private GlobalConfig() {}
	
	public static String get(String key) {
		String value = System.getProperty(key);
		if (value != null) {
			return value;
		}
		
		return configFile.getProperty(key);
	}
	
	public static String getRequired(String key) {
		String value = get(key);
		
		if (StringUtils.isBlank(value)) {
			throw new RuntimeException("Key missing in config.properties: " + key);
		}
		
		return value;
	}
}
