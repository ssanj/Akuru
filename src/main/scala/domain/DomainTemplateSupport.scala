/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

import MongoObject.empty

trait DomainTemplateSupport { this:DomainTemplateFieldSupport with OwnerFieldSupport =>

  sealed abstract class Template[O <: DomainObject] {
    def toMongo(product:Product): MongoObject = {
        val mongos = product.productIterator.collect { case fv:FieldValue[_, _] => fv } map ((fv:FieldValue[_, _]) => fv.mongo)
        mongos.foldLeft(empty)(_.merge(_))
    }
  }

  abstract class NestedTemplate[O <: DomainObject, N <: NestedObject] extends Template[O] {

    val parentField:FieldType[O, N]

    def flatten(f:EmbeddedArrayField[O, N]): FieldType[O, N] = EmbeddedField[O, N](f.name)

    def flatten(f:NestedEmbeddedArrayField[O, N]): FieldType[O, N] = NestedEmbeddedField[O, N](f.parentField, f.name)

    def field[T : Primitive : ToMongo](name:String): NestedField[O, T] = new Owner[O].nestedField[T](parentField, name)

    def arrayField[T](name:String)(implicit p:Primitive[T], tm:ToMongo[Seq[T]]): NestedArrayField[O, T] =
      new Owner[O].nestedArrayField[T](parentField, name)

    def embeddedField[T <: NestedObject](name:String): NestedEmbeddedField[O, T] = NestedEmbeddedField[O, T](parentField, name)

    def embeddedArrayField[T <: NestedObject](name:String): NestedEmbeddedArrayField[O, T] = NestedEmbeddedArrayField[O, T](parentField, name)

    def mongoToNested(mo:MongoObject): Option[N]

    implicit def _mongoToNested(mo:MongoObject): Option[N] = mongoToNested(mo)
  }

  abstract class DomainTemplate[O <: DomainObject : ClassManifest] extends Template[O] {
    private val idKey = "_id"
    val idField: Field[O, MID] = new Owner[O].field[MID](idKey)
    val defaultId: idField.Value = idField === None

    def field[T : Primitive : ToMongo](name:String): Field[O, T] = new Owner[O].field[T](name)

    def embeddedField[T <: NestedObject](name:String): EmbeddedField[O, T] = new Owner[O].embeddedField[T](name)

    def arrayField[T](name:String)(implicit p:Primitive[T], tm:ToMongo[Seq[T]]): ArrayField[O, T] = new Owner[O].arrayField[T](name)

    def embeddedArrayField[T <: NestedObject](name:String): EmbeddedArrayField[O, T] = new Owner[O].embeddedArrayField[T](name)

    def mongoToDomain(mo:MongoObject): Option[O]

    //implicit def _domainToMongoObject(domain: O): MongoObject = domainToMongoObject(domain)
    implicit def _domainToMongoObject(domain: O): MongoObject =  toMongo(domain)

    implicit def _mongoToDomain(mo:MongoObject): Option[O] = mongoToDomain(mo)

    lazy val collectionName:String = implicitly[ClassManifest[O]].erasure.getSimpleName.toLowerCase


    implicit object DomainCollectionName extends CollectionName[O] {
      override lazy val name:String = collectionName
    }
  }
}