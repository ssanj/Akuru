package akuru.domain

/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */


import akuru.Tools

trait DomainSupport extends
          DomainTypeSupport with
          DomainTemplateFieldSupport with
          DomainTemplateSupport with
          OwnerFieldSupport with
          DomainFuncsSupport with
          Tools