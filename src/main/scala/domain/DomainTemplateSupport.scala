/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

trait DomainTemplateSupport { this:DomainTypeSupport with DomainTemplateFieldSupport with OwnerFieldSupport =>

  sealed abstract class Template[O <: DomainObject]

  abstract class NestedTemplate[O <: DomainObject, N <: NestedObject] extends Template[O] {

    val parentField:FieldType[O, N]

    def fromType(pf:EmbeddedField[O, N]) = Field[O, N](pf.name).asInstanceOf[FieldType[O, N]]

    def fromType(pf:EmbeddedArrayField[O, N]) = EmbeddedField[O, N](pf.name).asInstanceOf[FieldType[O, N]]

    def fromType(pf:NestedEmbeddedField[O, N]) = NestedEmbeddedField[O, N](pf.parentField, pf.name).asInstanceOf[FieldType[O, N]]

    def fromType(pf:NestedEmbeddedArrayField[O, N]) = NestedEmbeddedField[O, N](pf.parentField, pf.name).
            asInstanceOf[FieldType[O, N]]

    def field[T : Primitive](name:String): NestedField[O, T] = new Owner[O].nestedField[T](parentField, name)

    def arrayField[T : Primitive](name:String): NestedArrayField[O, T] = new Owner[O].nestedArrayField[T](parentField, name)

    def embeddedField[T <: NestedObject](name:String): NestedEmbeddedField[O, T] = NestedEmbeddedField[O, T](parentField, name)

    def embeddedArrayField[T <: NestedObject](name:String): NestedEmbeddedArrayField[O, T] = NestedEmbeddedArrayField[O, T](parentField, name)

    def nestedToMongoObject(nested: N): MongoObject

    def mongoToNested(mo:MongoObject): Option[N]

    implicit def _nestedToMongoObject(nested: N): MongoObject = nestedToMongoObject(nested)

    implicit def _mongoToNested(mo:MongoObject): Option[N] = mongoToNested(mo)
  }

  abstract class DomainTemplate[O <: DomainObject : ClassManifest] extends Template[O] {
    private val idKey = "_id"
    val idField: Field[O, MID] = new Owner[O].field[MID](idKey)
    val defaultId: idField.Value = idField === None

    def field[T : Primitive](name:String): Field[O, T] = new Owner[O].field[T](name)

    def enumField[T <: Enumeration#Value](name:String): Field[O, T] = new Owner[O].field[T](name)

    def embeddedField[T <: NestedObject](name:String): EmbeddedField[O, T] = new Owner[O].embeddedField[T](name)

    def arrayField[T : Primitive](name:String): ArrayField[O, T] = new Owner[O].arrayField[T](name)

    def embeddedArrayField[T <: NestedObject](name:String): EmbeddedArrayField[O, T] = new Owner[O].embeddedArrayField[T](name)

    def domainToMongoObject(domain: O): MongoObject

    def mongoToDomain(mo:MongoObject): Option[O]

    implicit def _domainToMongoObject(domain: O): MongoObject = domainToMongoObject(domain)

    implicit def _mongoToDomain(mo:MongoObject): Option[O] = mongoToDomain(mo)

    lazy val collectionName:String = implicitly[ClassManifest[O]].erasure.getSimpleName.toLowerCase


    implicit object DomainCollectionName extends CollectionName[O] {
      override lazy val name:String = collectionName
    }
  }
}