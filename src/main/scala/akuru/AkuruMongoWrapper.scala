/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCursor

trait AkuruMongoWrapper {

  def afind[T <: DomainObject : CollectionName : DBName : MongoToDomain, R](f: => MongoObject)(c: MongoCursor => MongoCursor)
     (success: Seq[T] => WorkResult[R])(failure: => WorkResult[R]): WorkUnit[T, R] =  cp => {
      cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).aFind[T](f)(c).fold(l => Left(l), r =>
        if (r.isEmpty) failure else success(r))
  }

  def aSave[T <: DomainObject : DomainToMongo : CollectionName : DBName, R](f: => T)(success:  => WorkResult[R])
    (failure: MongoWriteResult => WorkResult[R]): WorkUnit[T, R] = cp => {
    cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).aSave[T](f).fold(l => Left(l), wr => if (wr.ok) success else failure(wr))
  }

  def aDrop[T <: DomainObject : CollectionName : DBName, R](success: => WorkResult[R]): WorkUnit[T, R] = cp =>
    cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).aDrop.fold(l => Left(l), r => success)
}