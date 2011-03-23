package akuru.domain

/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
trait OwnerFieldSupport { this:DomainTypeSupport with DomainTemplateFieldSupport =>

  class Owner[O <: DomainObject] {
    def createField[T](name:String): Field[O, T] = new Field[O, T](name)

    def createArrayField[T](name:String): ArrayField[O, T] = ArrayField[O, T](name)

    def embeddedField[T <: NestedObject](name:String): EmbeddedField[O, T] = EmbeddedField[O, T](name)

    def embeddedArrayField[T <: NestedObject](name:String): EmbeddedArrayField[O, T] = EmbeddedArrayField[O, T](name)

    def createNestedField[T](parentField:FieldType[O, _ <: NestedObject], name:String): NestedField[O, T] =
      NestedField[O, T](parentField, name)

    def createNestedArrayField[T](parentField:FieldType[O, _ <: NestedObject], name:String): NestedArrayField[O, T] =
      NestedArrayField[O, T](parentField, name)

    def createNestedEmbeddedField[T <: NestedObject](parentField:FieldType[O, _ <: NestedObject], name:String): NestedEmbeddedField[O, T] =
      NestedEmbeddedField[O, T](parentField, name)

    def createNestedEmbeddedArrayField[T <: NestedObject](parentField:FieldType[O, _ <: NestedObject], name:String):
      NestedEmbeddedArrayField[O, T] = NestedEmbeddedArrayField[O, T](parentField, name)
  }
}