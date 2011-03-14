/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait QueryTypes {

  import MongoObject.mongo

  sealed trait SortObject {
    val value:MongoObject
  }

  object SortOrder extends Enumeration {
    val ASC =  Value(1)
    val DSC  = Value(-1)
  }

  case class MongoSortObject(mo:MongoObject) extends SortObject {
    override val value:MongoObject = mo
  }

  sealed trait UpdateObject {
    val value:MongoObject
  }

  case class DomainUpdateObject[T <: DomainObject : DomainToMongo](domain:T) extends UpdateObject {
    override val value:MongoObject = implicitly[DomainToMongo[T]].apply(domain)
  }

  case class MongoUpdateObject(mo:MongoObject) extends UpdateObject {
    override val value:MongoObject = mo

    def &(other:MongoUpdateObject): MongoUpdateObject = MongoUpdateObject(mo.mergeMongoObjectValues(other.value))

    import MongoTypes.MongoObject.mongo
    def &[O <: DomainObject, T : ClassManifest](other:FieldValue[O, T]): MongoUpdateObject =
      MongoUpdateObject(mo.mergeMongoObjectValues(mongo.putAnything[O, T](other)))
  }

  object UpdateObject {
    implicit def domainToUpdateObject[T <: DomainObject : DomainToMongo](value:T): UpdateObject = DomainUpdateObject[T](value)
    //we don't add an implicit def for mongo -> updateObject here as we want to limit it to specific mongo objects.
  }

  sealed abstract class JoinerValue[O <: DomainObject] {
    def done: MongoObject
  }

  case class FieldValueJoinerValue[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]) extends JoinerValue[O] {
    def done: MongoObject = mongo.putAnything[O, T](fv)
  }

  case class MongoJoinerValue[O <: DomainObject](mo: MongoObject) extends JoinerValue[O] {
    def done: MongoObject = mo
  }

  case class FieldValueJoiner[O <: DomainObject](join: JoinerValue[O]) {
    def and2[S : ClassManifest](fv2:FieldValue[O, S]): FieldValueJoiner[O] =
      FieldValueJoiner[O](MongoJoinerValue[O](join.done.putAnything[O, S](fv2)))

    def and2(another:FieldValueJoiner[O]): FieldValueJoiner[O] = FieldValueJoiner[O](MongoJoinerValue[O](join.done.merge(another.done)))

    def done: MongoObject = join.done
  }

  case class MongoJoiner(mo:MongoObject) {

    def and(another:MongoObject): MongoJoiner = MongoJoiner(mo.merge(another))

    def done: MongoObject = mo
  }
}