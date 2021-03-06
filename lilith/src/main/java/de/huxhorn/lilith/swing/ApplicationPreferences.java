/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2016 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.huxhorn.lilith.swing;

import de.huxhorn.lilith.Lilith;
import de.huxhorn.lilith.LilithSounds;
import de.huxhorn.lilith.conditions.CallLocationCondition;
import de.huxhorn.lilith.conditions.EventContainsCondition;
import de.huxhorn.lilith.conditions.FormattedMessageContainsCondition;
import de.huxhorn.lilith.conditions.FormattedMessageEqualsCondition;
import de.huxhorn.lilith.conditions.GroovyCondition;
import de.huxhorn.lilith.conditions.LevelCondition;
import de.huxhorn.lilith.conditions.LilithCondition;
import de.huxhorn.lilith.conditions.LoggerContainsCondition;
import de.huxhorn.lilith.conditions.LoggerEqualsCondition;
import de.huxhorn.lilith.conditions.LoggerStartsWithCondition;
import de.huxhorn.lilith.conditions.MessagePatternContainsCondition;
import de.huxhorn.lilith.conditions.MessagePatternEqualsCondition;
import de.huxhorn.lilith.conditions.ThreadGroupNameCondition;
import de.huxhorn.lilith.conditions.ThreadNameCondition;
import de.huxhorn.lilith.conditions.ThrowableCondition;
import de.huxhorn.lilith.data.access.HttpStatus;
import de.huxhorn.lilith.data.logging.LoggingEvent;
import de.huxhorn.lilith.prefs.LilithPreferences;
import de.huxhorn.lilith.swing.filefilters.GroovyConditionFileFilter;
import de.huxhorn.lilith.swing.preferences.SavedCondition;
import de.huxhorn.lilith.swing.table.ColorScheme;
import de.huxhorn.sulky.swing.PersistentTableColumnModel;
import de.huxhorn.sulky.conditions.Condition;

import de.huxhorn.sulky.io.IOUtilities;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ApplicationPreferences
{

	private static final Preferences PREFERENCES =
		Preferences.userNodeForPackage(ApplicationPreferences.class);

	private static final int MAX_PREV_SEARCHES = 15;
	private static final int MAX_RECENT_FILES = 15;

	private static final String PREVIOUS_SEARCH_STRINGS_XML_FILENAME = "previousSearchStrings.xml";
	private static final String RECENT_FILES_XML_FILENAME = "recentFiles.xml";
	public static final String STATUS_COLORS_XML_FILENAME = "statusColors.xml";
	public static final String LEVEL_COLORS_XML_FILENAME = "levelColors.xml";

	private static final String DETAILS_VIEW_ROOT_FOLDER = "detailsView";
	public static final String DETAILS_VIEW_CSS_FILENAME = "detailsView.css";
	public static final String DETAILS_VIEW_GROOVY_FILENAME = "detailsView.groovy";
	public static final String CONDITIONS_XML_FILENAME = "savedConditions.xml";

	public static final String STATUS_COLORS_PROPERTY = "statusColors";
	public static final String LEVEL_COLORS_PROPERTY = "levelColors";
	public static final String LOOK_AND_FEEL_PROPERTY = "lookAndFeel";
	public static final String CLEANING_LOGS_ON_EXIT_PROPERTY = "cleaningLogsOnExit";
	public static final String COLORING_WHOLE_ROW_PROPERTY = "coloringWholeRow";
	public static final String SHOWING_TOOLBAR_PROPERTY = "showingToolbar";
	public static final String SHOWING_STATUSBAR_PROPERTY = "showingStatusbar";
	public static final String SHOWING_PRIMARY_IDENTIFIER_PROPERTY = "showingPrimaryIdentifier";
	public static final String SHOWING_SECONDARY_IDENTIFIER_PROPERTY = "showingSecondaryIdentifier";
	public static final String SHOWING_FULL_CALLSTACK_PROPERTY = "showingFullCallstack";
	public static final String USING_WRAPPED_EXCEPTION_STYLE_PROPERTY = "usingWrappedExceptionStyle";
	public static final String SHOWING_STACKTRACE_PROPERTY = "showingStackTrace";
	public static final String CHECKING_FOR_UPDATE_PROPERTY = "checkingForUpdate";
	public static final String CHECKING_FOR_SNAPSHOT_PROPERTY = "checkingForSnapshot";
	public static final String SOURCE_FILTERING_PROPERTY = "sourceFiltering";
	public static final String SOUND_LOCATIONS_PROPERTY = "soundLocations";
	public static final String SCALE_FACTOR_PROPERTY = "scaleFactor";
	public static final String MUTE_PROPERTY = "mute";
	public static final String USING_INTERNAL_FRAMES_PROPERTY = "usingInternalFrames";
	public static final String SCROLLING_TO_BOTTOM_PROPERTY = "scrollingToBottom";
	public static final String SOURCE_NAMES_PROPERTY = "sourceNames";
	public static final String APPLICATION_PATH_PROPERTY = "applicationPath";
	public static final String TRAY_ACTIVE_PROPERTY = "trayActive";
	public static final String HIDING_ON_CLOSE_PROPERTY = "hidingOnClose";
	public static final String AUTO_OPENING_PROPERTY = "autoOpening";
	public static final String AUTO_CLOSING_PROPERTY = "autoClosing";
	public static final String IMAGE_PATH_PROPERTY = "imagePath";
	public static final String SOUND_PATH_PROPERTY = "soundPath";
	public static final String AUTO_FOCUSING_WINDOW_PROPERTY = "autoFocusingWindow";
	public static final String SOURCE_LISTS_PROPERTY = "sourceLists";
	public static final String BLACK_LIST_NAME_PROPERTY = "blackListName";
	public static final String WHITE_LIST_NAME_PROPERTY = "whiteListName";
	public static final String CONDITIONS_PROPERTY = "conditions";
	public static final String SPLASH_SCREEN_DISABLED_PROPERTY = "splashScreenDisabled";
	public static final String ASKING_BEFORE_QUIT_PROPERTY = "askingBeforeQuit";
	public static final String CURRENT_TIP_OF_THE_DAY_PROPERTY = "currentTipOfTheDay";
	public static final String SHOWING_TIP_OF_THE_DAY_PROPERTY = "showingTipOfTheDay";
	public static final String MAXIMIZING_INTERNAL_FRAMES_PROPERTY = "maximizingInternalFrames";
	public static final String GLOBAL_LOGGING_ENABLED_PROPERTY = "globalLoggingEnabled";
	public static final String LOGGING_STATISTIC_ENABLED_PROPERTY = "loggingStatisticEnabled";
	public static final String PREVIOUS_SEARCH_STRINGS_PROPERTY = "previousSearchStrings";
	public static final String RECENT_FILES_PROPERTY = "recentFiles";
	public static final String SHOWING_FULL_RECENT_PATH_PROPERTY="showingFullRecentPath";
	public static final String DEFAULT_CONDITION_NAME_PROPERTY = "defaultConditionName";


	public static final String LOGGING_LAYOUT_GLOBAL_XML_FILENAME = "loggingLayoutGlobal.xml";
	public static final String LOGGING_LAYOUT_XML_FILENAME = "loggingLayout.xml";
	public static final String ACCESS_LAYOUT_GLOBAL_XML_FILENAME = "accessLayoutGlobal.xml";
	public static final String ACCESS_LAYOUT_XML_FILENAME = "accessLayout.xml";

	public static final String SOURCE_NAMES_XML_FILENAME = "SourceNames.xml";
	public static final String SOURCE_LISTS_XML_FILENAME = "SourceLists.xml";
	public static final String SOURCE_NAMES_PROPERTIES_FILENAME = "SourceNames.properties";
	public static final String SOUND_LOCATIONS_XML_FILENAME = "SoundLocations.xml";
	//public static final String SOUND_LOCATIONS_PROPERTIES_FILENAME = "SoundLocations.properties";
	public static final String PREVIOUS_APPLICATION_PATH_FILENAME = ".previous.application.path";

	private static final String OLD_LICENSED_PREFERENCES_KEY = "licensed";
	private static final String LICENSED_PREFERENCES_KEY = "licensedVersion";
	public static final String USER_HOME;
	public static final String DEFAULT_APPLICATION_PATH;
	private static final Map<String, String> DEFAULT_SOURCE_NAMES;
	private static final Map<String, String> DEFAULT_SOUND_LOCATIONS;
	private static final Map<LoggingEvent.Level, ColorScheme> DEFAULT_LEVEL_COLOR_SCHEMES;
	private static final Map<HttpStatus.Type, ColorScheme> DEFAULT_STATUS_COLOR_SCHEMES;
	private static final String PREVIOUS_OPEN_PATH_PROPERTY = "previousOpenPath";
	private static final String PREVIOUS_IMPORT_PATH_PROPERTY = "previousImportPath";
	private static final String PREVIOUS_EXPORT_PATH_PROPERTY = "previousExportPath";

	public static final String STARTUP_LOOK_AND_FEEL;

	private static final long CONDITIONS_CHECK_INTERVAL = 30000;
	private static final String GROOVY_SUFFIX = ".groovy";
	private static final String EXAMPLE_GROOVY_CONDITIONS_BASE = "/conditions/";
	private static final String EXAMPLE_GROOVY_CLIPBOARD_FORMATTERS_BASE = "/clipboardFormatters/";
	private static final String GROOVY_EXAMPLE_LIST = "list.txt";

	public static final String SAVED_CONDITION = "Saved";

	private static final String[] DEFAULT_CONDITIONS = new String[]{
		EventContainsCondition.DESCRIPTION,
		LevelCondition.DESCRIPTION,
		FormattedMessageContainsCondition.DESCRIPTION,
		FormattedMessageEqualsCondition.DESCRIPTION,
		MessagePatternContainsCondition.DESCRIPTION,
		MessagePatternEqualsCondition.DESCRIPTION,
		LoggerStartsWithCondition.DESCRIPTION,
		LoggerContainsCondition.DESCRIPTION,
		LoggerEqualsCondition.DESCRIPTION,
		CallLocationCondition.DESCRIPTION,
		ThrowableCondition.DESCRIPTION,
		ThreadNameCondition.DESCRIPTION,
		ThreadGroupNameCondition.DESCRIPTION,
		SAVED_CONDITION,
	};

	private static final String[] LEVEL_VALUES = {
			"TRACE", "DEBUG", "INFO", "WARN", "ERROR"
	};
	private String[] clipboardFormatterScriptFiles;
	private long lastClipboardFormatterCheck;

	private static final LilithPreferences DEFAULT_VALUES=new LilithPreferences();

	static
	{
		PREFERENCES.remove(OLD_LICENSED_PREFERENCES_KEY); // remove garbage

		USER_HOME = System.getProperty("user.home");
		File defaultAppPath = new File(USER_HOME, ".lilith");
		DEFAULT_APPLICATION_PATH = defaultAppPath.getAbsolutePath();

		Map<String, String> defaultSoundLocations = new HashMap<>();
		defaultSoundLocations.put(LilithSounds.SOURCE_ADDED, "/events/SourceAdded.mp3");
		defaultSoundLocations.put(LilithSounds.SOURCE_REMOVED, "/events/SourceRemoved.mp3");
		defaultSoundLocations.put(LilithSounds.ERROR_EVENT_ALARM, "/events/ErrorEventAlarm.mp3");
		defaultSoundLocations.put(LilithSounds.WARN_EVENT_ALARM, "/events/WarnEventAlarm.mp3");
		DEFAULT_SOUND_LOCATIONS = Collections.unmodifiableMap(defaultSoundLocations);

		Map<String, String> defaultSourceNames = new HashMap<>();
		defaultSourceNames.put("127.0.0.1", "Localhost");
		DEFAULT_SOURCE_NAMES = Collections.unmodifiableMap(defaultSourceNames);

		HashMap<LoggingEvent.Level, ColorScheme> defaultLevelColors = new HashMap<>();
		defaultLevelColors.put(LoggingEvent.Level.TRACE,
			new ColorScheme(new Color(0x1F, 0x44, 0x58), new Color(0x80, 0xBA, 0xD9), new Color(0x80, 0xBA, 0xD9)));
		defaultLevelColors.put(LoggingEvent.Level.DEBUG,
			new ColorScheme(Color.BLACK, Color.GREEN, Color.GREEN));
		defaultLevelColors.put(LoggingEvent.Level.INFO,
			new ColorScheme(Color.BLACK, Color.WHITE, Color.WHITE));
		defaultLevelColors.put(LoggingEvent.Level.WARN,
			new ColorScheme(Color.BLACK, Color.YELLOW, Color.YELLOW));
		defaultLevelColors.put(LoggingEvent.Level.ERROR,
			new ColorScheme(Color.YELLOW, Color.RED, Color.ORANGE));
		DEFAULT_LEVEL_COLOR_SCHEMES = Collections.unmodifiableMap(defaultLevelColors);

		HashMap<HttpStatus.Type, ColorScheme> defaultStatusColors = new HashMap<>();
		defaultStatusColors.put(HttpStatus.Type.SUCCESSFUL,
			new ColorScheme(Color.BLACK, Color.GREEN, Color.GREEN));
		defaultStatusColors.put(HttpStatus.Type.INFORMATIONAL,
			new ColorScheme(Color.BLACK, Color.WHITE, Color.WHITE));
		defaultStatusColors.put(HttpStatus.Type.REDIRECTION,
			new ColorScheme(Color.BLACK, Color.YELLOW, Color.YELLOW));
		defaultStatusColors.put(HttpStatus.Type.CLIENT_ERROR,
			new ColorScheme(Color.GREEN, Color.RED, Color.ORANGE));
		defaultStatusColors.put(HttpStatus.Type.SERVER_ERROR,
			new ColorScheme(Color.YELLOW, Color.RED, Color.ORANGE));
		DEFAULT_STATUS_COLOR_SCHEMES = Collections.unmodifiableMap(defaultStatusColors);

		String lafName = null;
		LookAndFeel laf = UIManager.getLookAndFeel();
		if(laf != null)
		{
			lafName = laf.getName();
		}
		STARTUP_LOOK_AND_FEEL = lafName;
	}

	private boolean usingScreenMenuBar;

	/**
	 * Creates a condition of the given name and value.
	 *
	 * @param conditionName the name of the condition
	 * @param value the value for the condition
	 * @return the created condition
	 * @throws IllegalArgumentException if value is not allowed for conditionName.
	 */
	public Condition createCondition(String conditionName, String value)
	{
		if(conditionName == null)
		{
			throw new NullPointerException("conditionName must not be null!");
		}

		switch(conditionName)
		{
			case EventContainsCondition.DESCRIPTION:
				return new EventContainsCondition(value);

			case LevelCondition.DESCRIPTION:
				boolean found = false;
				for(String current : LEVEL_VALUES)
				{
					if(current.equalsIgnoreCase(value))
					{
						value=current;
						found=true;
					}
				}
				if(found)
				{
					return new LevelCondition(value);
				}
				throw new IllegalArgumentException("Unknown level value '"+value+"'!");

			case FormattedMessageContainsCondition.DESCRIPTION:
				return new FormattedMessageContainsCondition(value);

			case FormattedMessageEqualsCondition.DESCRIPTION:
				return new FormattedMessageEqualsCondition(value);

			case MessagePatternContainsCondition.DESCRIPTION:
				return new MessagePatternContainsCondition(value);

			case MessagePatternEqualsCondition.DESCRIPTION:
				return new MessagePatternEqualsCondition(value);

			case LoggerStartsWithCondition.DESCRIPTION:
				return new LoggerStartsWithCondition(value);

			case LoggerContainsCondition.DESCRIPTION:
				return new LoggerContainsCondition(value);

			case LoggerEqualsCondition.DESCRIPTION:
				return new LoggerEqualsCondition(value);

			case ThrowableCondition.DESCRIPTION:
				return new ThrowableCondition(value);

			case ThreadNameCondition.DESCRIPTION:
				return new ThreadNameCondition(value);

			case ThreadGroupNameCondition.DESCRIPTION:
				return new ThreadGroupNameCondition(value);

			case CallLocationCondition.DESCRIPTION:
				return new CallLocationCondition(value);

			case SAVED_CONDITION:
				SavedCondition savedCondition = resolveSavedCondition(value);
				if(savedCondition != null)
				{
					return savedCondition.getCondition();
				}
				throw new IllegalArgumentException("Couldn't find saved condition named '" + value + "'.");

			default: // we assume a groovy condition...
				File resolvedScriptFile = resolveGroovyConditionScriptFile(conditionName);
				if(resolvedScriptFile != null)
				{
					// there is a file...
					return new GroovyCondition(resolvedScriptFile.getAbsolutePath(), value);
				}
				throw new IllegalArgumentException("Couldn't find condition '"+conditionName+"'!");
		}
	}

	public String resolveConditionName(Condition condition)
	{
		if(condition instanceof GroovyCondition)
		{
			// special handling of script files, even though it's also a LilithCondition
			GroovyCondition groovyCondition = (GroovyCondition) condition;
			String scriptFileName = groovyCondition.getScriptFileName();
			if(scriptFileName != null)
			{
				File scriptFile = new File(scriptFileName);
				// return the pure filename without the path
				return scriptFile.getName();
			}
			return null;
		}

		if(condition instanceof LilithCondition)
		{
			return ((LilithCondition)condition).getDescription();
		}
		// TODO? Special handling of SAVED_CONDITION

		return null;
	}

	public List<String> retrieveLevelValues()
	{
		return Arrays.asList(LEVEL_VALUES);
	}

	public List<String> retrieveAllConditions()
	{
		List<String> itemsVector = new ArrayList<>();

		itemsVector.addAll(Arrays.asList(DEFAULT_CONDITIONS));

		String[] groovyConditions = getAllGroovyConditionScriptFiles();
		if(groovyConditions != null)
		{
			itemsVector.addAll(Arrays.asList(groovyConditions));
		}
		return itemsVector;
	}

	private final Logger logger = LoggerFactory.getLogger(ApplicationPreferences.class);

	private PropertyChangeSupport propertyChangeSupport;

	private File startupApplicationPath;

	private File detailsViewRoot;

	private List<String> installedLookAndFeels;
	private String[] conditionScriptFiles;
	private long lastConditionsCheck;

	private Map<LoggingEvent.Level, ColorScheme> levelColors;
	private Map<HttpStatus.Type, ColorScheme> statusColors;

	private URL detailsViewRootUrl;

	/**
	 * Identifier => Name
	 */
	private Map<String, String> sourceNames;
	private long lastSourceNamesModified;

	private long lastConditionsModified;

	private Map<String, String> soundLocations;
	private long lastSoundLocationsModified;

	private Map<String, Set<String>> sourceLists;
	private long lastSourceListsModified;

	private LilithPreferences.SourceFiltering sourceFiltering;

	private Set<String> blackList;
	private Set<String> whiteList;
	private List<SavedCondition> conditions;
	private List<String> previousSearchStrings;

	private List<String> recentFiles;

	private File groovyConditionsPath;
	private File groovyClipboardFormattersPath;

	public ApplicationPreferences()
	{
		lastSourceNamesModified = -1;
		lastConditionsModified = -1;
		propertyChangeSupport = new PropertyChangeSupport(this);
		startupApplicationPath = getApplicationPath();

		installedLookAndFeels = new ArrayList<>();
		for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
		{
			installedLookAndFeels.add(info.getName());
		}
		Collections.sort(installedLookAndFeels);

		groovyConditionsPath = new File(startupApplicationPath, "conditions");
		if(groovyConditionsPath.mkdirs())
		{
			// groovy conditions directory was generated, create examples...
			installExampleConditions();
		}
		groovyClipboardFormattersPath = new File(startupApplicationPath, "clipboardFormatters");
		if(groovyClipboardFormattersPath.mkdirs())
		{
			// groovy clipboardFormatters directory was generated, create examples...
			installExampleClipboardFormatters();
		}
	}

	public File getGroovyConditionsPath()
	{
		return groovyConditionsPath;
	}

	public File getGroovyClipboardFormattersPath()
	{
		return groovyClipboardFormattersPath;
	}

	public void addRecentFile(File dataFile)
	{
		if(dataFile == null)
		{
			return;
		}
		if(!dataFile.isFile() || !dataFile.canRead())
		{
			if(logger.isWarnEnabled()) logger.warn("Tried to add invalid recent file.");
		}
		String absName=dataFile.getAbsolutePath();

		List<String> recents = getRecentFiles();
		recents.remove(absName); // remove previous entry if available
		recents.add(0, absName); // add to start of list.
		setRecentFiles(recents);
	}

	public void removeRecentFile(File dataFile)
	{
		if(dataFile == null)
		{
			return;
		}
		String absName=dataFile.getAbsolutePath();

		List<String> recents = getRecentFiles();
		recents.remove(absName); // remove previous entry if available
		setRecentFiles(recents);
	}


	private void setRecentFiles(List<String> recents)
	{
		List<String> copy;
		if(recents == null)
		{
			copy=new ArrayList<>();
		}
		else
		{
			copy=new ArrayList<>(recents);
		}
		Iterator<String> iter = copy.iterator();
		while(iter.hasNext())
		{
			String current = iter.next();
			File f=new File(current);
			if(!f.isFile() || !f.canRead())
			{
				iter.remove();
			}
			// Cleanup before setting...
		}
		Object oldValue=getRecentFiles();
		while(copy.size() > MAX_RECENT_FILES)
		{
			copy.remove(MAX_RECENT_FILES);
		}
		writeRecentFiles(copy);
		Object newValue=getRecentFiles();
		propertyChangeSupport.firePropertyChange(RECENT_FILES_PROPERTY, oldValue, newValue);
		if(logger.isInfoEnabled()) logger.info("recentFiles set to {}.", newValue);
	}

	public void clearRecentFiles()
	{
		setRecentFiles(new ArrayList<>());
	}

	public List<String> getRecentFiles()
	{
		initRecentFiles();
		return new ArrayList<>(recentFiles);
	}

	public void clearPreviousSearchStrings()
	{
		setPreviousSearchStrings(new ArrayList<>());
	}

	/**
	 * This will prevent unchecked warnings and will also validate the content properly.
	 *
	 * @param iface the expected type of the elements.
	 * @param obj the input Object, ideally a List of the given type
	 * @return the input as a List of the given type.
	 */
	private static <T> List<T> transformToList(Class<T> iface, Object obj)
	{
		final Logger logger = LoggerFactory.getLogger(ApplicationPreferences.class);

		List<T> resultList = null;
		if(obj instanceof List)
		{
			List list = (List) obj;
			resultList = new ArrayList<>(list.size());
			for(Object current:list)
			{
				if(iface.isInstance(current))
				{
					resultList.add(iface.cast(current));
				}
				else
				{
					if(logger.isWarnEnabled()) logger.warn("Expected {} but got {}!", iface.getName(), current);
				}
			}
		}
		else
		{
			if(logger.isWarnEnabled()) logger.warn("Expected List but got {}!", obj);
		}
		return resultList;
	}

	/**
	 * This will prevent unchecked warnings and will also validate the content properly.
	 *
	 * @param iface the expected type of the elements.
	 * @param obj the input Object, ideally a Set of the given type
	 * @return the input as a Set of the given type.
	 */
	private static <T> Set<T> transformToSet(Class<T> iface, Object obj)
	{
		final Logger logger = LoggerFactory.getLogger(ApplicationPreferences.class);

		Set<T> resultSet = null;
		if(obj instanceof Set)
		{
			Set set = (Set) obj;
			resultSet = new HashSet<>(set.size());
			for(Object current:set)
			{
				if(iface.isInstance(current))
				{
					resultSet.add(iface.cast(current));
				}
				else
				{
					if(logger.isWarnEnabled()) logger.warn("Expected {} but got {}!", iface.getName(), current);
				}
			}
		}
		else
		{
			if(logger.isWarnEnabled()) logger.warn("Expected Set but got {}!", obj);
		}
		return resultSet;
	}

	/**
	 * This will prevent unchecked warnings and will also validate the content properly.
	 *
	 * @param keyClass the expected type of the keys.
	 * @param valueClass the expected type of the values.
	 * @param obj the input Object, ideally a Map of the given types
	 * @return the input as a Map of the given types.
	 */
	private static <K,V> Map<K,V> transformToMap(Class<K> keyClass, Class<V> valueClass, Object obj)
	{
		final Logger logger = LoggerFactory.getLogger(ApplicationPreferences.class);

		Map<K,V> resultMap = null;
		if(obj instanceof Map)
		{
			Map map = (Map) obj;
			resultMap = new HashMap<>(map.size());
			for(Object c:map.entrySet())
			{
				Map.Entry current = (Map.Entry) c;
				Object key = current.getKey();
				Object value = current.getValue();

				if(!keyClass.isInstance(key))
				{
					if(logger.isWarnEnabled()) logger.warn("Expected {} as key but got {}!", keyClass.getName(), key);
					continue;
				}
				if(!valueClass.isInstance(value))
				{
					if(logger.isWarnEnabled()) logger.warn("Expected {} as value but got {}!", valueClass.getName(), value);
					continue;
				}
				resultMap.put(keyClass.cast(key), valueClass.cast(value));
			}
		}
		else
		{
			if(logger.isWarnEnabled()) logger.warn("Expected Map but got {}!", obj);
		}
		return resultMap;
	}

	private void initRecentFiles()
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, RECENT_FILES_XML_FILENAME);

		if(file.isFile() && this.recentFiles == null)
		{
			XMLDecoder d = null;
			try
			{
				d = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));

				this.recentFiles = transformToList(String.class, d.readObject());
			}
			catch(Throwable ex)
			{
				if(logger.isWarnEnabled()) logger.warn("Exception while loading recentFiles from file '" + file.getAbsolutePath() + "'!", ex);
				IOUtilities.interruptIfNecessary(ex);
			}
			finally
			{
				if(d != null)
				{
					d.close();
				}
			}
		}

		if(this.recentFiles == null)
		{
			this.recentFiles = new ArrayList<>();
		}
	}

	private boolean writeRecentFiles(List<String> recentFiles)
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, RECENT_FILES_XML_FILENAME);
		XMLEncoder e = null;
		Throwable error = null;
		try
		{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			e = new XMLEncoder(bos);
			e.writeObject(recentFiles);
		}
		catch(FileNotFoundException ex)
		{
			error = ex;
		}
		finally
		{
			if(e != null)
			{
				e.close();
			}
		}
		this.recentFiles = null;
		if(error != null)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while writing recentFiles!", error);
			return false;
		}
		return true;
	}

	public void addPreviousSearchString(String searchString)
	{
		if(searchString == null)
		{
			return;
		}

		if(searchString.trim().length() == 0)
		{
			// ignore whitespace-only strings
			return;
		}
		List<String> previousSearchStrings=getPreviousSearchStrings();
		if(previousSearchStrings.contains(searchString))
		{
			// remove previous string so it'll be at position 0
			previousSearchStrings.remove(searchString);
		}
		previousSearchStrings.add(0, searchString);
		setPreviousSearchStrings(previousSearchStrings);
	}

	private void setPreviousSearchStrings(List<String> previousSearchStrings)
	{
		Object oldValue=getPreviousSearchStrings();
		while(previousSearchStrings.size() > MAX_PREV_SEARCHES)
		{
			previousSearchStrings.remove(MAX_PREV_SEARCHES);
		}
		writePreviousSearchStrings(previousSearchStrings);
		Object newValue=getPreviousSearchStrings();
		propertyChangeSupport.firePropertyChange(PREVIOUS_SEARCH_STRINGS_PROPERTY, oldValue, newValue);
		if(logger.isInfoEnabled()) logger.info("previousSearchStrings set to {}.", newValue);
	}

	public List<String> getPreviousSearchStrings()
	{
		initPreviousSearchStrings();
		return new ArrayList<>(previousSearchStrings);
	}

	private void initPreviousSearchStrings()
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, PREVIOUS_SEARCH_STRINGS_XML_FILENAME);

		if(file.isFile() && this.previousSearchStrings == null)
		{
			XMLDecoder d = null;
			try
			{
				d = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));

				this.previousSearchStrings = transformToList(String.class, d.readObject());
			}
			catch(Throwable ex)
			{
				if(logger.isWarnEnabled()) logger.warn("Exception while loading previous search strings from file '" + file.getAbsolutePath() + "'!", ex);
				IOUtilities.interruptIfNecessary(ex);
			}
			finally
			{
				if(d != null)
				{
					d.close();
				}
			}
		}

		if(this.previousSearchStrings == null)
		{
			this.previousSearchStrings = new ArrayList<>();
		}
	}

	private boolean writePreviousSearchStrings(List<String> searchStrings)
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, PREVIOUS_SEARCH_STRINGS_XML_FILENAME);
		XMLEncoder e = null;
		Throwable error = null;
		try
		{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			e = new XMLEncoder(bos);
			e.writeObject(searchStrings);
		}
		catch(FileNotFoundException ex)
		{
			error = ex;
		}
		finally
		{
			if(e != null)
			{
				e.close();
			}
		}
		this.previousSearchStrings = null;
		if(error != null)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while writing previous search strings!", error);
			return false;
		}
		return true;
	}

	public File resolveGroovyConditionScriptFile(String input)
	{
		if(input == null)
		{
			return null;
		}
		if(!input.endsWith(GROOVY_SUFFIX))
		{
			input = input + GROOVY_SUFFIX;
		}
		File scriptFile = new File(groovyConditionsPath, input);
		if(scriptFile.isFile())
		{
			return scriptFile;
		}
		return null;
	}

	public String[] getAllGroovyConditionScriptFiles()
	{
		if(conditionScriptFiles == null || ((System
			.currentTimeMillis() - lastConditionsCheck) > CONDITIONS_CHECK_INTERVAL))
		{

			File[] groovyFiles = groovyConditionsPath.listFiles(new GroovyConditionFileFilter());
			if(groovyFiles != null && groovyFiles.length > 0)
			{
				conditionScriptFiles = new String[groovyFiles.length];
				for(int i = 0; i < groovyFiles.length; i++)
				{
					File current = groovyFiles[i];
					conditionScriptFiles[i] = current.getName();
				}
				Arrays.sort(conditionScriptFiles);
				lastConditionsCheck = System.currentTimeMillis();
			}
		}
		return conditionScriptFiles;
	}

	public File resolveClipboardFormatterScriptFile(String input)
	{
		if(input == null)
		{
			return null;
		}
		if(!input.endsWith(GROOVY_SUFFIX))
		{
			input = input + GROOVY_SUFFIX;
		}
		File scriptFile = new File(groovyClipboardFormattersPath, input);
		if(scriptFile.isFile())
		{
			return scriptFile;
		}
		return null;
	}

	public String[] getClipboardFormatterScriptFiles()
	{
		if(clipboardFormatterScriptFiles == null || ((System.currentTimeMillis() - lastClipboardFormatterCheck) > CONDITIONS_CHECK_INTERVAL))
		{

			File[] groovyFiles = groovyClipboardFormattersPath.listFiles(new GroovyConditionFileFilter());
			if(groovyFiles != null && groovyFiles.length > 0)
			{
				clipboardFormatterScriptFiles = new String[groovyFiles.length];
				for(int i = 0; i < groovyFiles.length; i++)
				{
					File current = groovyFiles[i];
					clipboardFormatterScriptFiles[i] = current.getName();
				}
				Arrays.sort(clipboardFormatterScriptFiles);
				lastClipboardFormatterCheck = System.currentTimeMillis();
			}
			else
			{
				clipboardFormatterScriptFiles = null;
			}
		}
		return clipboardFormatterScriptFiles;
	}

	public void installExampleConditions()
	{
		String path = EXAMPLE_GROOVY_CONDITIONS_BASE + GROOVY_EXAMPLE_LIST;
		URL url = ApplicationPreferences.class.getResource(path);
		if(url == null)
		{
			if(logger.isErrorEnabled()) logger.error("Couldn't find resource at {}!", path);
		}
		else
		{
			List<String> lines = readLines(url);
			for(String current : lines)
			{
				path = EXAMPLE_GROOVY_CONDITIONS_BASE + current;
				url = ApplicationPreferences.class.getResource(path);
				if(url == null)
				{
					if(logger.isErrorEnabled()) logger.error("Couldn't find resource at {}!", path);
					continue;
				}
				File target = new File(groovyConditionsPath, current);
				copy(url, target, true);
			}
		}
	}

	public void installExampleClipboardFormatters()
	{
		String path = EXAMPLE_GROOVY_CLIPBOARD_FORMATTERS_BASE + GROOVY_EXAMPLE_LIST;
		URL url = ApplicationPreferences.class.getResource(path);
		if(url == null)
		{
			if(logger.isErrorEnabled()) logger.error("Couldn't find resource at {}!", path);
		}
		else
		{
			List<String> lines = readLines(url);
			for(String current : lines)
			{
				path = EXAMPLE_GROOVY_CLIPBOARD_FORMATTERS_BASE + current;
				url = ApplicationPreferences.class.getResource(path);
				if(url == null)
				{
					if(logger.isErrorEnabled()) logger.error("Couldn't find resource at {}!", path);
					continue;
				}
				File target = new File(groovyClipboardFormattersPath, current);
				copy(url, target, true);
			}
		}
	}

	private void initLevelColors()
	{
		if(levelColors == null)
		{
			File appPath = getStartupApplicationPath();
			File levelColorsFile = new File(appPath, LEVEL_COLORS_XML_FILENAME);

			if(levelColorsFile.isFile())
			{
				XMLDecoder d = null;
				try
				{
					d = new XMLDecoder(new BufferedInputStream(new FileInputStream(levelColorsFile)));

					levelColors = transformToMap(LoggingEvent.Level.class, ColorScheme.class, d.readObject());
				}
				catch(Throwable ex)
				{
					if(logger.isWarnEnabled()) logger.warn("Exception while loading Level-ColorSchemes from file '"	+ levelColorsFile.getAbsolutePath() + "'!", ex);
					levelColors = null;
					IOUtilities.interruptIfNecessary(ex);
				}
				finally
				{
					if(d != null)
					{
						d.close();
					}
				}
			}
		}

		if(levelColors != null && levelColors.size() != DEFAULT_LEVEL_COLOR_SCHEMES.size())
		{
			if(logger.isWarnEnabled()) logger.warn("Reverting Level-ColorSchemes to defaults.");
			levelColors = null;
		}

		if(levelColors == null)
		{
			levelColors = cloneLevelColors(DEFAULT_LEVEL_COLOR_SCHEMES);
		}
	}

	private Map<LoggingEvent.Level, ColorScheme> cloneLevelColors(Map<LoggingEvent.Level, ColorScheme> input)
	{
		if(input != null && input.size() != DEFAULT_LEVEL_COLOR_SCHEMES.size())
		{
			if(logger.isWarnEnabled()) logger.warn("Reverting Level-ColorSchemes to defaults.");
			input = null;
		}

		if(input == null)
		{
			input = DEFAULT_LEVEL_COLOR_SCHEMES;
		}

		Map<LoggingEvent.Level, ColorScheme> result = new HashMap<>();

		for(Map.Entry<LoggingEvent.Level, ColorScheme> current : input.entrySet())
		{
			try
			{
				result.put(current.getKey(), current.getValue().clone());
			}
			catch(CloneNotSupportedException ex)
			{
				if(logger.isErrorEnabled()) logger.error("Exception while cloning Level-ColorScheme!!", ex);
			}
		}
		return result;
	}

	public void setLevelColors(Map<LoggingEvent.Level, ColorScheme> colors)
	{
		Object oldValue = getLevelColors();
		colors = cloneLevelColors(colors);
		writeLevelColors(colors);
		this.levelColors = colors;
		Object newValue = getLevelColors();
		propertyChangeSupport.firePropertyChange(LEVEL_COLORS_PROPERTY, oldValue, newValue);
		if(logger.isInfoEnabled()) logger.info("LevelColors set to {}.", this.levelColors);
	}

	private void writeLevelColors(Map<LoggingEvent.Level, ColorScheme> colors)
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, LEVEL_COLORS_XML_FILENAME);
		try
		{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			XMLEncoder e = new XMLEncoder(bos);
			PersistenceDelegate delegate = new EnumPersistenceDelegate();
			e.setPersistenceDelegate(LoggingEvent.Level.class, delegate);
			e.writeObject(colors);
			e.close();
		}
		catch(Throwable ex)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while writing colors!", ex);
			IOUtilities.interruptIfNecessary(ex);
		}
	}

	public Map<LoggingEvent.Level, ColorScheme> getLevelColors()
	{
		if(levelColors == null)
		{
			initLevelColors();
		}
		return cloneLevelColors(levelColors);
	}

	private void initStatusColors()
	{
		if(statusColors == null)
		{
			File appPath = getStartupApplicationPath();
			File statusColorsFile = new File(appPath, STATUS_COLORS_XML_FILENAME);

			if(statusColorsFile.isFile())
			{
				XMLDecoder d = null;
				try
				{
					d = new XMLDecoder(new BufferedInputStream(new FileInputStream(statusColorsFile)));

					statusColors = transformToMap(HttpStatus.Type.class, ColorScheme.class, d.readObject());
				}
				catch(Throwable ex)
				{
					if(logger.isWarnEnabled()) logger.warn("Exception while loading status Status-ColorSchemes from file '" + statusColorsFile.getAbsolutePath() + "'!", ex);
					statusColors = null;
					IOUtilities.interruptIfNecessary(ex);
				}
				finally
				{
					if(d != null)
					{
						d.close();
					}
				}
			}
		}

		if(statusColors != null && statusColors.size() != DEFAULT_STATUS_COLOR_SCHEMES.size())
		{
			if(logger.isWarnEnabled()) logger.warn("Reverting Status-ColorSchemes to defaults.");
			statusColors = null;
		}

		if(statusColors == null)
		{
			statusColors = cloneStatusColors(DEFAULT_STATUS_COLOR_SCHEMES);
		}
	}

	private Map<HttpStatus.Type, ColorScheme> cloneStatusColors(Map<HttpStatus.Type, ColorScheme> input)
	{
		if(input != null && input.size() != DEFAULT_STATUS_COLOR_SCHEMES.size())
		{
			if(logger.isWarnEnabled()) logger.warn("Reverting Status-ColorSchemes to defaults.");
			input = null;
		}

		if(input == null)
		{
			input = DEFAULT_STATUS_COLOR_SCHEMES;
		}

		Map<HttpStatus.Type, ColorScheme> result = new HashMap<>();

		for(Map.Entry<HttpStatus.Type, ColorScheme> current : input.entrySet())
		{
			try
			{
				result.put(current.getKey(), current.getValue().clone());
			}
			catch(CloneNotSupportedException ex)
			{
				if(logger.isErrorEnabled()) logger.error("Exception while cloning Status-ColorScheme!!", ex);
			}
		}
		return result;
	}

	public void setStatusColors(Map<HttpStatus.Type, ColorScheme> colors)
	{
		Object oldValue = getStatusColors();
		colors = cloneStatusColors(colors);
		writeStatusColors(colors);
		this.statusColors = colors;
		Object newValue = getStatusColors();
		propertyChangeSupport.firePropertyChange(STATUS_COLORS_PROPERTY, oldValue, newValue);
		if(logger.isInfoEnabled()) logger.info("StatusColors set to {}.", this.statusColors);
	}

	private void writeStatusColors(Map<HttpStatus.Type, ColorScheme> colors)
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, STATUS_COLORS_XML_FILENAME);
		try
		{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			XMLEncoder e = new XMLEncoder(bos);
			PersistenceDelegate delegate = new EnumPersistenceDelegate();
			e.setPersistenceDelegate(HttpStatus.Type.class, delegate);
			e.writeObject(colors);
			e.close();
		}
		catch(Throwable ex)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while writing colors!", ex);
			IOUtilities.interruptIfNecessary(ex);
		}
	}

	public Map<HttpStatus.Type, ColorScheme> getStatusColors()
	{
		if(statusColors == null)
		{
			initStatusColors();
		}
		return cloneStatusColors(statusColors);
	}

	private void initSourceLists()
	{
		File appPath = getStartupApplicationPath();
		File sourceListsFile = new File(appPath, SOURCE_LISTS_XML_FILENAME);

		if(sourceListsFile.isFile())
		{
			long lastModified = sourceListsFile.lastModified();
			if(sourceLists != null && lastSourceListsModified >= lastModified)
			{
				if(logger.isDebugEnabled()) logger.debug("Won't reload source lists.");
				return;
			}
			XMLDecoder d = null;
			try
			{
				d = new XMLDecoder(new BufferedInputStream(new FileInputStream(sourceListsFile)));

				Map<String, Set> interimMap = transformToMap(String.class, Set.class, d.readObject());

				HashMap<String, Set<String>> resultMap = null;
				if(interimMap != null)
				{
					resultMap = new HashMap<>();
					for(Map.Entry<String, Set> current : interimMap.entrySet())
					{
						Set<String> value = transformToSet(String.class, current.getValue());
						if(value == null)
						{
							continue;
						}
						resultMap.put(current.getKey(), value);
					}
				}
				sourceLists = resultMap;
				lastSourceListsModified = lastModified;
			}
			catch(Throwable ex)
			{
				if(logger.isWarnEnabled()) logger.warn("Exception while loading source lists from sourceListsFile '" + sourceListsFile.getAbsolutePath() + "'!", ex);
				sourceLists = new HashMap<>();
				IOUtilities.interruptIfNecessary(ex);
			}
			finally
			{
				if(d != null)
				{
					d.close();
				}
			}
		}
		else if(sourceLists == null)
		{
			sourceLists = new HashMap<>();
		}
	}

	public Map<String, Set<String>> getSourceLists()
	{
		initSourceLists();
		return new HashMap<>(sourceLists);
	}

	public void setSourceLists(Map<String, Set<String>> sourceLists)
	{
		Object oldValue = getSourceLists();
		writeSourceLists(sourceLists);
		Object newValue = getSourceLists();
		blackList = null;
		whiteList = null;
		propertyChangeSupport.firePropertyChange(SOURCE_LISTS_PROPERTY, oldValue, newValue);
		if(sourceLists == null)
		{
			setSourceFiltering(LilithPreferences.SourceFiltering.NONE);
			setWhiteListName("");
			setBlackListName("");
		}
		else
		{
			String blackListName = getBlackListName();
			if(sourceLists.get(blackListName) == null)
			{
				setBlackListName("");
				if(getSourceFiltering() == LilithPreferences.SourceFiltering.BLACKLIST)
				{
					setSourceFiltering(LilithPreferences.SourceFiltering.NONE);
				}
			}

			String whiteListName = getWhiteListName();
			if(sourceLists.get(whiteListName) == null)
			{
				setWhiteListName("");
				if(getSourceFiltering() == LilithPreferences.SourceFiltering.WHITELIST)
				{
					setSourceFiltering(LilithPreferences.SourceFiltering.NONE);
				}
			}
		}
	}

	public void setSourceFiltering(LilithPreferences.SourceFiltering sourceFiltering)
	{
		Object oldValue = getSourceFiltering();
		PREFERENCES.put(SOURCE_FILTERING_PROPERTY, sourceFiltering.toString());
		this.sourceFiltering = sourceFiltering;
		propertyChangeSupport.firePropertyChange(SOURCE_FILTERING_PROPERTY, oldValue, sourceFiltering);
		if(logger.isInfoEnabled()) logger.info("SourceFiltering set to {}.", this.sourceFiltering);
	}

	public LilithPreferences.SourceFiltering getSourceFiltering()
	{
		if(sourceFiltering != null)
		{
			return sourceFiltering;
		}
		String sf = PREFERENCES.get(SOURCE_FILTERING_PROPERTY, "NONE");
		try
		{
			sourceFiltering = LilithPreferences.SourceFiltering.valueOf(sf);
		}
		catch(IllegalArgumentException e)
		{
			sourceFiltering = LilithPreferences.SourceFiltering.NONE;
		}
		return sourceFiltering;
	}

	public void initDetailsViewRoot(boolean overwriteAlways)
	{
		detailsViewRoot = new File(startupApplicationPath, DETAILS_VIEW_ROOT_FOLDER);
		if(detailsViewRoot.mkdirs())
		{
			if(logger.isInfoEnabled()) logger.info("Created directory {}.", detailsViewRoot.getAbsolutePath());
		}
		try
		{
			detailsViewRootUrl = detailsViewRoot.toURI().toURL();
		}
		catch(MalformedURLException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while creating detailsViewRootUrl for '{}'!", detailsViewRoot.getAbsolutePath());
			detailsViewRootUrl = null;
		}

		{
			String resourcePath = "/detailsView/" + DETAILS_VIEW_CSS_FILENAME;
			String historyBasePath = "/detailsView/history/detailsView.css/";
			File detailsViewCssFile = new File(detailsViewRoot, DETAILS_VIEW_CSS_FILENAME);

			initIfNecessary(detailsViewCssFile, resourcePath, historyBasePath, overwriteAlways);
		}

		{
			String resourcePath = "/detailsView/" + DETAILS_VIEW_GROOVY_FILENAME;
			String historyBasePath = "/detailsView/history/detailsView.groovy/";
			File detailsViewGroovyFile = new File(detailsViewRoot, DETAILS_VIEW_GROOVY_FILENAME);

			initIfNecessary(detailsViewGroovyFile, resourcePath, historyBasePath, overwriteAlways);
		}
	}

	private void initIfNecessary(File file, String resourcePath, String historyBasePath, boolean overwriteAlways)
	{
		boolean delete = false;
		if(overwriteAlways)
		{
			delete = true;
		}
		else if(file.isFile())
		{
			byte[] available = null;

			try
			{
				FileInputStream availableFile = new FileInputStream(file);
				available = getMD5(availableFile);
			}
			catch(FileNotFoundException e)
			{
				// ignore
			}

			byte[] current = getMD5(ApplicationPreferences.class.getResourceAsStream(resourcePath));
			if(Arrays.equals(available, current))
			{
				// we are done already. The current version is the latest version.
				if(logger.isDebugEnabled()) logger.debug("The current version of {} is also the latest version.", file.getAbsolutePath());
				return;
			}

			if(available != null)
			{
				// check older versions if available
				URL historyUrl = getClass().getResource(historyBasePath + "history.txt");
				if(historyUrl != null)
				{
					List<String> historyList = readLines(historyUrl);

					for(String currentLine : historyList)
					{
						InputStream is = getClass().getResourceAsStream(historyBasePath + currentLine + ".md5");
						if(is != null)
						{
							DataInputStream dis = new DataInputStream(is);
							byte[] checksum = new byte[16];
							try
							{
								dis.readFully(checksum);
								if(Arrays.equals(available, checksum))
								{
									if(logger.isInfoEnabled()) logger.info("Found old version of {}: {}", file.getAbsolutePath(), currentLine);
									delete = true;
									break;
								}
							}
							catch(IOException e)
							{
								if(logger.isWarnEnabled()) logger.warn("Exception while reading checksum of {}!", currentLine, e);
							}
							finally
							{
								try
								{
									dis.close();
								}
								catch(IOException e)
								{
									// ignore
								}
							}
						}
					}
				}
			}
			else
			{
				// we couldn't calculate the checksum. Try to delete it...
				delete = true;
			}
		}

		URL resourceUrl = ApplicationPreferences.class.getResource(resourcePath);
		if(resourceUrl == null)
		{
			if(logger.isErrorEnabled()) logger.error("Couldn't find resource {}!", resourcePath);
			return;
		}
		copy(resourceUrl, file, delete);
	}

	private void copy(URL source, File target, boolean overwrite)
	{
		if(overwrite)
		{
			if(target.isFile())
			{
				if(target.delete())
				{
					if(logger.isInfoEnabled()) logger.info("Deleted {}. ", target.getAbsolutePath());
				}
				else
				{
					if(logger.isWarnEnabled()) logger.warn("Tried to delete {} but couldn't!", target.getAbsolutePath());
				}
			}
		}

		if(!target.isFile())
		{
			InputStream is = null;
			FileOutputStream os = null;
			try
			{
				os = new FileOutputStream(target);
				is = source.openStream();
				IOUtils.copy(is, os);
				if(logger.isInfoEnabled()) logger.info("Initialized file at '{}' with data from '{}'.", target.getAbsolutePath(), source);
			}
			catch(IOException e)
			{
				if(logger.isWarnEnabled()) logger.warn("Exception while initializing '" + target.getAbsolutePath() + "' with data from '" + source + "'.!", e);
			}
			finally
			{
				IOUtilities.closeQuietly(is);
				IOUtilities.closeQuietly(os);
			}
		}
		else
		{
			if(logger.isInfoEnabled()) logger.info("Won't overwrite '{}'.", target.getAbsolutePath());
		}
	}

	/**
	 * Returns a list of strings containing all non-empty, non-comment lines found in the given URL.
	 * Commented lines start with a #.
	 *
	 * @param url the URL to read the lines from.
	 * @return a List of type String containing all non-empty, non-comment lines.
	 */
	private List<String> readLines(URL url)
	{
		List<String> result = new ArrayList<>();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			for(; ;)
			{
				String currentLine = reader.readLine();
				if(currentLine == null)
				{
					break;
				}
				currentLine = currentLine.trim();
				if(!"".equals(currentLine) && !currentLine.startsWith("#"))
				{
					result.add(currentLine);
				}
			}
		}
		catch(IOException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while reading lines from {}!", url, e);
		}
		finally
		{
			if(reader != null)
			{
				try
				{
					reader.close();
				}
				catch(IOException e)
				{
					// ignore
				}
			}
		}
		return result;
	}

	public File getDetailsViewRoot()
	{
		if(detailsViewRoot != null)
		{
			return detailsViewRoot;
		}
		initDetailsViewRoot(false);
		return detailsViewRoot;
	}

	public URL getDetailsViewRootUrl()
	{
		if(detailsViewRootUrl != null)
		{
			return detailsViewRootUrl;
		}
		initDetailsViewRoot(false);
		return detailsViewRootUrl;
	}

	public boolean isValidSource(String source)
	{
		if(source == null)
		{
			return false;
		}
		LilithPreferences.SourceFiltering filtering = getSourceFiltering();
		switch(filtering)
		{
			case BLACKLIST:
				return !isBlackListed(source);
			case WHITELIST:
				return isWhiteListed(source);
		}
		return true;
	}

	public boolean isBlackListed(String source)
	{
		if(blackList == null)
		{
			String listName = getBlackListName();
			initSourceLists();
			blackList = sourceLists.get(listName);
			if(blackList == null)
			{
				// meaning there was no list of the given name.
				if(logger.isInfoEnabled()) logger.info("Couldn't find source list '{}'!", listName);
				setSourceFiltering(LilithPreferences.SourceFiltering.NONE);
				setBlackListName("");
				return true;
			}
		}
		return blackList.contains(source);
	}

	public void setBlackListName(String name)
	{
		Object oldValue = getBlackListName();
		PREFERENCES.put(BLACK_LIST_NAME_PROPERTY, name);
		Object newValue = getBlackListName();
		propertyChangeSupport.firePropertyChange(BLACK_LIST_NAME_PROPERTY, oldValue, newValue);
		if(logger.isInfoEnabled()) logger.info("BlackListName set to {}.", newValue);
	}

	public String getBlackListName()
	{
		return PREFERENCES.get(BLACK_LIST_NAME_PROPERTY, "");
	}

	public boolean isWhiteListed(String source)
	{
		if(whiteList == null)
		{
			String listName = getWhiteListName();
			initSourceLists();
			whiteList = sourceLists.get(listName);
			if(whiteList == null)
			{
				// meaning there was no source list of the given name.
				if(logger.isInfoEnabled()) logger.info("Couldn't find source list '{}'!", listName);
				setSourceFiltering(LilithPreferences.SourceFiltering.NONE);
				setWhiteListName("");
				return true;
			}
		}
		return whiteList.contains(source);
	}

	public void setWhiteListName(String name)
	{
		Object oldValue = getWhiteListName();
		PREFERENCES.put(WHITE_LIST_NAME_PROPERTY, name);
		Object newValue = getWhiteListName();
		propertyChangeSupport.firePropertyChange(WHITE_LIST_NAME_PROPERTY, oldValue, newValue);
		if(logger.isInfoEnabled()) logger.info("WhiteListName set to {}.", newValue);
	}

	public String getWhiteListName()
	{
		return PREFERENCES.get(WHITE_LIST_NAME_PROPERTY, "");
	}

	public void setLookAndFeel(String name)
	{
		Object oldValue = getLookAndFeel();
		PREFERENCES.put(LOOK_AND_FEEL_PROPERTY, name);
		Object newValue = getLookAndFeel();
		propertyChangeSupport.firePropertyChange(LOOK_AND_FEEL_PROPERTY, oldValue, newValue);
		if(logger.isInfoEnabled()) logger.info("LookAndFeel set to {}.", newValue);
	}

	public String getLookAndFeel()
	{
		String result = PREFERENCES.get(LOOK_AND_FEEL_PROPERTY, STARTUP_LOOK_AND_FEEL);
		if(!installedLookAndFeels.contains(result))
		{
			result = STARTUP_LOOK_AND_FEEL;
			if(logger.isInfoEnabled()) logger.info("Look and Feel corrected to \"{}\".", result);
		}
		return result;
	}

	public void setCurrentTipOfTheDay(int currentTipOfTheDay)
	{
		Object oldValue = getCurrentTipOfTheDay();
		PREFERENCES.putInt(CURRENT_TIP_OF_THE_DAY_PROPERTY, currentTipOfTheDay);
		Object newValue = getCurrentTipOfTheDay();
		propertyChangeSupport.firePropertyChange(CURRENT_TIP_OF_THE_DAY_PROPERTY, oldValue, newValue);
	}

	public int getCurrentTipOfTheDay()
	{
		return PREFERENCES.getInt(CURRENT_TIP_OF_THE_DAY_PROPERTY, -1);
	}

	public void setShowingTipOfTheDay(boolean showingTipOfTheDay)
	{
		Object oldValue = isShowingTipOfTheDay();
		PREFERENCES.putBoolean(SHOWING_TIP_OF_THE_DAY_PROPERTY, showingTipOfTheDay);
		Object newValue = isShowingTipOfTheDay();
		propertyChangeSupport.firePropertyChange(SHOWING_TIP_OF_THE_DAY_PROPERTY, oldValue, newValue);
	}

	public boolean isShowingTipOfTheDay()
	{
		return PREFERENCES.getBoolean(SHOWING_TIP_OF_THE_DAY_PROPERTY, DEFAULT_VALUES.isShowingTipOfTheDay());
	}

	public void setMaximizingInternalFrames(boolean showingTipOfTheDay)
	{
		Object oldValue = isMaximizingInternalFrames();
		PREFERENCES.putBoolean(MAXIMIZING_INTERNAL_FRAMES_PROPERTY, showingTipOfTheDay);
		Object newValue = isMaximizingInternalFrames();
		propertyChangeSupport.firePropertyChange(MAXIMIZING_INTERNAL_FRAMES_PROPERTY, oldValue, newValue);
	}

	public boolean isMaximizingInternalFrames()
	{
		return PREFERENCES.getBoolean(MAXIMIZING_INTERNAL_FRAMES_PROPERTY, DEFAULT_VALUES.isMaximizingInternalFrames());
	}

	public void setGlobalLoggingEnabled(boolean globalLoggingEnabled)
	{
		Object oldValue = isGlobalLoggingEnabled();
		PREFERENCES.putBoolean(GLOBAL_LOGGING_ENABLED_PROPERTY, globalLoggingEnabled);
		Object newValue = isGlobalLoggingEnabled();
		propertyChangeSupport.firePropertyChange(GLOBAL_LOGGING_ENABLED_PROPERTY, oldValue, newValue);
	}

	public boolean isGlobalLoggingEnabled()
	{
		return PREFERENCES.getBoolean(GLOBAL_LOGGING_ENABLED_PROPERTY, DEFAULT_VALUES.isGlobalLoggingEnabled());
	}

	public void setLoggingStatisticEnabled(boolean enabled)
	{
		Object oldValue = isLoggingStatisticEnabled();
		PREFERENCES.putBoolean(LOGGING_STATISTIC_ENABLED_PROPERTY, enabled);
		Object newValue = isLoggingStatisticEnabled();
		propertyChangeSupport.firePropertyChange(LOGGING_STATISTIC_ENABLED_PROPERTY, oldValue, newValue);
	}

	public boolean isLoggingStatisticEnabled()
	{
		return PREFERENCES.getBoolean(LOGGING_STATISTIC_ENABLED_PROPERTY, DEFAULT_VALUES.isLoggingStatisticEnabled());
	}


	private void initConditions()
	{
		File appPath = getStartupApplicationPath();
		File conditionsFile = new File(appPath, CONDITIONS_XML_FILENAME);

		if(conditionsFile.isFile())
		{
			long lastModified = conditionsFile.lastModified();
			if(conditions != null && lastConditionsModified >= lastModified)
			{
				if(logger.isDebugEnabled()) logger.debug("Won't reload conditions.");
				return;
			}
			XMLDecoder d = null;
			try
			{
				d = new XMLDecoder(new BufferedInputStream(new FileInputStream(conditionsFile)));

				conditions = transformToList(SavedCondition.class, d.readObject());
				lastConditionsModified = lastModified;
				if(logger.isDebugEnabled()) logger.debug("Loaded conditions {}.", conditions);
			}
			catch(Throwable ex)
			{
				if(logger.isWarnEnabled()) logger.warn("Exception while loading conditions from file '" + conditionsFile.getAbsolutePath() + "'!", ex);
				IOUtilities.interruptIfNecessary(ex);
			}
			finally
			{
				if(d != null)
				{
					d.close();
				}
			}
		}

		if(conditions == null)
		{
			conditions = new ArrayList<>();
		}
	}

	public SavedCondition resolveSavedCondition(Condition condition)
	{
		if(condition == null)
		{
			return null;
		}
		initConditions();
		for(SavedCondition current : conditions)
		{
			if(condition.equals(current.getCondition()))
			{
				try
				{
					return current.clone();
				}
				catch(CloneNotSupportedException e)
				{
					return null;
				}
			}
		}
		return null;
	}

	public SavedCondition resolveSavedCondition(String conditionName)
	{
		if(conditionName == null)
		{
			return null;
		}
		initConditions();
		for(SavedCondition current : conditions)
		{
			if(conditionName.equals(current.getName()))
			{
				try
				{
					return current.clone();
				}
				catch(CloneNotSupportedException e)
				{
					return null;
				}
			}
		}
		return null;
	}


	public List<SavedCondition> getConditions()
	{
		initConditions();

		// perform deep clone... otherwise no propchange would be fired.
		ArrayList<SavedCondition> result = new ArrayList<>(conditions.size());
		for(SavedCondition current : conditions)
		{
			try
			{
				result.add(current.clone());
			}
			catch(CloneNotSupportedException e)
			{
				// ignore
			}
		}

		return result;
	}

	public List<String> getConditionNames()
	{
		initConditions();

		// perform deep clone... otherwise no propchange would be fired.
		ArrayList<String> result = new ArrayList<>(conditions.size());
		result.addAll(conditions.stream()
				.map(SavedCondition::getName)
				.collect(Collectors.toList()));

		return result;
	}

	public void setConditions(List<SavedCondition> conditions)
	{
		Object oldValue = getConditions();
		writeConditions(conditions);
		Object newValue = getConditions();
		propertyChangeSupport.firePropertyChange(CONDITIONS_PROPERTY, oldValue, newValue);
	}

	public void setAutoOpening(boolean autoOpening)
	{
		Object oldValue = isAutoOpening();
		PREFERENCES.putBoolean(AUTO_OPENING_PROPERTY, autoOpening);
		Object newValue = isAutoOpening();
		propertyChangeSupport.firePropertyChange(AUTO_OPENING_PROPERTY, oldValue, newValue);
	}

	public boolean isAutoOpening()
	{
		return PREFERENCES.getBoolean(AUTO_OPENING_PROPERTY, DEFAULT_VALUES.isAutoOpening());
	}

	public void setTrayActive(boolean trayActive)
	{
		Object oldValue = isTrayActive();
		PREFERENCES.putBoolean(TRAY_ACTIVE_PROPERTY, trayActive);
		Object newValue = isTrayActive();
		propertyChangeSupport.firePropertyChange(TRAY_ACTIVE_PROPERTY, oldValue, newValue);
	}

	public boolean isTrayActive()
	{
		return PREFERENCES.getBoolean(TRAY_ACTIVE_PROPERTY, DEFAULT_VALUES.isTrayActive());
	}

	public void setHidingOnClose(boolean trayActive)
	{
		Object oldValue = isHidingOnClose();
		PREFERENCES.putBoolean(HIDING_ON_CLOSE_PROPERTY, trayActive);
		Object newValue = isHidingOnClose();
		propertyChangeSupport.firePropertyChange(HIDING_ON_CLOSE_PROPERTY, oldValue, newValue);
	}

	public boolean isHidingOnClose()
	{
		return PREFERENCES.getBoolean(HIDING_ON_CLOSE_PROPERTY, DEFAULT_VALUES.isHidingOnClose());
	}

	public void setShowingToolbar(boolean showingToolbar)
	{
		Object oldValue = isShowingToolbar();
		PREFERENCES.putBoolean(SHOWING_TOOLBAR_PROPERTY, showingToolbar);
		Object newValue = isShowingToolbar();
		propertyChangeSupport.firePropertyChange(SHOWING_TOOLBAR_PROPERTY, oldValue, newValue);
	}

	public boolean isShowingToolbar()
	{
		return PREFERENCES.getBoolean(SHOWING_TOOLBAR_PROPERTY, DEFAULT_VALUES.isShowingToolbar());
	}

	public boolean isShowingStatusBar()
	{
		return PREFERENCES.getBoolean(SHOWING_STATUSBAR_PROPERTY, DEFAULT_VALUES.isShowingStatusbar());
	}

	public void setShowingStatusBar(boolean showingStatusBar)
	{
		Object oldValue = isShowingStatusBar();
		PREFERENCES.putBoolean(SHOWING_STATUSBAR_PROPERTY, showingStatusBar);
		Object newValue = isShowingStatusBar();
		propertyChangeSupport.firePropertyChange(SHOWING_STATUSBAR_PROPERTY, oldValue, newValue);
	}


	public void setShowingPrimaryIdentifier(boolean showingPrimaryIdentifier)
	{
		Object oldValue = isShowingPrimaryIdentifier();
		PREFERENCES.putBoolean(SHOWING_PRIMARY_IDENTIFIER_PROPERTY, showingPrimaryIdentifier);
		Object newValue = isShowingPrimaryIdentifier();
		propertyChangeSupport.firePropertyChange(SHOWING_PRIMARY_IDENTIFIER_PROPERTY, oldValue, newValue);
	}

	public boolean isShowingPrimaryIdentifier()
	{
		return PREFERENCES.getBoolean(SHOWING_PRIMARY_IDENTIFIER_PROPERTY, DEFAULT_VALUES.isShowingPrimaryIdentifier());
	}

	public void setShowingSecondaryIdentifier(boolean showingSecondaryIdentifier)
	{
		Object oldValue = isShowingSecondaryIdentifier();
		PREFERENCES.putBoolean(SHOWING_SECONDARY_IDENTIFIER_PROPERTY, showingSecondaryIdentifier);
		Object newValue = isShowingSecondaryIdentifier();
		propertyChangeSupport.firePropertyChange(SHOWING_SECONDARY_IDENTIFIER_PROPERTY, oldValue, newValue);
	}

	public boolean isShowingSecondaryIdentifier()
	{
		return PREFERENCES.getBoolean(SHOWING_SECONDARY_IDENTIFIER_PROPERTY, DEFAULT_VALUES.isShowingPrimaryIdentifier());
	}

	public void setSplashScreenDisabled(boolean splashScreenDisabled)
	{
		Object oldValue = isSplashScreenDisabled();
		PREFERENCES.putBoolean(SPLASH_SCREEN_DISABLED_PROPERTY, splashScreenDisabled);
		Object newValue = isSplashScreenDisabled();
		propertyChangeSupport.firePropertyChange(SPLASH_SCREEN_DISABLED_PROPERTY, oldValue, newValue);
	}

	public boolean isSplashScreenDisabled()
	{
		return PREFERENCES.getBoolean(SPLASH_SCREEN_DISABLED_PROPERTY, DEFAULT_VALUES.isSplashScreenDisabled());
	}

	public void setAskingBeforeQuit(boolean askingBeforeQuit)
	{
		Object oldValue = isAskingBeforeQuit();
		PREFERENCES.putBoolean(ASKING_BEFORE_QUIT_PROPERTY, askingBeforeQuit);
		Object newValue = isAskingBeforeQuit();
		propertyChangeSupport.firePropertyChange(ASKING_BEFORE_QUIT_PROPERTY, oldValue, newValue);
	}

	public boolean isAskingBeforeQuit()
	{
		return PREFERENCES.getBoolean(ASKING_BEFORE_QUIT_PROPERTY, DEFAULT_VALUES.isAskingBeforeQuit());
	}

	public void setShowingFullCallstack(boolean showingFullCallstack)
	{
		Object oldValue = isShowingFullCallstack();
		PREFERENCES.putBoolean(SHOWING_FULL_CALLSTACK_PROPERTY, showingFullCallstack);
		Object newValue = isShowingFullCallstack();
		propertyChangeSupport.firePropertyChange(SHOWING_FULL_CALLSTACK_PROPERTY, oldValue, newValue);
	}

	public boolean isShowingFullCallstack()
	{
		return PREFERENCES.getBoolean(SHOWING_FULL_CALLSTACK_PROPERTY, DEFAULT_VALUES.isShowingFullCallstack());
	}

	public void setUsingWrappedExceptionStyle(boolean showingFullCallstack)
	{
		Object oldValue = isUsingWrappedExceptionStyle();
		PREFERENCES.putBoolean(USING_WRAPPED_EXCEPTION_STYLE_PROPERTY, showingFullCallstack);
		Object newValue = isUsingWrappedExceptionStyle();
		propertyChangeSupport.firePropertyChange(USING_WRAPPED_EXCEPTION_STYLE_PROPERTY, oldValue, newValue);
	}

	public boolean isUsingWrappedExceptionStyle()
	{
		return PREFERENCES.getBoolean(USING_WRAPPED_EXCEPTION_STYLE_PROPERTY, DEFAULT_VALUES.isUsingWrappedExceptionStyle());
	}

	public void setShowingStackTrace(boolean showingStackTrace)
	{
		Object oldValue = isShowingStackTrace();
		PREFERENCES.putBoolean(SHOWING_STACKTRACE_PROPERTY, showingStackTrace);
		Object newValue = isShowingStackTrace();
		propertyChangeSupport.firePropertyChange(SHOWING_STACKTRACE_PROPERTY, oldValue, newValue);
	}

	public boolean isShowingStackTrace()
	{
		return PREFERENCES.getBoolean(SHOWING_STACKTRACE_PROPERTY, DEFAULT_VALUES.isShowingStackTrace());
	}

	public void setCleaningLogsOnExit(boolean cleaningLogsOnExit)
	{
		Object oldValue = isCleaningLogsOnExit();
		PREFERENCES.putBoolean(CLEANING_LOGS_ON_EXIT_PROPERTY, cleaningLogsOnExit);
		Object newValue = isCleaningLogsOnExit();
		propertyChangeSupport.firePropertyChange(CLEANING_LOGS_ON_EXIT_PROPERTY, oldValue, newValue);
	}

	public boolean isCleaningLogsOnExit()
	{
		return PREFERENCES.getBoolean(CLEANING_LOGS_ON_EXIT_PROPERTY, DEFAULT_VALUES.isCleaningLogsOnExit());
	}

	public void setColoringWholeRow(boolean coloringWholeRow)
	{
		Object oldValue = isColoringWholeRow();
		PREFERENCES.putBoolean(COLORING_WHOLE_ROW_PROPERTY, coloringWholeRow);
		Object newValue = isColoringWholeRow();
		propertyChangeSupport.firePropertyChange(COLORING_WHOLE_ROW_PROPERTY, oldValue, newValue);
	}

	public boolean isColoringWholeRow()
	{
		return PREFERENCES.getBoolean(COLORING_WHOLE_ROW_PROPERTY, DEFAULT_VALUES.isColoringWholeRow());
	}

	public void setCheckingForUpdate(boolean checkingForUpdate)
	{
		Object oldValue = isCheckingForUpdate();
		PREFERENCES.putBoolean(CHECKING_FOR_UPDATE_PROPERTY, checkingForUpdate);
		Object newValue = isCheckingForUpdate();
		propertyChangeSupport.firePropertyChange(CHECKING_FOR_UPDATE_PROPERTY, oldValue, newValue);
	}

	public boolean isCheckingForUpdate()
	{
		return PREFERENCES.getBoolean(CHECKING_FOR_UPDATE_PROPERTY, DEFAULT_VALUES.isCheckingForUpdate());
	}

	public void setCheckingForSnapshot(boolean checkingForSnapshot)
	{
		Object oldValue = isCheckingForSnapshot();
		PREFERENCES.putBoolean(CHECKING_FOR_SNAPSHOT_PROPERTY, checkingForSnapshot);
		Object newValue = isCheckingForSnapshot();
		propertyChangeSupport.firePropertyChange(CHECKING_FOR_SNAPSHOT_PROPERTY, oldValue, newValue);
	}

	public boolean isCheckingForSnapshot()
	{
		return PREFERENCES.getBoolean(CHECKING_FOR_SNAPSHOT_PROPERTY, DEFAULT_VALUES.isCheckingForSnapshot());
	}

	public void setAutoClosing(boolean autoClosing)
	{
		Object oldValue = isAutoClosing();
		PREFERENCES.putBoolean(AUTO_CLOSING_PROPERTY, autoClosing);
		Object newValue = isAutoClosing();
		propertyChangeSupport.firePropertyChange(AUTO_CLOSING_PROPERTY, oldValue, newValue);
	}

	public boolean isAutoClosing()
	{
		return PREFERENCES.getBoolean(AUTO_CLOSING_PROPERTY, DEFAULT_VALUES.isAutoClosing());
	}

	public File getImagePath()
	{
		String imagePath = PREFERENCES.get(IMAGE_PATH_PROPERTY, USER_HOME);
		File result = new File(imagePath);
		if(!result.isDirectory())
		{
			result = new File(USER_HOME);
		}
		return result;
	}

	public void setImagePath(File imagePath)
	{
		if(!imagePath.isDirectory())
		{
			throw new IllegalArgumentException("'" + imagePath.getAbsolutePath() + "' is not a directory!");
		}
		Object oldValue = getImagePath();
		PREFERENCES.put(IMAGE_PATH_PROPERTY, imagePath.getAbsolutePath());
		Object newValue = getImagePath();
		propertyChangeSupport.firePropertyChange(IMAGE_PATH_PROPERTY, oldValue, newValue);
	}

	public File getPreviousOpenPath()
	{
		String imagePath = PREFERENCES.get(PREVIOUS_OPEN_PATH_PROPERTY, USER_HOME);
		File result = new File(imagePath);
		if(!result.isDirectory())
		{
			result = new File(USER_HOME);
		}
		return result;
	}

	public void setPreviousOpenPath(File openPath)
	{
		if(!openPath.isDirectory())
		{
			throw new IllegalArgumentException("'" + openPath.getAbsolutePath() + "' is not a directory!");
		}
		Object oldValue = getPreviousOpenPath();
		PREFERENCES.put(PREVIOUS_OPEN_PATH_PROPERTY, openPath.getAbsolutePath());
		Object newValue = getPreviousOpenPath();
		propertyChangeSupport.firePropertyChange(PREVIOUS_OPEN_PATH_PROPERTY, oldValue, newValue);
	}

	public File getPreviousImportPath()
	{
		String path = PREFERENCES.get(PREVIOUS_IMPORT_PATH_PROPERTY, USER_HOME);
		File result = new File(path);
		if(!result.isDirectory())
		{
			result = new File(USER_HOME);
		}
		return result;
	}

	public void setPreviousImportPath(File importPath)
	{
		if(!importPath.isDirectory())
		{
			throw new IllegalArgumentException("'" + importPath.getAbsolutePath() + "' is not a directory!");
		}
		Object oldValue = getPreviousImportPath();
		PREFERENCES.put(PREVIOUS_IMPORT_PATH_PROPERTY, importPath.getAbsolutePath());
		Object newValue = getPreviousImportPath();
		propertyChangeSupport.firePropertyChange(PREVIOUS_IMPORT_PATH_PROPERTY, oldValue, newValue);
	}

	public File getPreviousExportPath()
	{
		String path = PREFERENCES.get(PREVIOUS_EXPORT_PATH_PROPERTY, USER_HOME);
		File result = new File(path);
		if(!result.isDirectory())
		{
			result = new File(USER_HOME);
		}
		return result;
	}

	public void setPreviousExportPath(File exportPath)
	{
		if(!exportPath.isDirectory())
		{
			throw new IllegalArgumentException("'" + exportPath.getAbsolutePath() + "' is not a directory!");
		}
		Object oldValue = getPreviousImportPath();
		PREFERENCES.put(PREVIOUS_EXPORT_PATH_PROPERTY, exportPath.getAbsolutePath());
		Object newValue = getPreviousExportPath();
		propertyChangeSupport.firePropertyChange(PREVIOUS_EXPORT_PATH_PROPERTY, oldValue, newValue);
	}

	public File getSoundPath()
	{
		String soundPath = PREFERENCES.get(SOUND_PATH_PROPERTY, USER_HOME);
		File result = new File(soundPath);
		if(!result.isDirectory())
		{
			result = new File(USER_HOME);
		}
		return result;
	}

	public void setSoundPath(File soundPath)
	{
		if(!soundPath.isDirectory())
		{
			throw new IllegalArgumentException("'" + soundPath.getAbsolutePath() + "' is not a directory!");
		}
		Object oldValue = getSoundPath();
		PREFERENCES.put(SOUND_PATH_PROPERTY, soundPath.getAbsolutePath());
		Object newValue = getSoundPath();
		propertyChangeSupport.firePropertyChange(SOUND_PATH_PROPERTY, oldValue, newValue);
	}

	public void setScaleFactor(double scale)
	{
		Object oldValue = getScaleFactor();
		PREFERENCES.putDouble(SCALE_FACTOR_PROPERTY, scale);
		Object newValue = getScaleFactor();
		propertyChangeSupport.firePropertyChange(SCALE_FACTOR_PROPERTY, oldValue, newValue);
	}

	public double getScaleFactor()
	{
		return PREFERENCES.getDouble(SCALE_FACTOR_PROPERTY, 1.0d);
	}

	public void setMute(boolean mute)
	{
		Object oldValue = isMute();
		PREFERENCES.putBoolean(MUTE_PROPERTY, mute);
		Object newValue = isMute();
		propertyChangeSupport.firePropertyChange(MUTE_PROPERTY, oldValue, newValue);
	}

	public boolean isMute()
	{
		return PREFERENCES.getBoolean(MUTE_PROPERTY, DEFAULT_VALUES.isMute());
	}

	public void setLicensed(boolean licensed)
	{
		Object oldValue = isLicensed();
		if(licensed)
		{
			PREFERENCES.put(LICENSED_PREFERENCES_KEY, Lilith.APP_VERSION);
		}
		else
		{
			PREFERENCES.remove(LICENSED_PREFERENCES_KEY);
		}
		Object newValue = isLicensed();
		propertyChangeSupport.firePropertyChange(LICENSED_PREFERENCES_KEY, oldValue, newValue);
	}

	public boolean isLicensed()
	{
		return Lilith.APP_VERSION.equals(PREFERENCES.get(LICENSED_PREFERENCES_KEY, null));
	}

	public void setApplicationPath(File applicationPath)
	{
		if(applicationPath.mkdirs())
		{
			if(logger.isInfoEnabled()) logger.info("Created directory {}.", applicationPath.getAbsolutePath());
		}
		if(!applicationPath.isDirectory())
		{
			throw new IllegalArgumentException("'" + applicationPath.getAbsolutePath() + "' is not a directory!");
		}
		Object oldValue = getStartupApplicationPath(); // !!!
		PREFERENCES.put(APPLICATION_PATH_PROPERTY, applicationPath.getAbsolutePath());
		Object newValue = getApplicationPath();
		propertyChangeSupport.firePropertyChange(APPLICATION_PATH_PROPERTY, oldValue, newValue);
	}

	public File getApplicationPath()
	{
		String appPath = PREFERENCES.get(APPLICATION_PATH_PROPERTY, DEFAULT_APPLICATION_PATH);
		File result = new File(appPath);
		if(result.mkdirs())
		{
			if(logger.isInfoEnabled()) logger.info("Created directory {}.", result.getAbsolutePath());
		}
		return result;
	}

	public void setDefaultConditionName(String conditionName)
	{
		Object oldValue = getDefaultConditionName();
		PREFERENCES.put(DEFAULT_CONDITION_NAME_PROPERTY, conditionName);
		Object newValue = getDefaultConditionName();
		propertyChangeSupport.firePropertyChange(DEFAULT_CONDITION_NAME_PROPERTY, oldValue, newValue);
	}

	public String getDefaultConditionName()
	{
		return PREFERENCES.get(DEFAULT_CONDITION_NAME_PROPERTY, EventContainsCondition.DESCRIPTION);
	}

	/**
	 * The StartupApplicationPath is initialized on application startup via ApplicationPreferences.getApplicationPath.
	 * If a part of the application needs the application path it should *always* use this method instead of
	 * getApplicationPath() since the application path might change while this one will always stay
	 * the same.
	 *
	 * A switch of the application path while the application is running isn't safe so it's changed for real
	 * upon next restart.
	 *
	 * @return the application path at startup time.
	 */
	public File getStartupApplicationPath()
	{
		return startupApplicationPath;
	}

	public void setUsingInternalFrames(boolean usingInternalFrames)
	{
		Object oldValue = isUsingInternalFrames();
		PREFERENCES.putBoolean(USING_INTERNAL_FRAMES_PROPERTY, usingInternalFrames);
		Object newValue = isUsingInternalFrames();
		propertyChangeSupport.firePropertyChange(USING_INTERNAL_FRAMES_PROPERTY, oldValue, newValue);
	}

	public boolean isUsingInternalFrames()
	{
		return PREFERENCES.getBoolean(USING_INTERNAL_FRAMES_PROPERTY, DEFAULT_VALUES.isUsingInternalFrames());
	}

	public void setAutoFocusingWindow(boolean autoFocusingWindow)
	{
		Object oldValue = isAutoFocusingWindow();
		PREFERENCES.putBoolean(AUTO_FOCUSING_WINDOW_PROPERTY, autoFocusingWindow);
		Object newValue = isAutoFocusingWindow();
		propertyChangeSupport.firePropertyChange(AUTO_FOCUSING_WINDOW_PROPERTY, oldValue, newValue);
	}

	public boolean isAutoFocusingWindow()
	{
		return PREFERENCES.getBoolean(AUTO_FOCUSING_WINDOW_PROPERTY, DEFAULT_VALUES.isAutoFocusingWindow());
	}

	/**
	 * Whether or not the full path of recent files should be visible in the "Recent Files" menu.
	 * I'm not going to add this to the preferences UI until someone screams for this option.
	 * Default is false.
	 *
	 * @param showingFullRecentPath show full path for recent files
	 */
	public void setShowingFullRecentPath(boolean showingFullRecentPath)
	{
		Object oldValue = isShowingFullRecentPath();
		PREFERENCES.putBoolean(SHOWING_FULL_RECENT_PATH_PROPERTY, showingFullRecentPath);
		Object newValue = isShowingFullRecentPath();
		propertyChangeSupport.firePropertyChange(SHOWING_FULL_RECENT_PATH_PROPERTY, oldValue, newValue);
	}

	/**
	 * Whether or not the full path of recent files should be visible in the "Recent Files" menu.
	 * I'm not going to add this to the preferences UI until someone screams for this option.
	 * Default is false.
	 *
	 * @return show full path for recent files
	 */
	public boolean isShowingFullRecentPath()
	{
		return PREFERENCES.getBoolean(SHOWING_FULL_RECENT_PATH_PROPERTY, DEFAULT_VALUES.isShowingFullRecentPath());
	}

	public void setSourceNames(Map<String, String> sourceNames)
	{
		Object oldValue = getSourceNames();
		writeSourceNames(sourceNames);
		Object newValue = getSourceNames();
		propertyChangeSupport.firePropertyChange(SOURCE_NAMES_PROPERTY, oldValue, newValue);
	}

	public Map<String, String> getSourceNames()
	{
		File appPath = getStartupApplicationPath();
		File sourceNamesFile = new File(appPath, SOURCE_NAMES_XML_FILENAME);

		if(sourceNamesFile.isFile())
		{
			if(loadSourceNamesXml(sourceNamesFile))
			{
				return new HashMap<>(sourceNames);
			}
		}

		sourceNamesFile = new File(appPath, SOURCE_NAMES_PROPERTIES_FILENAME);
		if(sourceNamesFile.isFile())
		{
			if(loadSourceNamesProperties(sourceNamesFile))
			{
				return new HashMap<>(sourceNames);
			}
		}
		return new HashMap<>(DEFAULT_SOURCE_NAMES);
	}


	public Map<String, String> getSoundLocations()
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, SOUND_LOCATIONS_XML_FILENAME);

		if(file.isFile())
		{
			if(loadSoundLocationsXml(file))
			{
				return new HashMap<>(soundLocations);
			}
		}

		return new HashMap<>(DEFAULT_SOUND_LOCATIONS);
	}

	public void setSoundLocations(Map<String, String> soundLocations)
	{
		Object oldValue = getSoundLocations();
		writeSoundLocations(soundLocations);
		Object newValue = getSoundLocations();
		propertyChangeSupport.firePropertyChange(SOUND_LOCATIONS_PROPERTY, oldValue, newValue);
	}

	public void resetSoundLocations()
	{
		if(logger.isInfoEnabled()) logger.info("Initializing preferences with default sound locations.");
		setSoundLocations(DEFAULT_SOUND_LOCATIONS);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void reset()
	{
		final Logger logger = LoggerFactory.getLogger(ApplicationPreferences.class);
		boolean licensed = isLicensed();
		try
		{
			PREFERENCES.clear();
			resetSoundLocations();
			setLicensed(licensed);
			setApplicationPath(new File(DEFAULT_APPLICATION_PATH));
		}
		catch(BackingStoreException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while clearing preferences!");
		}
	}


	public void setScrollingToBottom(boolean scrollingToBottom)
	{
		Object oldValue = isScrollingToBottom();
		PREFERENCES.putBoolean(SCROLLING_TO_BOTTOM_PROPERTY, scrollingToBottom);
		Object newValue = isScrollingToBottom();
		propertyChangeSupport.firePropertyChange(SCROLLING_TO_BOTTOM_PROPERTY, oldValue, newValue);
	}

	public boolean isScrollingToBottom()
	{
		return PREFERENCES.getBoolean(SCROLLING_TO_BOTTOM_PROPERTY, DEFAULT_VALUES.isScrollingToBottom());
	}

	private boolean loadSoundLocationsXml(File file)
	{
		long lastModified = file.lastModified();
		if(soundLocations != null && lastSoundLocationsModified >= lastModified)
		{
			if(logger.isDebugEnabled()) logger.debug("Won't reload sound locations.");
			return true;
		}
		Map<String, String> props = loadPropertiesXml(file);
		if(props != null)
		{
			lastSoundLocationsModified = lastModified;
			// correct values, i.e. add missing keys
			DEFAULT_SOUND_LOCATIONS.entrySet().stream()
					.filter(current -> !props.containsKey(current.getKey()))
					.forEach(current -> props.put(current.getKey(), ""));

			soundLocations = props;
			return true;
		}
		return false;
	}

	private boolean writeSoundLocations(Map<String, String> sourceNames)
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, SOUND_LOCATIONS_XML_FILENAME);
		return writePropertiesXml(file, sourceNames, "Sound locations");
	}

	private boolean loadSourceNamesXml(File file)
	{
		long lastModified = file.lastModified();
		if(sourceNames != null && lastSourceNamesModified >= lastModified)
		{
			if(logger.isDebugEnabled()) logger.debug("Won't reload source names.");
			return true;
		}
		Map<String, String> props = loadPropertiesXml(file);
		if(props != null)
		{
			lastSourceNamesModified = lastModified;
			sourceNames = props;
			return true;
		}
		return false;
	}

	private boolean loadSourceNamesProperties(File sourceNamesFile)
	{
		long lastModified = sourceNamesFile.lastModified();
		if(sourceNames != null && lastSourceNamesModified >= lastModified)
		{
			if(logger.isDebugEnabled()) logger.debug("Won't reload source names.");
			return true;
		}

		Map<String, String> props = loadProperties(sourceNamesFile);
		if(props != null)
		{
			lastSourceNamesModified = lastModified;
			sourceNames = props;
			return true;
		}
		return false;
	}

	private boolean writeSourceNames(Map<String, String> sourceNames)
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, SOURCE_NAMES_XML_FILENAME);
		return writePropertiesXml(file, sourceNames, "Source names");
	}

	private boolean writeSourceLists(Map<String, Set<String>> sourceLists)
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, SOURCE_LISTS_XML_FILENAME);
		XMLEncoder e = null;
		Throwable error = null;
		try
		{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			e = new XMLEncoder(bos);
			e.writeObject(sourceLists);
		}
		catch(FileNotFoundException ex)
		{
			error = ex;
		}
		finally
		{
			if(e != null)
			{
				e.close();
			}
		}
		if(error != null)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while writing source lists!", error);
			return false;
		}
		return true;
	}

	private boolean writeConditions(List<SavedCondition> conditions)
	{
		File appPath = getStartupApplicationPath();
		File file = new File(appPath, CONDITIONS_XML_FILENAME);
		XMLEncoder e = null;
		Throwable error = null;
		try
		{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			e = new XMLEncoder(bos);
			e.writeObject(conditions);
			if(logger.isInfoEnabled()) logger.info("Wrote conditions {}.", conditions);
		}
		catch(FileNotFoundException ex)
		{
			error = ex;
		}
		finally
		{
			if(e != null)
			{
				e.close();
			}
		}
		if(error != null)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while writing source lists!", error);
			return false;
		}
		return true;
	}

	/**
	 * @param file the properties xml file
	 * @return the resulting map
	 * @noinspection MismatchedQueryAndUpdateOfCollection
	 */
	private Map<String, String> loadPropertiesXml(File file)
	{
		InputStream is = null;
		try
		{
			is = new BufferedInputStream(new FileInputStream(file));
			Properties props = new Properties();
			props.loadFromXML(is);
			Map<String, String> result = new HashMap<>();
			for(Object keyObj : props.keySet())
			{
				String key = (String) keyObj;
				String value = (String) props.get(key);
				if(value != null)
				{
					result.put(key, value);
				}
			}
			return result;
		}
		catch(IOException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Couldn't load properties from '" + file.getAbsolutePath() + "'!", e);
		}
		finally
		{
			IOUtilities.closeQuietly(is);
		}
		return null;
	}

	/**
	 * @param file            the properties xml file
	 * @param stringStringMap the map to be written
	 * @param comment         the comment
	 * @return whether or not the file could be written
	 * @noinspection MismatchedQueryAndUpdateOfCollection
	 */
	private boolean writePropertiesXml(File file, Map<String, String> stringStringMap, String comment)
	{
		Properties output = new Properties();
		for(Map.Entry<String, String> entry : stringStringMap.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(value != null)
			{
				output.put(key, value);
			}
		}
		OutputStream os = null;
		Throwable error = null;
		try
		{
			os = new BufferedOutputStream(new FileOutputStream(file));
			output.storeToXML(os, comment, StandardCharsets.UTF_8.toString());
		}
		catch(IOException e)
		{
			error = e;
		}
		finally
		{
			IOUtilities.closeQuietly(os);
		}
		if(error != null)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while writing source names!", error);
			return false;
		}
		return true;
	}


	private Map<String, String> loadProperties(File file)
	{
		InputStream is = null;
		try
		{
			is = new BufferedInputStream(new FileInputStream(file));
			Properties props = new Properties();
			props.load(is);
			Map<String, String> result = new HashMap<>();
			for(Object keyObj : props.keySet())
			{
				String key = (String) keyObj;
				String value = (String) props.get(key);
				if(value != null)
				{
					result.put(key, value);
				}
			}
			return result;
		}
		catch(IOException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Couldn't load properties from '" + file.getAbsolutePath() + "'!", e);
		}
		finally
		{
			IOUtilities.closeQuietly(is);
		}
		return null;
	}

	public void writeLoggingColumnLayout(boolean global, List<PersistentTableColumnModel.TableColumnLayoutInfo> layoutInfos)
	{
		File appPath = getStartupApplicationPath();
		File file;
		if(global)
		{
			file = new File(appPath, LOGGING_LAYOUT_GLOBAL_XML_FILENAME);
		}
		else
		{
			file = new File(appPath, LOGGING_LAYOUT_XML_FILENAME);
		}
		writeColumnLayout(file, layoutInfos);
	}

	public void writeAccessColumnLayout(boolean global, List<PersistentTableColumnModel.TableColumnLayoutInfo> layoutInfos)
	{
		File appPath = getStartupApplicationPath();
		File file;
		if(global)
		{
			file = new File(appPath, ACCESS_LAYOUT_GLOBAL_XML_FILENAME);
		}
		else
		{
			file = new File(appPath, ACCESS_LAYOUT_XML_FILENAME);
		}
		writeColumnLayout(file, layoutInfos);
	}

	public List<PersistentTableColumnModel.TableColumnLayoutInfo> readLoggingColumnLayout(boolean global)
	{
		File appPath = getStartupApplicationPath();
		File file;
		if(global)
		{
			file = new File(appPath, LOGGING_LAYOUT_GLOBAL_XML_FILENAME);
		}
		else
		{
			file = new File(appPath, LOGGING_LAYOUT_XML_FILENAME);
		}
		return readColumnLayout(file);
	}

	public List<PersistentTableColumnModel.TableColumnLayoutInfo> readAccessColumnLayout(boolean global)
	{
		File appPath = getStartupApplicationPath();
		File file;
		if(global)
		{
			file = new File(appPath, ACCESS_LAYOUT_GLOBAL_XML_FILENAME);
		}
		else
		{
			file = new File(appPath, ACCESS_LAYOUT_XML_FILENAME);
		}
		return readColumnLayout(file);
	}

	private boolean writeColumnLayout(File file, List<PersistentTableColumnModel.TableColumnLayoutInfo> layoutInfos)
	{
		XMLEncoder e = null;
		Throwable error = null;
		try
		{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			e = new XMLEncoder(bos);
			e.writeObject(layoutInfos);
			if(logger.isInfoEnabled()) logger.info("Wrote layouts {} to file '{}'.", layoutInfos, file.getAbsolutePath());
		}
		catch(FileNotFoundException ex)
		{
			error = ex;
		}
		finally
		{
			if(e != null)
			{
				e.close();
			}
		}
		if(error != null)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while writing layouts to file '" + file.getAbsolutePath() + "'!", error);
			return false;
		}
		return true;
	}

	private List<PersistentTableColumnModel.TableColumnLayoutInfo> readColumnLayout(File file)
	{
		XMLDecoder d = null;
		List<PersistentTableColumnModel.TableColumnLayoutInfo> result;
		try
		{
			d = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));

			result = transformToList(PersistentTableColumnModel.TableColumnLayoutInfo.class, d.readObject());
		}
		catch(Throwable ex)
		{
			if(logger.isInfoEnabled()) logger.info("Exception while loading layouts from file '{}'':", file.getAbsolutePath(), ex.getMessage());
			result = null;
			IOUtilities.interruptIfNecessary(ex);
		}
		finally
		{
			if(d != null)
			{
				d.close();
			}
		}
		return result;
	}

	/**
	 * Quick &amp; dirty MD5 checksum function.
	 * Returns null in case of error.
	 *
	 * @param input the input
	 * @return the checksum
	 */
	public static byte[] getMD5(InputStream input)
	{
		if(input == null)
		{
			return null;
		}
		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[1024];
			for(; ;)
			{
				int read = input.read(buffer);
				if(read < 0)
				{
					break;
				}
				messageDigest.update(buffer, 0, read);
			}
			return messageDigest.digest();
		}
		catch(Throwable ex)
		{
			final Logger logger = LoggerFactory.getLogger(ApplicationPreferences.class);
			if(logger.isWarnEnabled()) logger.warn("Exception while calculating checksum!", ex);
			IOUtilities.interruptIfNecessary(ex);
		}
		finally
		{
			try
			{
				input.close();
			}
			catch(IOException e)
			{
				// ignore
			}
		}
		return null;
	}

	public void flush()
	{
		try
		{
			PREFERENCES.flush();
		}
		catch(BackingStoreException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while flushing preferences!", e);
		}
	}

	public boolean isReplacingOnApply(ActionEvent event)
	{
		// TODO make default behavior configurable.
		boolean result = false;
		if(event == null || (ActionEvent.SHIFT_MASK & event.getModifiers()) == 0)
		{
			result = true;
		}
		return result;
	}

	public void setUsingScreenMenuBar(boolean usingScreenMenuBar)
	{
		this.usingScreenMenuBar = usingScreenMenuBar;
	}

	public boolean isUsingScreenMenuBar()
	{
		return usingScreenMenuBar;
	}

	/**
	 * As described in http://weblogs.java.net/blog/malenkov/archive/2006/08/how_to_encode_e.html
	 */
	static class EnumPersistenceDelegate
		extends PersistenceDelegate
	{
		protected boolean mutatesTo(Object oldInstance, Object newInstance)
		{
			return oldInstance == newInstance;
		}

		protected Expression instantiate(Object oldInstance, Encoder out)
		{
			Enum e = (Enum) oldInstance;
			return new Expression(e, e.getClass(), "valueOf", new Object[]{e.name()});
		}
	}
}
