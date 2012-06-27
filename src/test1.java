import java.io.*;
import java.util.*;

public class test1 {

  public static void main(String[] args) throws Exception {

String[] commandArray = {
    "/bin/bash",
    "/usr/local/bin/CMS0294Alert.sh",
    "-c 3", "-s 'abc'", "-m 'def'"
};
System.out.println(commandArray);
Process process = Runtime.getRuntime().exec(commandArray);
System.out.println(process);

int exitValue = process.waitFor();  
            System.out.println("exit value: " + exitValue);  
            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));  
            String line = "";  
            while ((line = buf.readLine()) != null) {  
                System.out.println("exec response: " + line);  
            } 

  }
}    
