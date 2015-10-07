package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import edu.upenn.cis.cis455.servlet.Session;

/**
 * Test class for Session class
 * @author cis455
 *
 */
public class SessionTest {
	
	/**
	 * test a new session is new
	 */
	 @Test
	 public void createdSessionShouldBeNew() {

	    Session session = new Session();
	    assertEquals(true, session.isNew());
	    
	    }
	 /**
	  * test that a new session is valid
	  */
	 @Test
	 public void createdSessionShouldBeValid() {

	    Session session = new Session();
	    assertEquals(true, session.isValid());
	    
	    }
	 /**
	  * test set and set methods on session attributes
	  */
	 @Test
	 public void testSetAndGetAttributes() {

	    Session session = new Session();
	    String name = "Name";
	    String value = "Value";
	    session.setAttribute(name, value);
	    assertEquals(value, session.getAttribute(name));
	    
	    }
}