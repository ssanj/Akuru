/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.putId
import MongoTypes.MongoObject.empty

trait NestedDomainObjects {

  case class DailySpend(date: DailySpend.dateField.Value,
                        spends: DailySpend.spendsField.Value,
                        id: DailySpend.idField.Value = DailySpend.defaultId) extends DomainObject

  object DailySpend extends DomainTemplate[DailySpend] {
    val dateField = field[Long]("currentDate")
    val spendsField = field[Spend]("spends")

    override def domainToMongoObject(ds: DailySpend): MongoObject = {
      putId(ds.id.value).putAnything(ds.date).putNested(spendsField, ds.spends)
    }

    override def mongoToDomain(mo:MongoObject): Option[DailySpend] = {
      for {
        date <- mo.getPrimitiveObject(dateField)
        spends <- mo.getNestedObject(spendsField)
      } yield DailySpend(dateField === date, spendsField === spends, idField === mo.getId)
    }

    val collectionName = "ds"

    case class Spend(cost: Spend.costField.Value, description: Spend.descriptionField.Value, tags: Spend.tagsField.Value) extends NestedObject

    object Spend extends NestedTemplate[DailySpend](spendsField){
      val costField = field[Double]("cost")
      val descriptionField = field[String]( "description")
      val tagsField = field[Seq[Spend.Tag]]("tags")

      implicit def spendsToMongo(spend:Spend): MongoObject = {
        empty.putAnything(spend.cost).putAnything(spend.description).putNestedArray(tagsField, spend.tags)
      }

      implicit def mongoToSpend(mo:MongoObject): Option[Spend] = {
        for {
          cost <- mo.getPrimitiveObject(costField)
          description <- mo.getPrimitiveObject(descriptionField)
          tags <- mo.getNestedObjectArray(tagsField)
        } yield Spend(costField === cost, descriptionField === description, tagsField === tags)
      }

      case class Tag(name: Tag.nameField.Value) extends NestedObject

      object Tag extends NestedTemplate[DailySpend](tagsField) {
        val nameField = field[String]("name")

        implicit def tagToMongo(tag: Tag): MongoObject =  empty.putAnything(tag.name)

        implicit def mongoToTag(mo:MongoObject): Option[Tag] = {
          for {
            name <- mo.getPrimitiveObject(nameField)
          } yield Tag(nameField === name)
        }
      }
    }
  }
}