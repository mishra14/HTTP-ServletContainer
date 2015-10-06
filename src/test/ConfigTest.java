package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import edu.upenn.cis.cis455.servlet.Config;
import edu.upenn.cis.cis455.servlet.Context;

public class ConfigTest {
	
	 @Test
	  public void testSetAndGetInitParameters() {
		 String name = "Test Config";
		 String key = "testKey";
		 String value = "testValue";
		 Context context = new Context();
	    Config config = new Config(name, context);
	    config.setInitParam(key, value);
	    assertEquals(value, config.getInitParameter(key));
	    
	 }
	 @Test
	 public void testGetServletName() {
		 
		String name = "Test Config";
		Context context = new Context();
	    Config config = new Config(name, context);
	    assertEquals(name, config.getServletName());
	 }
	 
	 @Test
	 public void testGetServletContext() {
		 
		 String name = "Test Config";
		 String key = "testKey";
		 String value = "testValue";
		 Context context = new Context();
	    Config config = new Config(name, context);
	    assertEquals(context, config.getServletContext());


	 } 
}