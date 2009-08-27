package im.wades;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class TwitterStreamHandler implements Runnable {
	private HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

	protected abstract HttpMethod getMethod();
	private boolean running = true;

	public void run() {
        try {
		while (running) {
			try {
				runStream();
			} catch (Exception e) {
				e.printStackTrace();
				// Back off
				try {
					Thread.sleep(5 * 1000L);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
        } finally {
            System.out.println("quiting");
        }
	}
	
	public void runStream() throws HttpException, IOException {
		HttpClient client = new HttpClient(connectionManager);

		HttpMethod method = getMethod();
		
		// Should grab the last 10 tweet before joining the live stream, doesn't seem to work though
		//post.addParameter("count", "10");
		
		String twitterUsername = GlobalConfig.getRequired("twitter.username");
		String twitterPassword = GlobalConfig.getRequired("twitter.password");
		
		method.addRequestHeader("Authorization", "Basic " + new String(Base64.encodeBase64((twitterUsername + ":" + twitterPassword).getBytes("UTF-8"))));

		System.out.println("executing: " + method.getURI());
		
		int code = client.executeMethod(method);

		if (code == 200) {
		  System.out.println("Code 200");
		  
			InputStream stream = method.getResponseBodyAsStream();
			Reader reader = new InputStreamReader(stream, "UTF-8");
			LineReader lineReader = new LineReader(reader);

			while (running) {
				String line;
				try {
					line = lineReader.readLine();
				} catch (SocketException e) {
					System.out.println("Disconnected... Reconnecting");
					break;
				}
				
				if (line == null) {
					break;
				}
				if (StringUtils.isBlank(line)) {
					continue;
				}
				
				try {
					parseLine(line);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
		  System.out.println("code: " + code + ": " + method.getStatusLine().toString());
			throw new HttpException(method.getStatusLine().toString());
		}
	}
	
	private void parseLine(String line) throws IOException {
		Map<String, ?> entry = new ObjectMapper().readValue(line, HashMap.class);
		
		handleEntry(entry);
	}
	
	protected abstract void handleEntry(Map<String, ?> entry);
}
