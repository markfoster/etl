import java.util.*;
import org.w3c.dom.*;

public class ETLContext {

        private static Map audit = null;
        private static Map docs = null;

        private static final ETLContext context = new ETLContext();

        private ETLContext() { }

	public static ETLContext getContext() {
		return context;
	}

	public Map getAuditMap() {
		return this.audit;
	}

	public void setAuditMap(Map audit) {
		this.audit = audit;
	}

	public Map getDocumentMap() {
		return this.docs;
	}

	public void setDocumentMap(Map docs) {
		this.docs = docs;
	}
}
