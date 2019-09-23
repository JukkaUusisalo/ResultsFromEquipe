package models.servicemodels

import org.joda.time.DateTime

case class MeetingClass (
  id:Int,
  name:String,
  discipline:String,
  startTime:DateTime,
  sectionId:Int)
