/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoUpdateObject

/**
 * { $set : { field : value } }
 * sets field to value. All datatypes are supported with $$$set.
 */
trait SetFuncs { this:Funcs =>

  private object SetFuncs {
    val functionName = "$set"
  }

  import SetFuncs._
  import NestedObject._

  /**
   * Used when you do $set(field1 === value1 & field2 === value2 & .... fieldx === valuex)
   */
  def $set[O <: DomainObject](update: MongoUpdateObject[O]): MongoUpdateObject[O] =  toMongoUpdateObject[O]($funcMongo(functionName, update.value))

  /**
   * Used when you do $set(field1 === value1)
   */
  def $set[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): MongoUpdateObject[O] =
    toMongoUpdateObject[O](anyFunction1[O, T](functionName, fv, nestedPath))

  /**
   * Used when you want to replace an EmbeddedField.
   * @param fv FieldType that points to a NestedObject.
   * @param value An instance of a NestedObject.
   *
   * Eg. DailySpends.spendsField, Spend(costField === 10, descriptionField === "blah",
   *  tagsField === Seq(Tag(nameField === "tag1), Tag(nameField === "tag2"))
   */
  def $set[O <: DomainObject, T <: NestedObject : ClassManifest](fv: EmbeddedField[O, T], value: => T): MongoUpdateObject[O] =
    setObject[O, T](fv.path, nestedObjectToMongo[O, T](value))

  /**
   * Used when you want to replace an EmbeddedArrayField.
   * @param fv FieldType that points to a NestedObject.
   * @param values A Seq of NestedObjects.
   * TODO: Test.
   */
  def $set[O <: DomainObject, T <: NestedObject : ClassManifest](fv: EmbeddedArrayField[O, T], values: => Seq[T]):
  MongoUpdateObject[O] =
    setArray[O, T](fv.path, values.map(v => nestedObjectToMongo[O, T](v)))

  /**
   * Used to replace a single NestedEmbeddedField value.
   * @param fv FieldType that points to a NestedObject.
   * @param value An instance of a NestedObject.
   * TODO: Test
   */
  def $set[O <: DomainObject, T <: NestedObject : ClassManifest](fv: NestedEmbeddedField[O, T], value: => T):
  MongoUpdateObject[O] = setObject[O, T](fv.path, nestedObjectToMongo[O, T](value))
//    toMongoUpdateObject[O](($funcMongo(functionName, mongo.putMongo(fv.path, implicitly[NestedToMongo[T]].apply(value) ))))

  /**
   * Used when you want to replace a NestedEmbeddedArrayField.
   * @param fv FieldType that points to a NestedObject.
   * @param values A Seq of NestedObjects.
   * TODO: Test
   *
   * Eg. Spends.tags
   */
  def $set[O <: DomainObject, T <: NestedObject : ClassManifest](fv: NestedEmbeddedArrayField[O, T], values: => Seq[T]):
  MongoUpdateObject[O] =
    setArray[O, T](fv.path, values.map(v => nestedObjectToMongo[O, T](v)))

  private def setArray[O <: DomainObject, T <: NestedObject : ClassManifest](path:String, values: => Seq[MongoObject]): MongoUpdateObject[O] = {
    toMongoUpdateObject[O]($funcMongo(functionName, mongo.putMongoArray(path, values)))
  }

  private def setObject[O <: DomainObject, T <: NestedObject : ClassManifest](path:String, value: MongoObject): MongoUpdateObject[O] = {
    toMongoUpdateObject[O](($funcMongo(functionName, mongo.putMongo(path, value))))
  }
}