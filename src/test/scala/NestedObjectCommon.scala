/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait NestedObjectCommon { this:CommonSpec =>

  import DailySpend._
  import Spend._
  import Tag._

  def init: FutureConnection = {
    initDailySpend ~~>
      save(DailySpend(dateField === 123456L,
            spendsField === Spend(costField === 50.99D, descriptionField === "books",
              tagsField === Seq(Tag(nameField === "books"), Tag(nameField === "misc")))))
  }
}