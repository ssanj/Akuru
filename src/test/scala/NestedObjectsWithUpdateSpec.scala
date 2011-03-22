/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class NestedObjectsWithUpdateSpec extends CommonSpec with AkuruDSL with NestedObjectCommon {

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
        success
      } withoutResults error("The DS was not updated") )
    ) ~~>() verifySuccess
  }

  "A NestedObect on update" should "update a nested object" in {
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
        success
      } withoutResults error("The DS was not updated") )
    ) ~~>() verifySuccess
  }
}