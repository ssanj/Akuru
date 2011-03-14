/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoCollectionUpdateMultipleSpec extends AkuruDSL with CommonSpec with MongoCollectionCommonUpdateSpec {

  import Blog._
  import akuru.MongoTypes.MongoObject._

  override def cardinality: UpdateQuery[Blog] = ( update many Blog )
  override def specName = "A MongoCollection with Multiple Updates"
  override def expectedResults = 2

  it should "update multiple matched results" in {
    ( initBlog ~~>
      save(Blog(titleField === "Functor", labelsField === Seq("fp", "patterns"))) ~~>
      save(Blog(titleField === "Applicative", labelsField === Seq("fp", "patterns"))) ~~>
      save(Blog(titleField === "Semigroup", labelsField === Seq("fp", "patterns"))) ~~>
      save(Blog(titleField === "Lessons Learned", labelsField === Seq("work", "ideas"))) ~~>
      ( find many Blog where labelsField === ("fp"/) withResults {b => b.size should equal (3); success} ) ~~>
      ( update many Blog where labelsField === ("fp"/) withValues (set(labelsField === Seq("functional programming", "patterns")))
              returnErrors ) ~~>
      ( find many Blog where labelsField === ("fp"/) withResults {b => b.size should equal (0); success} ) ~~>
      ( find many Blog where labelsField === ("functional programming"/) withResults {b => b.size should equal (3); success} )
    ) ~~>() verifySuccess
  }
}