/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

trait DomainTemplateSupport { this:DomainTypeSupport with DomainTemplateFieldSupport with OwnerFieldSupport =>

  sealed abstract class Template[O <: DomainObject]

  abstract class NestedTemplate[O <: DomainObject, N <: NestedObject](parentField:FieldType[O, N]) extends Template[O] {

    //TODO: Find a better way to do this.
    def this(parentField:NestedArrayField[O, N]) = this(NestedField[O, N](parentField.parentField, parentField.name))

    def field[T](name:String): NestedField[O, T] = new Owner[O].createNestedField[T](parentField, name)

    def arrayField[T](name:String): NestedArrayField[O, T] = new Owner[O].createNestedArrayField[T](parentField, name)

    def nestedToMongoObject(nested: N): MongoObject

    def mongoToNested(mo:MongoObject): Option[N]

    implicit def _nestedToMongoObject(nested: N): MongoObject = nestedToMongoObject(nested)

    implicit def _mongoToNested(mo:MongoObject): Option[N] = mongoToNested(mo)
  }

  abstract class DomainTemplate[O <: DomainObject] extends Template[O] {
    private val idKey = "_id"
    val idField: Field[O, MID] = new Owner[O].createField[MID](idKey)
    val defaultId: idField.Value = idField === None

    def field[T](name:String): Field[O, T] = new Owner[O].createField[T](name)

    def embeddedField[T <: NestedObject](name:String): EmbeddedField[O, T] = new Owner[O].embeddedField[T](name)

    def arrayField[T](name:String): ArrayField[O, T] = new Owner[O].createArrayField[T](name)

    def embeddedArrayField[T <: NestedObject](name:String): EmbeddedArrayField[O, T] = new Owner[O].embeddedArrayField[T](name)

    val collectionName:String

    def domainToMongoObject(domain: O): MongoObject

    def mongoToDomain(mo:MongoObject): Option[O]

    implicit def _domainToMongoObject(domain: O): MongoObject = domainToMongoObject(domain)

    implicit def _mongoToDomain(mo:MongoObject): Option[O] = mongoToDomain(mo)


    implicit object DomainCollectionName extends CollectionName[O] {
      override lazy val name:String = collectionName
    }
  }
}