/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class PathAwareNestingSpec extends CommonSpec{

 import DailySpend._
 import Spend._
 import Tag._

 "A DomainObject with a flat field" should "be path-aware" in {
    Blog.titleField.path should equal (Blog.titleField.name)
    Blog.labelsField.path should equal (Blog.labelsField.name)
 }

 "A NestedObject within a DomainObject" should "be path-aware" in {
  Spend.descriptionField.path should equal ("spends.description")
  Spend.costField.path should equal ("spends.cost")
  Spend.tagsField.path should equal ("spends.tags")
 }

 "A NestedObject within a NestedObject" should "be path-aware" in {
   Tag.nameField.path should equal ("spends.tags.name")
 }
}