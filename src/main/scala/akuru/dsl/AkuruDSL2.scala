/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package dsl

//TODO: Rename this to AkuruDSL once updates are complete.
trait AkuruDSL2 extends AkuruDrop with AkuruFinder with AkuruSave { this:AkuruMongoWrapper with DSLTools => }