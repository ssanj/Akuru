/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

final class NestedObjectsWithQuerySpec extends CommonSpec with AkuruDSL with NestedObjectCommon {

  import Spend._

  "A NestedTemplateField object" should "be found by field a search" in  {
    ( init ~~>
      ( find * DailySpend where (Tag.nameField === "books") withResults{ds =>
        ds.size should equal (1)
        val dailyspend = ds(0)
        dailyspend.date.value should equal (123456L)
        val spend = dailyspend.spends.value
        spend.cost.value should equal (50.99D)
        spend.description.value should equal ("books")
        val tags = spend.tags.value
        tags.size should equal (2)
        val expectedTags = tags map (_.name.value)
        expectedTags should contain ("books")
        expectedTags should contain ("misc")
        success
      } withoutResults error("Could not find DS."))
    ) ~~>() verifySuccess
  }

  it should "be found by a numeric operation" in {
    ( init ~~>
      ( find * DailySpend where (costField < 50D) withResults(_ => error("shouldn't have found DS.")) withoutResults success ) ~~>
      ( find * DailySpend where (costField > 50D) withResults (_ => success) withoutResults error("could not find DS.") )
    ) ~~>() verifySuccess
  }

  it should "be found by a regex" in {
    ( init ~~>
      ( find * DailySpend where (descriptionField === ("mis*"/)) withResults (_ => error("shouldn't have found DS.")) withoutResults success ) ~~>
      ( find * DailySpend where (descriptionField === ("boo*"/)) withResults (_ => success) withoutResults error("could not find DS.") )
    ) ~~>() verifySuccess
  }
}