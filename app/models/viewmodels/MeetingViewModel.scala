package models.viewmodels

import models.servicemodels.Meeting

case class MeetingViewModel(
  meeting:Meeting,
  meetingClasses:List[MeetingClassViewModel]
)
