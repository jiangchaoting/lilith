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
package de.huxhorn.lilith.swing.menu;

import de.huxhorn.lilith.swing.ApplicationPreferences;
import de.huxhorn.lilith.swing.actions.FilterAction;
import de.huxhorn.lilith.swing.actions.FocusCallLocationAction;
import de.huxhorn.lilith.swing.actions.FocusFormattedMessageAction;
import de.huxhorn.lilith.swing.actions.FocusHttpMethodAction;
import de.huxhorn.lilith.swing.actions.FocusHttpRemoteUserAction;
import de.huxhorn.lilith.swing.actions.FocusHttpRequestUrlAction;
import de.huxhorn.lilith.swing.actions.FocusHttpStatusCodeAction;
import de.huxhorn.lilith.swing.actions.FocusMessagePatternAction;
import de.huxhorn.lilith.swing.actions.FocusThreadGroupNameAction;
import de.huxhorn.lilith.swing.actions.FocusThreadNameAction;
import de.huxhorn.lilith.swing.actions.FocusThrowableAction;
import de.huxhorn.lilith.swing.actions.FocusThrowablesAction;
import de.huxhorn.lilith.swing.actions.NegateFilterAction;
import javax.swing.JMenuItem;

public class ExcludeMenu
	extends AbstractFilterMainMenu
{
	private static final long serialVersionUID = 7891868504135342449L;

	public ExcludeMenu(ApplicationPreferences applicationPreferences, boolean htmlTooltip)
	{
		super("Exclude", applicationPreferences, htmlTooltip);
		createUI();
		viewContainerUpdated();
	}

	private void createUI()
	{
		FilterAction messagePatternAction = new NegateFilterAction(new FocusMessagePatternAction(htmlTooltip));
		registerFilterAction(messagePatternAction);

		FilterAction formattedMessageAction = new NegateFilterAction(new FocusFormattedMessageAction(htmlTooltip));
		registerFilterAction(formattedMessageAction);

		FilterAction callLocationAction = new NegateFilterAction(new FocusCallLocationAction());
		registerFilterAction(callLocationAction);

		FilterAction throwablesAction = new NegateFilterAction(new FocusThrowablesAction());
		registerFilterAction(throwablesAction);

		FilterAction throwableAction = new NegateFilterAction(new FocusThrowableAction());
		registerFilterAction(throwableAction);

		FilterAction threadNameAction = new NegateFilterAction(new FocusThreadNameAction());
		registerFilterAction(threadNameAction);

		FilterAction threadGroupNameAction = new NegateFilterAction(new FocusThreadGroupNameAction());
		registerFilterAction(threadGroupNameAction);

		FilterAction statusCodeAction = new NegateFilterAction(new FocusHttpStatusCodeAction());
		registerFilterAction(statusCodeAction);

		FilterAction methodAction = new NegateFilterAction(new FocusHttpMethodAction());
		registerFilterAction(methodAction);

		FilterAction requestUrlAction = new NegateFilterAction(new FocusHttpRequestUrlAction());
		registerFilterAction(requestUrlAction);

		FilterAction remoteUserAction = new NegateFilterAction(new FocusHttpRemoteUserAction());
		registerFilterAction(remoteUserAction);

		AbstractFilterMenu savedMenu = new ExcludeSavedConditionsMenu(applicationPreferences, htmlTooltip);
		registerAbstractFilterMenu(savedMenu);

		AbstractFilterMenu loggerMenu = new ExcludeLoggerMenu();
		registerAbstractFilterMenu(loggerMenu);

		// no levelMenu

		AbstractFilterMenu mdcMenu = new ExcludeMDCMenu();
		registerAbstractFilterMenu(mdcMenu);

		AbstractFilterMenu markerMenu = new ExcludeMarkerMenu();
		registerAbstractFilterMenu(markerMenu);

		AbstractFilterMenu ndcMenu = new ExcludeNDCMenu(htmlTooltip);
		registerAbstractFilterMenu(ndcMenu);

		AbstractFilterMenu statusTypeMenu = new ExcludeHttpStatusTypeMenu();
		registerAbstractFilterMenu(statusTypeMenu);

		AbstractFilterMenu requestUriMenu = new ExcludeHttpRequestUriMenu();
		registerAbstractFilterMenu(requestUriMenu);

		AbstractFilterMenu requestParameterMenu = new ExcludeRequestParameterMenu();
		registerAbstractFilterMenu(requestParameterMenu);

		AbstractFilterMenu requestHeaderMenu = new ExcludeRequestHeaderMenu();
		registerAbstractFilterMenu(requestHeaderMenu);

		AbstractFilterMenu responseHeaderMenu = new ExcludeResponseHeaderMenu();
		registerAbstractFilterMenu(responseHeaderMenu);


		JMenuItem messagePatternItem = new JMenuItem(messagePatternAction);
		JMenuItem formattedMessageItem = new JMenuItem(formattedMessageAction);
		JMenuItem callLocationItem = new JMenuItem(callLocationAction);
		JMenuItem throwablesItem = new JMenuItem(throwablesAction);
		JMenuItem throwableItem = new JMenuItem(throwableAction);
		JMenuItem threadNameItem = new JMenuItem(threadNameAction);
		JMenuItem threadGroupNameItem = new JMenuItem(threadGroupNameAction);
		JMenuItem statusCodeItem = new JMenuItem(statusCodeAction);
		JMenuItem methodItem = new JMenuItem(methodAction);
		JMenuItem requestUrlItem = new JMenuItem(requestUrlAction);
		JMenuItem remoteUserItem = new JMenuItem(remoteUserAction);


		registerLoggingComponent(savedMenu);
		registerLoggingComponent(null);
		registerLoggingComponent(loggerMenu);
		registerLoggingComponent(null);
		registerLoggingComponent(messagePatternItem);
		registerLoggingComponent(formattedMessageItem);
		registerLoggingComponent(null);
		registerLoggingComponent(callLocationItem);
		registerLoggingComponent(null);
		registerLoggingComponent(throwablesItem);
		registerLoggingComponent(throwableItem);
		registerLoggingComponent(null);
		registerLoggingComponent(threadNameItem);
		registerLoggingComponent(threadGroupNameItem);
		registerLoggingComponent(null);
		registerLoggingComponent(mdcMenu);
		registerLoggingComponent(markerMenu);
		registerLoggingComponent(ndcMenu);


		registerAccessComponent(savedMenu);
		registerAccessComponent(null);
		registerAccessComponent(statusCodeItem);
		registerAccessComponent(statusTypeMenu);
		registerAccessComponent(null);
		registerAccessComponent(methodItem);
		registerAccessComponent(null);
		registerAccessComponent(requestUriMenu);
		registerAccessComponent(requestUrlItem);
		registerAccessComponent(null);
		registerAccessComponent(requestParameterMenu);
		registerAccessComponent(requestHeaderMenu);
		registerAccessComponent(responseHeaderMenu);
		registerAccessComponent(null);
		registerAccessComponent(remoteUserItem);
	}
}
