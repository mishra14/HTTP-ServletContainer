package test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.upenn.cis.cis455.servlet.Context;

/**
 * Class to test the COntext class
 * @author cis455
 *
 */
public class ContextTest {
	
	/**
	 * test set and get init params
	 */
	 @Test
	  public void testSetAndGetInitParameters() {
	
		 String key = "testKey";
		 String value = "testValue";
	    Context context = new Context();
	    context.setInitParam(key, value);
	    assertEquals(value, context.getInitParameter(key));
	    
	 }
	 /**
	  * test set and get attributes method
	  */
	 @Test
	 public void testSetAndGetAttributes() {
		 
		String key = "testAttributeKey";
		String value = "testAttributeValue";
	    Context context = new Context();
	    context.setAttribute(key, value);
	    assertEquals(value, context.getAttribute(key));
	 }
	 
	 /**
	  * test remove attribute
	  */
	 @Test
	 public void testRemoveAttribute() {
		 
		String key = "testKey";
		String value = "testValue";
	    Context context = new Context();
	    context.setAttribute(key, value);
	    assertEquals(value, context.getAttribute(key));
	    context.removeAttribute(key);
	    assertEquals(null, context.getAttribute(key));

	 } 
}