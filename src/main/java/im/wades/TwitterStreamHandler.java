package im.wades;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class TwitterStreamHandler implements Runnable {
	private HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

	protected abstract HttpMethod getMethod();

	public void run() {
		// TODO: loop on errors, disconnects
		
		try {
			runStream();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void runStream() throws HttpException, IOException {
		HttpClient client = new HttpClient(connectionManager);

		HttpMethod method = getMethod();
		
		// Should grab the last 10 tweet before joining the live stream, doesn't seem to work though
		//post.addParameter("count", "10");
		
		method.addRequestHeader("Authorization", "Basic " + new String(Base64.encodeBase64((GlobalConfig.get("twitter.username") + ":" + GlobalConfig.get("twitter.password")).getBytes("UTF-8"))));

		System.out.println("executing: " + method.getURI());
		
		int code = client.executeMethod(method);

		if (code == 200) {
			InputStream stream = method.getResponseBodyAsStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(stream));

			String lengthBytesString;
			while (true) {
				do {
					lengthBytesString = reader.readLine();
				} while (lengthBytesString.length() < 1);
				
				int lengthBytes = Integer.valueOf(lengthBytesString);
				int off = 0;
				char[] buffer = new char[lengthBytes];
				while (off < lengthBytes) {
					off += reader.read(buffer, off, lengthBytes - off);
				}
				String line = new String(buffer);
				
				parseLine(line);
			}
		} else {
			throw new HttpException(method.getStatusLine().toString());
		}
	}
	
	private void parseLine(String line) throws IOException {
		Map<String, ?> entry = new ObjectMapper().readValue(line, HashMap.class);
		
		handleEntry(entry);
	}
	
	protected abstract void handleEntry(Map<String, ?> entry);
}
