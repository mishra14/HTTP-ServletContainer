package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import edu.upenn.cis.cis455.servlet.Session;

public class SessionTest {
	
	 @Test
	 public void createdSessionShouldBeNew() {

	    Session session = new Session();
	    assertEquals(true, session.isNew());
	    
	    }
	 
	 @Test
	 public void createdSessionShouldBeValid() {

	    Session session = new Session();
	    assertEquals(true, session.isValid());
	    
	    }
	 @Test
	 public void testSetAndGetAttributes() {

	    Session session = new Session();
	    String name = "Name";
	    String value = "Value";
	    session.setAttribute(name, value);
	    assertEquals(value, session.getAttribute(name));
	    
	    }
}