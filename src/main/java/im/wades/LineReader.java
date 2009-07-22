package im.wades;

import java.io.IOException;
import java.io.Reader;

public class LineReader {
	private static final int CR = '\r';
	
	private Reader reader;
	private StringBuilder sb = new StringBuilder();
	
	private char[] buffer = new char[1024];
	
	public LineReader(Reader reader) {
		this.reader = reader;
	}
	
	public String readLine() throws IOException {
		while (true) {
			if (sb.length() > 0) {
				// See if we already have a new line
				String current = sb.toString();
				int index = current.indexOf(CR);
				if (index >= 0) {
					String line = current.substring(0, index+1);
					if (index < current.length()) {
						sb = new StringBuilder(line.substring(index+1));
					} else {
						sb = new StringBuilder();
					}
					return line;
				}
			}
			
			// read
			int count = reader.read(buffer, 0, buffer.length);
			if (count == -1) {
				return null;
			}

			sb.append(buffer, 0, count);
		}
	}
}
