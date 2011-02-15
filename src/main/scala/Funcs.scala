/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoObject
import MongoTypes.FieldValue

trait Funcs {

  import com.mongodb.DBObject

  implicit def fieldValueToMongo[T](fv: FieldValue[T]): MongoObject = fieldToMongo1[T](fv)

  implicit def dbObjectToMongoObject(dbo: DBObject): MongoObject = MongoObject(dbo)

  implicit def MongoObjectToDBObject(mo: MongoObject): DBObject = mo.toDBObject

  implicit def tuple2PrimitiveToMongoObject(tuple2: Tuple2[String, AnyRef]): MongoObject = mongo.putPrimitive(tuple2._1, tuple2._2)

  implicit def tuple2MongoToMongoObject(tuple2: Tuple2[String, MongoObject]): MongoObject = mongo.putMongo(tuple2._1, tuple2._2)

  //TODO: remove this
  implicit def dboToDBOToMongo(dbo: DBObject): DBOToMongo = DBOToMongo(dbo)

  implicit def sequencedFVTOMongo[T](fv:FieldValue[Seq[T]]): SequencedFVTOMongo[T] = SequencedFVTOMongo[T](fv)

  implicit def mongoToMongoJoiner(mo:MongoObject): MongoJoiner = MongoJoiner(mo)

  implicit def fvToMongoJoiner[T](fv:FieldValue[T]): MongoJoiner = MongoJoiner(mongo.putPrimitive[T](fv))

  implicit def mongoJoinerToMongo(mj:MongoJoiner): MongoObject = mj.done

  def $funcMongo(action: String, col: String, value: MongoObject): MongoObject = mongo.putMongo(action, mongo.putMongo(col, value))

  def $funcMongo(action: String, value: MongoObject): MongoObject = mongo.putMongo(action, value)

  def $funcPrimitive(action: String, col: String, value: AnyRef): MongoObject = mongo.putMongo(action, mongo.putPrimitive(col, value))

  def fieldToMongo1[T](fv: FieldValue[T]): MongoObject = mongo.putPrimitive[T](fv)

  def fieldToMongo2[R, T](fv1: FieldValue[R], fv2: FieldValue[T]): MongoObject = mongo.putPrimitive(fv1).merge(mongo.putPrimitive(fv2))

  def fieldToMongo3[R, S, T](fv1: FieldValue[R], fv2: FieldValue[S], fv3: FieldValue[T]): MongoObject =
    mongo.putPrimitive(fv1).merge(mongo.putPrimitive(fv2)).merge(mongo.putPrimitive(fv3))

  def empty = new MongoObject

  def mongo = new MongoObject

  def query(tuples: Tuple2[String, AnyRef]*) = new MongoObject(tuples.toSeq)

  def mongoObject(tuples: Tuple2[String, AnyRef]*) = new MongoObject(tuples.toSeq)

  def combine(value:MongoObject*): MongoObject = if (value.isEmpty) mongo else value.foldLeft(value.head)((a, b) => a.merge(b))

  //TODO: remove this
  case class DBOToMongo(dbo: DBObject) {
    def toMongo(): MongoObject = MongoObject(dbo)
  }

  case class SequencedFVTOMongo[T](fv:FieldValue[Seq[T]]) {
    def splat(): MongoObject = mongo.putPrimitiveArray[T](fv)
  }

  case class MongoJoiner(mo:MongoObject) {

    def and(another:MongoObject): MongoJoiner = MongoJoiner(mo.merge(another))

    def done: MongoObject = mo
  }

  def anyFunction1[T]: String => FieldValue[T] => MongoObject = fname => fv => $funcMongo(fname, fieldToMongo1[T](fv))

  def anyFunction2[R, T]: String => (FieldValue[R], FieldValue[T]) => MongoObject = fname => (fv1, fv2) =>
    $funcMongo(fname, fieldToMongo2[R,T](fv1, fv2))

  def anyFunction3[R, S, T]: String => (FieldValue[R], FieldValue[S], FieldValue[T]) => MongoObject =
    fname => (fv1, fv2, fv3) => $funcMongo(fname, fieldToMongo3[R,S,T](fv1, fv2, fv3))
}