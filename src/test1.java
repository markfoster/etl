import java.io.*;
import java.util.*;

public class test1 {

    public static void main(String[] args) throws Exception {

	int iTotal = 21222;

	for (int i = 0; i < iTotal; i++) {

	    if (iTotal > 10000) {
		if (i % 500 == 0)
		    System.out.println(String.format("Record %d / %d", i, iTotal));
	    }

	}

	/**
	 * BigDecimal d = new
	 * 
	 * 2.9788620471954345703125, 101.7407684326171875
	 **/

	/**
	 * String[] commandArray = { "/bin/bash",
	 * "/usr/local/bin/CMS0294Alert.sh", "-c 3", "-s 'abc'", "-m 'def'" };
	 * System.out.println(commandArray); Process process =
	 * Runtime.getRuntime().exec(commandArray); System.out.println(process);
	 * 
	 * int exitValue = process.waitFor(); System.out.println("exit value: "
	 * + exitValue); BufferedReader buf = new BufferedReader(new
	 * InputStreamReader(process.getInputStream())); String line = ""; while
	 * ((line = buf.readLine()) != null) {
	 * System.out.println("exec response: " + line); }
	 **/

    }
}
