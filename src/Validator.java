import java.io.File;
// dom4j import
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Validator {
	private File file;
	private Document doc;
	private SAXReader reader;
	private handler h;

	public Validator() {
		reader = new SAXReader(true);
		h = new handler();
		// Lets configure the reader for full validation
		// set the validation feature to true to report validation errors
		try {
			reader.setFeature("http://xml.org/sax/features/validation", true);
			// set the validation/schema feature to true to report validation
			// errors against a schema
			reader.setFeature(
					"http://apache.org/xml/features/validation/schema", true);

			// set the validation/schema-full-checking feature to true to enable
			// full schema, grammar-constraint checking
			reader.setFeature(
					"http://apache.org/xml/features/validation/schema-full-checking",
					true);
			reader.setFeature(
					"http://apache.org/xml/features/continue-after-fatal-error",
					true);
			// reader.setFeature("http://apache.org/xml/features/validation-error-as-fatal",
			// true);
			reader.setErrorHandler(h);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	};

	public Validator(String fileName) {
		this();
		file = new File(fileName);
		try {
			doc = reader.read(file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// If everything went fine up to this point, now we have
		// an XML document in memory and can try to validate.
	}

	public void setFile(File file) {
		this.file = file;
	}

	public boolean validateXml() {
		try {
			h.setHasErrors(false);
			doc = reader.read(file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			// e.printStackTrace();
			return false;
		}
		return !h.getHasErrors();
	}

	class handler implements ErrorHandler {

		private boolean hasErrors;

		public handler() {
			hasErrors = false;
		}

		public boolean getHasErrors() {
			return hasErrors;
		}

		public void setHasErrors(boolean b) {
			this.hasErrors = b;
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Line: " + exception.getLineNumber() + ") "
					+ exception.getMessage());
			hasErrors = true;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			// TODO Auto-generated method stub

		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			// TODO Auto-generated method stub

		}
	}
}
