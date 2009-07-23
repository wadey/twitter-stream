package im.wades;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.BooleanUtils;

public class Utilities {
	private static final HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	
	private Utilities() {}
	
	public static void notify(String title, String message) {
		if (BooleanUtils.toBoolean(GlobalConfig.get("growl.enabled"))) {
			growl(title, message);
		}
		if (BooleanUtils.toBoolean(GlobalConfig.get("prowl.enabled"))) {
			prowl(title, message);
		}
	}
	
	public static void prowl(String title, String message) {
		PostMethod post = new PostMethod("https://prowl.weks.net/publicapi/add");
		
		post.addParameter("apikey", GlobalConfig.getRequired("prowl.apikey"));
		post.addParameter("priority", "0");
		post.addParameter("application", "Twitter");
		post.addParameter("event", title);
		post.addParameter("description", message);
		
		HttpClient client = new HttpClient(connectionManager);
		try {
			// TODO: retry on failure?
			int result = client.executeMethod(post);
			if (result != 200) {
				throw new RuntimeException("Prowl returned code: " + result + ": " + post.getResponseBodyAsString());
			}
		} catch (HttpException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void growl(String title, String message) {
		String[] cmd = new String[] {GlobalConfig.getRequired("growl.path"), "-n", GlobalConfig.getRequired("growl.application_name"), "--image", "twitter.png", "-m", message, title};
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
