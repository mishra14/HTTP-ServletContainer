package edu.upenn.cis.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.log4j.Logger;

/**
 * This is a wrapper class for overriding the flush method of PrintWriter
 * @author cis455
 *
 */
public class BufferedPrintWriter extends PrintWriter{

	private static final Logger logger = Logger.getLogger(BufferedPrintWriter.class);
	private Response response;
	
	public BufferedPrintWriter(Writer out, boolean autoFlush, Response response) {
		super(out, autoFlush);
		this.response = response;
	}
	@Override
	public void flush()
	{
		try {
			response.flushBuffer();
		} catch (IOException e) {
			logger.error("IOException while flushing the PrintWriter object");
		}
	}

}
