import java.io.*;
import java.util.*;

public class test2 {



  public static void main(String[] args) throws Exception {

  BufferedReader CSVFile = new BufferedReader(new FileReader("xml/report.csv"));

  String dataRow = CSVFile.readLine(); // Read first line.
  // The while checks to see if the data is null. If 
  // it is, we've hit the end of the file. If not, 
  // process the data.

  Map report = new HashMap();

  while (dataRow != null){
   String[] dataArray = dataRow.split(",");
   System.out.println("Length = " + dataArray.length);
   List l = new ArrayList();
   String key = dataArray[0];
   for (int i=1; i<dataArray.length; i++) { 
      String item = dataArray[i];
      System.out.print(item + "\t"); 
      l.add(item);
   }
   report.put(key, l);
   System.out.println(); // Print the data line.
   dataRow = CSVFile.readLine(); // Read next line of data.
  }
   System.out.println(report); // Print the data line.
  // Close the file once all data has been read.
  CSVFile.close();

  // End the printout with a blank line.
  System.out.println();

  }
}    
