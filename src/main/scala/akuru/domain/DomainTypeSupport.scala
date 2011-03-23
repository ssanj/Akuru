package akuru.domain

/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
import akuru.MongoObjectId
import akuru.MongoObject

trait DomainTypeSupport {

  type MID = Option[MongoObjectId]

  type MongoToDomain[T <: DomainObject] = MongoObject => Option[T]

  type DomainToMongo[T <: DomainObject] = T => MongoObject

  type NestedToMongo[T <: NestedObject] = T => MongoObject

  type MongoToNested[T <: NestedObject] = MongoObject => Option[T]

  sealed trait AkuruObject

  abstract class DomainObject extends AkuruObject

  abstract class NestedObject extends AkuruObject

  trait CollectionName[T <: DomainObject] {
    val name:String
  }

}