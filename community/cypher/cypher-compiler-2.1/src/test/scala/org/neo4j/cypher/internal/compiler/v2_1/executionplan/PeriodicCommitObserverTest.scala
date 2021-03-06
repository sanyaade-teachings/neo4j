/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.cypher.internal.compiler.v2_1.executionplan

import org.scalatest.BeforeAndAfter
import org.neo4j.cypher.internal.compiler.v2_1.spi.QueryContext
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.neo4j.graphdb.Node
import org.neo4j.cypher.internal.commons.CypherFunSuite

class PeriodicCommitObserverTest extends CypherFunSuite with BeforeAndAfter with MockitoSugar {


  var queryContext: QueryContext = _
  var observer: PeriodicCommitObserver = _

  before {
    queryContext = mock[QueryContext]
    observer = new PeriodicCommitObserver(10, queryContext)
  }

  test("should commit every batch size updates") {
    observer.notify(10)

    verify(queryContext, times(1)).commitAndRestartTx()
  }

  test("should not commit if there are less then batch size updates") {
    observer.notify(9)

    verify(queryContext, never()).commitAndRestartTx()
  }
}
