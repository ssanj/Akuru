/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.putId
import MongoTypes.Query
import MongoTypes.MongoObject.empty

trait NestedDomainObjects {

  case class DailySpend(date: DailySpend.dateField.Value,
                        spends: DailySpend.spendsField.Value,
                        id: DailySpend.idField.Value = DailySpend.defaultId) extends DomainObject

  object DailySpend extends DomainTemplate[DailySpend] {
    val dateField = field[Long]("currentDate")
    val spendsField = field[Spend]("spends")

    implicit def dailySpendToMongoObject(ds: DailySpend): MongoObject = {
      putId(ds.id.value).putAnything(ds.date).putNested(spendsField, ds.spends)
    }

    implicit object DSCollection extends CollectionName[DailySpend] {
      override val name = "ds"
    }

    case class Spend(cost: Spend.costField.Value, description: Spend.descriptionField.Value, tags: Spend.tagsField.Value) extends NestedObject

    object Spend extends Template[DailySpend]{
      val costField = nestedField[Double](spendsField, "cost")
      val descriptionField = nestedField[String](spendsField, "description")
      val tagsField = nestedField[Seq[Spend.Tag]](spendsField, "tags")

      implicit def spendsToMongo(spend:Spend): MongoObject = {
        empty.putAnything(spend.cost).putAnything(spend.description).putNestedArray(tagsField, spend.tags)
      }

      case class Tag(name: Tag.nameField.Value) extends NestedObject

      object Tag extends Template[DailySpend] {
        val nameField = nestedField[String](tagsField, "name")

        implicit def tagToMongo(tag: Tag): MongoObject =  empty.putAnything(tag.name)
      }
    }
//    implicit def mongoToDailySpend(mo:MongoObject): Option[DailySpend] = {
//      for {
//        date <- mo.getPrimitiveObject(dateField)
//        spends <- mo.getMongoObject(spendsField)
//      } yield DailySpend(dateField === date, spendsField === spends, idField === mo.getId)
//    }


//    implicit def mongoToSpend(mo:MongoObject): Option[Spend] = {
//      for {
//        cost <- mo.getPrimitiveObject(costField)
//        description <- mo.getPrimitiveObject(descriptionField)
//        labels <- mo.getPrimitiveObjects(labelsField)
//      } yield Spend(costField === cost, descriptionField === description, labelsField === labels)
//    }
  }
}