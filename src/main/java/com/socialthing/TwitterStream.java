package com.socialthing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;

public class TwitterStream {
	public static void main(String[] args) {
		String followIds = "7111912,619303,5882252,607,6601622,7214742,6323672,822722,11034282,8239732,4829901,6253282,46413,4374531,7282592,11990922,8714762,6264412,12332752,957361,783214,12238992,10252432,817209,8114422,3754891,841791,14053257,246,749863,1936871,12086072,12086242,6508432,627403,722793,1022021,5773142,5329162,1976611,14179807,11340982,9323772,14193093,11132462,2558171,5997662,14339438,14277276,12687402,14436769,3089271,14391242,8944112,14118444,1662741,1581511,1497,14693823,14297517,1657311,15367309,18713,14207804,15227736,688543,15488127,15825372,15829440,1448441,9653132,15184890,14441172,14306822,15595963,15695130,9417522,7124752,10671602,820828,17521192,4380901,5517,14692093,5668942,18166575,14678823,3481431,15519247,14534599,18228301,793926,15860068,16654276,15726199,14118775,16745947,15361570,16953534,19039557,17358203,13334762,15093321,15120999,15120132,18977397,17238404,15347089,14630648,18643279,5380672,1401881,7441552,18393724,21257458,717233,999,22181452,25020136,24350962,26863475,27391940,1541421,30350705,760799,19804160,30431029,8273,35249131,35743505,8516482,16031525,18370918,35910476,739643,14170974,14199666,32163783,19365183,22663655,14373435,1501,14712874,36728266,22161443,18907022,3044891,24726118,28221616,15029296,44248257,18044341,47996502,28445249,18495654,14589771,8731312,15450695,39534501,38886544,18866294,19477258,5424762,18057459,23262791,4234581,17993716";
		
		TwitterStreamHandler handler = new TwitterStreamHandler();
		
		for (String id : followIds.split(",")) {
			handler.addUser(Long.valueOf(id));
		}
		
		handler.run();
	}
	
	public static class TwitterStreamHandler implements Runnable {
		private HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

		private Set<Long> followIds = new HashSet<Long>(100000);

		public void addUser(Long userId) {
			followIds.add(userId);
		}

		public void run() {
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

			StringBuilder followString = new StringBuilder();

			boolean first = true;
			for (Long followId : followIds) {
				if (first) {
					first = false;
				} else {
					followString.append(',');
				}
				followString.append(followId);
			}

			PostMethod post = new PostMethod("http://stream.twitter.com/follow.json");
			post.addParameter("follow", followString.toString());
			post.addParameter("delimited", "length");
			//post.addParameter("count", "10");
			
			post.addRequestHeader("Authorization", "Basic " + new String(Base64.encodeBase64(("xxx" + ":" + "xxx").getBytes("UTF-8"))));

			System.out.println("executing: " + post.getURI());
			
			int code = client.executeMethod(post);

			if (code == 200) {
				InputStream stream = post.getResponseBodyAsStream();
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
				System.out.println(post.getStatusLine());
			}
		}
		
		private void parseLine(String line) throws IOException {
			Map<String, ?> entry = new ObjectMapper().readValue(line, HashMap.class);
			
			if (entry.containsKey("text")) {
				// tweet
				
				Map<String, ?> user = (Map<String, ?>) entry.get("user");
				
				// TODO: kind of hacky to ensure it is a Long
				Long userId = ((Number) user.get("id")).longValue();
				
				if (followIds.contains(userId)) {
					growl((String) user.get("screen_name"), (String) entry.get("text"));
					System.out.println("tweet: " + line);
				} else {
					// This is an @ reply
					Long inReplyToUserId = ((Number) entry.get("in_reply_to_user_id")).longValue();
					
					//if (inReplyToUserId == 7111912L) {
						growl((String) user.get("screen_name"), (String) entry.get("text"));
					//}
					
					System.out.println("@reply: " + line);
				}
				
			} else {
				// something else
				System.out.println(line);
			}
		}
	}
	
	public static void growl(String title, String message) throws IOException {
		String[] cmd = new String[]{"/usr/local/bin/growlnotify", "--image", "twitter.png", "-m", message, title};
		Runtime.getRuntime().exec(cmd);
	}
}
