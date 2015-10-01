
package uk.sipperfly.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * @author Rimsha Khalid(rimsha@avpreserve.com)
 */
class SimpleErrorHandler implements ErrorHandler {
	private String GACOM = "com.UKExactly";

	@Override
	public void warning(SAXParseException e) throws SAXException {
		Logger.getLogger(GACOM).log(Level.WARNING, "SAXParseException: ", e);
        System.out.println(e.getMessage());
    }

	@Override
    public void error(SAXParseException e) throws SAXException {
		Logger.getLogger(GACOM).log(Level.SEVERE, "SAXParseException: ", e);
        System.out.println(e.getMessage());
    }

	@Override
    public void fatalError(SAXParseException e) throws SAXException {
		Logger.getLogger(GACOM).log(Level.SEVERE, "SAXParseException: ", e);
        System.out.println(e.getMessage());
    }
}
