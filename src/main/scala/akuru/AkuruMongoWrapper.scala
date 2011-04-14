/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCursor

trait AkuruMongoWrapper {

  def afind[T <: DomainObject : CollectionName : DBName : MongoToDomain, R](f: => MongoObject)(c: MongoCursor => MongoCursor)
     (g: Seq[T] => WorkResult[R])(h: => WorkResult[R]): WorkUnit[T, R] =  cp => {
      cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).aFind[T](f)(c).fold(l => Left(l), r => if (r.isEmpty) h else g(r))
  }

  def aSave[T <: DomainObject : DomainToMongo : CollectionName : DBName, R](f: => T)(g: MongoWriteResult => WorkResult[R])
    (h: MongoWriteResult => WorkResult[R]): WorkUnit[T, R] = cp => {
    cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).aSave[T](f).fold(l => Left(l), wr =>
      if (wr.ok && wr.getN == Some(1)) g(wr) else h(wr))
  }

}