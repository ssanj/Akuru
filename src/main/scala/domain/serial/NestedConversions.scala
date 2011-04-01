/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package domain
package serial

import MongoObject.empty

trait NestedConversions {

  object NestedObject {

    class NestedObjectToMongo[T <: NestedObject] extends ToMongo[T] {
      def convert[O <: DomainObject](fv:FieldValue[O, T]): MongoObject =  empty.putMongo(fv.name, toMongo(fv.value))
    }

    def nestedObjectToMongo[O <: DomainObject, T <: NestedObject](nested:T): MongoObject = toMongo(nested)

    class NestedObjectArrayToMongo[T <: NestedObject] extends ToMongo[Seq[T]] {
      def convert[O <: DomainObject](fv:FieldValue[O, Seq[T]]): MongoObject = {
        val mongos:Seq[MongoObject] = fv.value.map(n => toMongo(n.asInstanceOf[Product]))
        empty.putMongoArray(fv.name, mongos)
      }
    }

     private def toMongo(product:Product): MongoObject = {
        val mongos = product.productIterator.collect { case fv:FieldValue[_, _] => fv } map ((fv:FieldValue[_, _]) =>
          fv.value match {
             case n:NestedObject => empty.putMongo(fv.name, toMongo(n.asInstanceOf[Product]))
             case _ => fv.mongo
        })

        mongos.foldLeft(empty)(_.merge(_))
      }
  }
}