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
package de.huxhorn.lilith.conditions

import spock.lang.Specification
import spock.lang.Unroll

class EventContainsConditionSpec extends Specification {
	@Unroll
	def "Corpus works as expected for #condition (searchString=#input)."() {
		expect:
		Corpus.executeConditionOnCorpus(condition) == expectedResult

		where:
		input                            | expectedResult
		null                             | [] as Set
		''                               | Corpus.matchAllSet()
		'snafu'                          | [] as Set

		// level
		'EBU'                            | [9] as Set

		// logger
		'foo'                            | [13, 14] as Set
		'com'                            | [13, 14] as Set
		'com.foo'                        | [13, 14] as Set
		'com.foo.Foo'                    | [13] as Set
		'com.foo.Bar'                    | [14] as Set

		// message & ndc
		'message'                        | [17, 18, 19, 20, 21, 36, 37, 38, 39, 40] as Set
		'a message'                      | [17, 19, 20, 21, 36, 38, 39, 40] as Set
		'another message'                | [18, 37] as Set
		'a message.'                     | [17, 36] as Set
		'another message.'               | [18, 37] as Set
		'paramValue'                     | [19, 21, 22, 38, 40, 41] as Set
		'{}'                             | [20, 21, 23, 38, 39, 40, 41, 42] as Set

		// mdc
		'Key'                            | [24] as Set

		// mdc & paramValue
		'Value'                          | [19, 21, 22, 24, 38, 40, 41] as Set

		// throwable
		'java.lang.RuntimeException'     | [25, 26, 27, 28, 29, 30] as Set
		'java.lang.NullPointerException' | [26, 27, 28, 29, 30] as Set
		'java.lang.FooException'         | [27, 29, 30] as Set
		'java.lang.BarException'         | [30] as Set
		'RuntimeException'               | [25, 26, 27, 28, 29, 30] as Set
		'Exception'                      | [25, 26, 27, 28, 29, 30] as Set
		'java.lang'                      | [25, 26, 27, 28, 29, 30] as Set

		// Marker
		'-Marker'                        | [31, 32] as Set
		'Foo-Marker'                     | [31] as Set
		'Bar-Marker'                     | [31, 32] as Set

		condition = new EventContainsCondition(input)
	}
}