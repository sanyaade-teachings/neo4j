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
package org.neo4j.cypher.internal.compiler.v2_1

import org.neo4j.cypher.internal.compiler.v2_1.commands.{PeriodicCommitQuery, AbstractQuery}
import executionplan.{ExecutionPlanBuilder, ExecutionPlan}
import executionplan.verifiers.HintVerifier
import parser.CypherParser
import spi.PlanContext
import org.neo4j.cypher.SyntaxException
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.cypher.internal.compiler.v2_1.ast.Statement

case class CypherCompiler(graph: GraphDatabaseService, queryCache: (String, => Object) => Object) {
  val parser = CypherParser()
  val verifiers = Seq(HintVerifier)
  val planBuilder = new ExecutionPlanBuilder(graph)

  @throws(classOf[SyntaxException])
  def isPeriodicCommit(queryText: String) = cachedQuery(queryText) match {
    case (_: PeriodicCommitQuery, _) => true
    case _ => false
  }

  @throws(classOf[SyntaxException])
  def prepare(queryText: String, context: PlanContext): ExecutionPlan = {
    val (query: AbstractQuery, statement: Statement) = cachedQuery(queryText)
    planBuilder.build(context, query, statement)
  }

  private def cachedQuery(queryText: String): (AbstractQuery, Statement) =
    queryCache(queryText, {
      verify(parse(queryText))
    }).asInstanceOf[(AbstractQuery, Statement)]

  private def verify(query: (AbstractQuery, Statement)): (AbstractQuery, Statement) = {
    query._1.verifySemantics()
    for (verifier <- verifiers)
      verifier.verify(query._1)
    query
  }

  private def parse(query: String): (AbstractQuery, Statement) = parser.parseToQuery(query)
}
