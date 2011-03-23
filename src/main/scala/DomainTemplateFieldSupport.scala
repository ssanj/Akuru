/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru.domain

trait DomainTemplateFieldSupport { this:DomainTypeSupport =>

  sealed abstract class FieldType[O <: DomainObject, T] {
    val name:String

    val path:String

    def apply(value:T): Value = Value(value)

    def === (value:T) : Value = apply(value)

    final case class Value(value:T) {
      val name = field.name
      val path = field.path
      lazy val field = FieldType.this  //we need this lazy as it should be accessed only after Field instantiation.
    }
  }

  /**
   * A DomainTemplateField is any field that is within a DomainTemplate.
   */
  sealed abstract class DomainTemplateField[O <: DomainObject, T] extends FieldType[O, T] {
    override val path:String = name
  }

  //tood: Rename to PrimitiveField
  final case class Field[O <: DomainObject, T](override val name:String) extends DomainTemplateField[O, T]

  final case class EmbeddedField[O <: DomainObject, T <: NestedObject](override val name:String) extends DomainTemplateField[O, T]

  final case class ArrayField[O <: DomainObject, T](override val name:String) extends DomainTemplateField[O, Seq[T]]

  final case class EmbeddedArrayField[O <: DomainObject, T <: NestedObject](override val name:String) extends DomainTemplateField[O, Seq[T]]

  sealed abstract class NestedTemplateField[O <: DomainObject, T] extends FieldType[O, T] {
    val parentField:FieldType[O, _ <: NestedObject]

    private object Constants {
      val pathSeparator = "."
    }

    import Constants._
    override val path = findPath(parentField) + pathSeparator + name

    private def findPath(ft:FieldType[O, _]): String =
      ft match {
        case f:DomainTemplateField[_, _] => f.name
        case n:NestedTemplateField[_, _] => findPath(n.parentField)  + pathSeparator + n.name
      }
  }

  final case class NestedField[O <: DomainObject, T](override val parentField:FieldType[O, _ <: NestedObject], override val name:String)
          extends NestedTemplateField[O, T]

  final case class NestedEmbeddedField[O <: DomainObject, T <: NestedObject](
        override val parentField:FieldType[O, _ <: NestedObject], override val name:String) extends NestedTemplateField[O, T]

  final case class NestedArrayField[O <: DomainObject, T](override val parentField:FieldType[O, _ <: NestedObject], override val name:String)
          extends NestedTemplateField[O, Seq[T]]

  final case class NestedEmbeddedArrayField[O <: DomainObject, T <: NestedObject](
    override val parentField:FieldType[O, _ <: NestedObject], override val name:String) extends NestedTemplateField[O, Seq[T]]

}