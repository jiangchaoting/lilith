/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2009 Joern Huxhorn
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.huxhorn.lilith.log4j.xml;

import de.huxhorn.lilith.data.logging.ExtendedStackTraceElement;
import de.huxhorn.lilith.data.logging.LoggingEvent;
import de.huxhorn.lilith.data.logging.Message;
import de.huxhorn.lilith.data.logging.ThrowableInfo;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class LoggingEventReaderTest
{
	private final Logger logger = LoggerFactory.getLogger(LoggingEventReaderTest.class);
	private LoggingEventReader instance;

	@Before
	public void setUp()
	{
		instance = new LoggingEventReader();
	}

	@Test
	public void full()
		throws XMLStreamException, UnsupportedEncodingException
	{
		String eventString = "<log4j:event logger=\"de.huxhorn.lilith.sandbox.Log4jSandbox$InnerClass\" timestamp=\"1233135682640\" level=\"DEBUG\" thread=\"main\">\n" +
			"<log4j:message><![CDATA[Foo!]]></log4j:message>\n" +
			"<log4j:NDC><![CDATA[NDC1 NDC2]]></log4j:NDC>\n" +
			"<log4j:throwable><![CDATA[java.lang.RuntimeException: Hello\n" +
			"\tat de.huxhorn.lilith.sandbox.Log4jSandbox$InnerClass.execute(Log4jSandbox.java:18)\n" +
			"\tat de.huxhorn.lilith.sandbox.Log4jSandbox.main(Log4jSandbox.java:38)\n" +
			"Caused by: java.lang.RuntimeException: Hi.\n" +
			"\tat de.huxhorn.lilith.sandbox.Log4jSandbox$InnerClass.foobar(Log4jSandbox.java:24)\n" +
			"\tat de.huxhorn.lilith.sandbox.Log4jSandbox$InnerClass.execute(Log4jSandbox.java:14)\n" +
			"\t... 1 more\n" +
			"]]></log4j:throwable>\n" +
			"<log4j:locationInfo class=\"de.huxhorn.lilith.sandbox.Log4jSandbox$InnerClass\" method=\"execute\" file=\"Log4jSandbox.java\" line=\"18\"/>\n" +
			"<log4j:properties>\n" +
			"<log4j:data name=\"key1\" value=\"value1\"/>\n" +
			"<log4j:data name=\"key2\" value=\"value2\"/>\n" +
			"</log4j:properties>\n" +
			"</log4j:event>";
		LoggingEvent readEvent = read(eventString);
		logEvent(readEvent);
	}

	private LoggingEvent read(String eventStr)
		throws XMLStreamException, UnsupportedEncodingException
	{
		if(logger.isDebugEnabled()) logger.debug("Before change: {}", eventStr);
		if(!eventStr.contains("xmlns:log4j=\"http://jakarta.apache.org/log4j/\""))
		{
			eventStr = eventStr
				.replace("<log4j:event ", "<log4j:event xmlns:log4j=\"http://jakarta.apache.org/log4j/\" ");
			if(logger.isDebugEnabled()) logger.debug("After change: {}", eventStr);
		}
		return read((eventStr).getBytes("UTF-8"));
	}

	private void logEvent(LoggingEvent event)
	{
		if(logger.isInfoEnabled())
		{
			StringBuilder msg = new StringBuilder();
			msg.append("loggingEvent=");
			if(event == null)
			{
				msg.append((String) null);
			}
			else
			{
				msg.append("[");
				msg.append("logger=").append(event.getLogger());
				msg.append(", level=").append(event.getLevel());
				msg.append(", threadInfo=").append(event.getThreadInfo());
				msg.append(", timeStamp=").append(event.getTimeStamp());
				msg.append(", message=").append(event.getMessage());
				appendCallStack(msg, event.getCallStack());
				appendThrowable(msg, event.getThrowable());
				msg.append(", mdc=").append(event.getMdc());
				appendNdc(msg, event.getNdc());

				msg.append("]");
			}
			logger.info(msg.toString());
		}
	}

	private void appendNdc(StringBuilder msg, Message[] ndc)
	{
		if(ndc != null)
		{
			List<Message> list = Arrays.asList(ndc);
			msg.append(", ndc=").append(list);
		}
	}

	private void appendCallStack(StringBuilder msg, ExtendedStackTraceElement[] callStack)
	{
		if(callStack != null)
		{
			List<ExtendedStackTraceElement> list = Arrays.asList(callStack);
			msg.append(", callStack=").append(list);
		}
	}

	private void appendThrowable(StringBuilder msg, ThrowableInfo throwable)
	{
		if(throwable != null)
		{
			msg.append(", throwable=");
			msg.append(throwable);
		}
	}

	private LoggingEvent read(byte[] bytes)
		throws XMLStreamException, UnsupportedEncodingException
	{
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();

		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		XMLStreamReader reader = inputFactory.createXMLStreamReader(new InputStreamReader(in, "utf-8"));
		return instance.read(reader);
	}
}