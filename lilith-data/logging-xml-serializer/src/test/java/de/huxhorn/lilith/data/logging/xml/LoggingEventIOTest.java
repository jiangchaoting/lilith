/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2010 Joern Huxhorn
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
package de.huxhorn.lilith.data.logging.xml;

import de.huxhorn.lilith.data.logging.ExtendedStackTraceElement;
import de.huxhorn.lilith.data.logging.LoggingEvent;
import de.huxhorn.lilith.data.logging.Marker;
import de.huxhorn.lilith.data.logging.Message;
import de.huxhorn.lilith.data.logging.ThreadInfo;
import de.huxhorn.lilith.data.logging.ThrowableInfo;
import de.huxhorn.lilith.data.eventsource.LoggerContext;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

public class LoggingEventIOTest
{
	private final Logger logger = LoggerFactory.getLogger(LoggingEventIOTest.class);

	@Test
	public void minimal()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		check(event);
	}

	@Test
	public void loggerContext()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		LoggerContext value = new LoggerContext();
		value.setName("ContextName");
		value.setBirthTime(1234567890000L);
		Map<String, String> propperties = new HashMap<String, String>();
		propperties.put("foo", "bar");
		value.setProperties(propperties);
		event.setLoggerContext(value);
		check(event);
	}

	@Test
	public void threadInfo()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		ThreadInfo threadInfo = new ThreadInfo(17L, "Thread-Name", 42L, "ThreadGroup-Name");
		event.setThreadInfo(threadInfo);
		check(event);
	}

	@Test
	public void arguments()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		String[] arguments = new String[]{"arg1", "arg2"};
		event.setMessage(new Message("message", arguments));
		check(event);
	}

	@Test
	public void nullArgument()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		String[] arguments = new String[]{"arg1", null, "arg3"};
		event.setMessage(new Message("message", arguments));
		check(event);
	}

	@Test
	public void singleThrowable()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		ThrowableInfo ti = createThrowableInfo("the.exception.class.Name", "Huhu! Exception Message");
		event.setThrowable(ti);
		check(event);
	}

	@Test
	public void multiThrowable()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		ThrowableInfo ti = createThrowableInfo("the.exception.class.Name", "Huhu! Exception Message");
		ThrowableInfo ti2 = createThrowableInfo("another.exception.class.Name", "Huhu! Exception Message");
		ThrowableInfo ti3 = createThrowableInfo("yet.another.exception.class.Name", "Huhu! Exception Message");
		ti.setCause(ti2);
		ti2.setCause(ti3);
		event.setThrowable(ti);
		check(event);
	}

	@Test
	public void mdc()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Map<String, String> mdc = new HashMap<String, String>();
		mdc.put("key1", "value1");
		mdc.put("key2", "value2");
		mdc.put("key3", "value3");
		event.setMdc(mdc);
		check(event);
	}

	@Test
	public void ndc()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Message[] ndc = new Message[]{
			new Message("message"),
			new Message("messagePattern {}", new String[]{"foo"})
		};
		event.setNdc(ndc);
		check(event);
	}

	@Test
	public void singleMarker()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Marker marker = new Marker("marker");
		event.setMarker(marker);
		check(event);
	}

	@Test
	public void childMarker()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Marker marker = new Marker("marker");
		Marker marker2_1 = new Marker("marker2-1");
		Marker marker2_2 = new Marker("marker2-2");
		marker.add(marker2_1);
		marker.add(marker2_2);
		event.setMarker(marker);
		check(event);
	}

	@Test
	public void recursiveMarker()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Marker marker = new Marker("marker");
		Marker marker2_1 = new Marker("marker2-1");
		Marker marker2_2 = new Marker("marker2-2");
		Marker marker3_1 = new Marker("marker3-1");
		marker.add(marker2_1);
		marker.add(marker2_2);
		marker2_2.add(marker3_1);
		marker3_1.add(marker2_1);
		event.setMarker(marker);
		check(event);
	}

	@Test
	public void callStack()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		event.setCallStack(createStackTraceElements());
		check(event);
	}

	@Test
	public void full()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();

		ThreadInfo threadInfo = new ThreadInfo(17L, "Thread-Name", 42L, "ThreadGroup-Name");
		event.setThreadInfo(threadInfo);

		String[] arguments = new String[]{"arg1", null, "arg3"};
		event.setMessage(new Message("message", arguments));

		ThrowableInfo ti = createThrowableInfo("the.exception.class.Name", "Huhu! Exception Message");
		ThrowableInfo ti2 = createThrowableInfo("another.exception.class.Name", "Huhu! Exception Message");
		ThrowableInfo ti3 = createThrowableInfo("yet.another.exception.class.Name", "Huhu! Exception Message");
		ti.setCause(ti2);
		ti2.setCause(ti3);
		event.setThrowable(ti);

		Map<String, String> mdc = new HashMap<String, String>();
		mdc.put("key1", "value1");
		mdc.put("key2", "value2");
		mdc.put("key3", "value3");
		event.setMdc(mdc);

		Message[] ndc = new Message[]{
			new Message("message"),
			new Message("messagePattern {}", new String[]{"foo"})
		};
		event.setNdc(ndc);

		Marker marker = new Marker("marker");
		Marker marker2_1 = new Marker("marker2-1");
		Marker marker2_2 = new Marker("marker2-2");
		Marker marker3_1 = new Marker("marker3-1");
		marker.add(marker2_1);
		marker.add(marker2_2);
		marker2_2.add(marker3_1);
		marker3_1.add(marker2_1);
		event.setMarker(marker);

		event.setCallStack(createStackTraceElements());
		check(event);
	}

	public LoggingEvent createMinimalEvent()
	{
		LoggingEvent event = new LoggingEvent();
		event.setLogger("Logger");
		event.setLevel(LoggingEvent.Level.INFO);
		event.setTimeStamp(1234567890000L);
		return event;
	}

	public ThrowableInfo createThrowableInfo(String className, String message)
	{
		ThrowableInfo ti = new ThrowableInfo();
		ti.setName(className);
		ti.setMessage(message);
		ti.setStackTrace(createStackTraceElements());
		return ti;
	}

	public ExtendedStackTraceElement[] createStackTraceElements()
	{
		//noinspection ThrowableInstanceNeverThrown
		Throwable t = new Throwable();
		StackTraceElement[] original = t.getStackTrace();

		ExtendedStackTraceElement[] result = new ExtendedStackTraceElement[original.length];
		for(int i = 0; i < original.length; i++)
		{
			StackTraceElement current = original[i];
			result[i] = new ExtendedStackTraceElement(current);

			if(i == 0)
			{
				// codeLocation, version and exact
				result[i].setCodeLocation("CodeLocation");
				result[i].setVersion("Version");
				result[i].setExact(true);
			}
			else if(i == 1)
			{
				// codeLocation, version and exact
				result[i].setCodeLocation("CodeLocation");
				result[i].setVersion("Version");
				result[i].setExact(false);
			}
		}

		return result;
	}

	public void check(LoggingEvent event)
		throws UnsupportedEncodingException, XMLStreamException
	{
		if(logger.isDebugEnabled()) logger.debug("Processing LoggingEvent:\n{}", event);
		byte[] bytes;
		LoggingEvent readEvent;

		bytes = write(event, false);
		readEvent = read(bytes, false);
		if(logger.isDebugEnabled()) logger.debug("LoggingEvent read uncompressed.");
		if(logger.isInfoEnabled()) logger.info("Original marker: {}", toString(event.getMarker()));
		if(logger.isInfoEnabled()) logger.info("Read     marker: {}", toString(readEvent.getMarker()));
		assertEquals(event, readEvent);

		bytes = write(event, true);
		readEvent = read(bytes, true);
		if(logger.isDebugEnabled()) logger.debug("LoggingEvent read compressed.");
		if(logger.isInfoEnabled()) logger.info("Original marker: {}", toString(event.getMarker()));
		if(logger.isInfoEnabled()) logger.info("Read     marker: {}", toString(readEvent.getMarker()));
	}

	public byte[] write(LoggingEvent event, boolean compressing)
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingXmlEncoder ser = new LoggingXmlEncoder(compressing);
		return ser.encode(event);
	}

	public LoggingEvent read(byte[] bytes, boolean compressing)
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingXmlDecoder des = new LoggingXmlDecoder(compressing);
		return des.decode(bytes);
	}

	String toString(Marker marker)
	{
		if(marker == null)
		{
			return null;
		}
		StringBuilder result = new StringBuilder();
		Map<String, Marker> processedMarkers = new HashMap<String, Marker>();
		recursiveToString(result, processedMarkers, marker);
		return result.toString();
	}

	private void recursiveToString(StringBuilder result, Map<String, Marker> processedMarkers, Marker marker)
	{
		if(processedMarkers.containsKey(marker.getName()))
		{
			result.append("Marker[ref=").append(marker.getName());
		}
		else
		{
			processedMarkers.put(marker.getName(), marker);
			result.append("Marker[name=").append(marker.getName());
			if(marker.hasReferences())
			{
				result.append(", children={");
				Map<String, Marker> children = marker.getReferences();
				boolean first = true;
				for(Map.Entry<String, Marker> current : children.entrySet())
				{
					if(first)
					{
						first = false;
					}
					else
					{
						result.append(", ");
					}
					recursiveToString(result, processedMarkers, current.getValue());
				}
				result.append("}");
			}
			result.append("]");
		}
	}
}
