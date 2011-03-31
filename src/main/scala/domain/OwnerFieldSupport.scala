/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

trait OwnerFieldSupport { this:DomainTemplateFieldSupport =>

  class Owner[O <: DomainObject] {
    def field[T : ToMongo](name:String): Field[O, T] = new Field[O, T](name)

    def arrayField[T](name:String)(implicit tm:ToMongo[Seq[T]]): ArrayField[O, T] = ArrayField[O, T](name)

    def embeddedField[T <: NestedObject](name:String): EmbeddedField[O, T] = EmbeddedField[O, T](name)

    def embeddedArrayField[T <: NestedObject](name:String): EmbeddedArrayField[O, T] = EmbeddedArrayField[O, T](name)

    def nestedField[T : ToMongo](parentField:FieldType[O, _ <: NestedObject], name:String): NestedField[O, T] =
      NestedField[O, T](parentField, name)

    def nestedArrayField[T](parentField:FieldType[O, _ <: NestedObject], name:String)(implicit tm:ToMongo[Seq[T]]): NestedArrayField[O, T] =
      NestedArrayField[O, T](parentField, name)

    def nestedEmbeddedField[T <: NestedObject](parentField:FieldType[O, _ <: NestedObject], name:String): NestedEmbeddedField[O, T] =
      NestedEmbeddedField[O, T](parentField, name)

    def nestedEmbeddedArrayField[T <: NestedObject](parentField:FieldType[O, _ <: NestedObject], name:String):
      NestedEmbeddedArrayField[O, T] = NestedEmbeddedArrayField[O, T](parentField, name)
  }
}