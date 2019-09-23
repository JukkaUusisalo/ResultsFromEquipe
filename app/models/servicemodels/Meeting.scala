package models.servicemodels

import org.joda.time.DateTime

case class Meeting(
  id:Int,
  displayName:String,
  startDate:DateTime,
  endDate:DateTime,
  discipline:String)
