/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

import MongoTypes.putId
import MongoTypes.MongoObject.empty

trait NestedDomainObjects {

//  case class DailySpend(date: DailySpend.dateField.Value,
//                        spends: DailySpend.spendsField.Value,
//                        id: DailySpend.idField.Value = DailySpend.defaultId) extends DomainObject
//
//  object DailySpend extends DomainTemplate[DailySpend] {
//    val dateField = field[Long]("currentDate")
//    val spendsField = embeddedField[Spend]("spends")
//
//    override def domainToMongoObject(ds: DailySpend): MongoObject = {
//      putId(ds.id).putAnything(ds.date).putNested(ds.spends)
//    }
//
//    override def mongoToDomain(mo:MongoObject): Option[DailySpend] = {
//      for {
//        date <- mo.getPrimitiveObject(dateField)
//        spends <- mo.getNestedObject(spendsField)
//        id <- mo.getIdObject
//      } yield DailySpend(dateField === date, spendsField === spends, idField === id)
//    }
//  }
//
//  case class Spend(cost: Spend.costField.Value, description: Spend.descriptionField.Value, tags: Spend.tagsField.Value) extends NestedObject
//
//  object Spend extends NestedTemplate[DailySpend, Spend]{
//    override val parentField = DailySpend.spendsField
//    val costField = field[Double]("cost")
//    val descriptionField = field[String]( "description")
//    val tagsField = embeddedArrayField[Tag]("tags")
//
//    override def nestedToMongoObject(spend:Spend): MongoObject = {
//      empty.putAnything(spend.cost).putAnything(spend.description).putNestedArray(spend.tags)
//    }
//
//    override def mongoToNested(mo:MongoObject): Option[Spend] = {
//      for {
//        cost <- mo.getPrimitiveObject(costField)
//        description <- mo.getPrimitiveObject(descriptionField)
//        tags <- mo.getNestedObjectArray(tagsField)
//      } yield Spend(costField === cost, descriptionField === description, tagsField === tags)
//    }
//  }
//
//  case class Tag(name: Tag.nameField.Value) extends NestedObject
//
//  object Tag extends NestedTemplate[DailySpend, Tag] {
//    override val parentField = fromType(Spend.tagsField)
//    val nameField = field[String]("name")
//
//    override def nestedToMongoObject(tag: Tag): MongoObject =  empty.putAnything(tag.name)
//
//    override def mongoToNested(mo:MongoObject): Option[Tag] = {
//      for {
//        name <- mo.getPrimitiveObject(nameField)
//      } yield Tag(nameField === name)
//    }
//  }
}