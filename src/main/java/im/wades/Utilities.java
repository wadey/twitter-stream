package im.wades;

import java.io.IOException;

public class Utilities {
	private Utilities() {}
	
	public static void growl(String title, String message) {
		String[] cmd = new String[] {GlobalConfig.get("growl.path"), "-n", "twitter-stream", "--image", "twitter.png", "-m", message, title};
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
