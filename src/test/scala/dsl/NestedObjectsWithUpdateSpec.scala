/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

final class NestedObjectsWithUpdateSpec extends CommonSpec with AkuruDSL with NestedObjectCommon {

  import DailySpend._
  import Spend._
  import Tag._
  import MongoTypes.MongoObject.$set
  import MongoTypes.MongoObject.$push
  "A NestedObect on update" should "update nested values" in {
    ( init ~~>
      ( update a DailySpend where (costField > 50D) withValues(
              $set(costField === 60.00D & descriptionField === "updated") &
                      $push(tagsField, Tag(nameField === "blah"))) returnErrors ) ~~>
      ( find * DailySpend where (costField > 58D) withResults{ds =>
        ds.size should equal (1)
        val dailyspend = ds(0)
        val spends = dailyspend.spends.value
        spends.cost.value should equal (60D)
        spends.description.value should equal ("updated")
        val tags = spends.tags.value
        val expected = tags map (_.name.value)
        expected should contain ("blah")
        success
      } withoutResults error("The DS was not updated") )
    ) ~~>() verifySuccess
  }

  it should "update a nested object" in {
    ( init ~~>
      ( update a DailySpend where (costField > 50D) withValues(
              $set(spendsField, Spend(costField === 200.22D, descriptionField === "spendyy",
                tagsField === Seq(Tag(nameField === "set"), Tag(nameField === "it"), Tag(nameField === "up") )))
        ) returnErrors ) ~~>
      ( find * DailySpend where (costField > 58D) withResults{ds =>
        ds.size should equal (1)
        val dailyspend = ds(0)
        val spends = dailyspend.spends.value
        spends.cost.value should equal (200.22D)
        spends.description.value should equal ("spendyy")
        val tags = spends.tags.value
        val expected = tags map (_.name.value)
        expected should contain ("set")
        expected should contain ("it")
        expected should contain ("up")
        success
      } withoutResults error("The DS was not updated") )
    ) ~~>() verifySuccess
  }
}