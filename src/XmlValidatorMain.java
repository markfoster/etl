import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.validation.Schema;
import org.xml.sax.SAXException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * 
 * @author mfoster
 */
public class XmlValidatorMain {

	private static final String lineSeparator = System
			.getProperty("line.separator");
	private static final String doubleLineSeparator = lineSeparator
			+ lineSeparator;

	// All the messages that need to be displayed:
	private static final String msg_compile_success = "Schema Compile Successful: [%1$s]"
			+ lineSeparator;
	private static final String msg_compile_fail = "Error compiling schema: %1$s:"
			+ doubleLineSeparator;
	private static final String msg_validation_success = "Validation Successful: [%1$s] against [%2$s]"
			+ lineSeparator;
	private static final String msg_validation_fail = "Error validating [%1$s] against [%2$s]:"
			+ doubleLineSeparator;

	public static void main(String[] arg) throws IOException {
		XmlValidatorMain test = new XmlValidatorMain();
		// List<String> extras = Args.parse(parser, arg);
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
                        File xml = new File("xml/Program.xml");
			doc = reader.read(xml);

                        Validator v = new Validator("xml/test.xml");
                        v.validateXml();

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (doc != null) {
			System.out.println(doc.toString());
			if (doc.getRootElement().getName() == "program") {
				System.out.println("program\n");
				System.out.println(doc.getRootElement().attributeValue("buid"));
			}
		}
	}
}
