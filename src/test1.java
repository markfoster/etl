import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.OutputKeys;

public class test1 {

  public static void main(String[] args) throws Exception {
  Source source = new StreamSource("input.xml");
  Source xsl = new StreamSource("xfer.xsl");
  Result result = new StreamResult(System.out);

  TransformerFactory factory = TransformerFactory.newInstance();
  Transformer transformer = factory.newTransformer(xsl);
  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
  transformer.transform(xsl, result);
  }
}    
