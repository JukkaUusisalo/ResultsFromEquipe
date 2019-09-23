package service

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.servicemodels.{Meeting, MeetingClass}
import models.{Meeting, MeetingClass}
import play.api.libs.ws._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}



@ImplementedBy(classOf[DefaultEquipeClient])
trait EquipeClient {
  def getRecentMeetings():Future[List[Meeting]]
  def getMeetingClasses(meetingId:Int):Future[List[MeetingClass]]
}


class DefaultEquipeClient @Inject() (ws:WSClient, ec: ExecutionContext) extends EquipeClient {

  implicit val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern("yyyy-MM-dd"))
    )
  )

  implicit val meetingReads: Reads[Meeting] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "display_name").read[String] and
    (JsPath \ "start_on").read[DateTime] and
    (JsPath \ "end_on").read[DateTime] and
    (JsPath \ "discipline").read[String]
    )(Meeting.apply _)

  implicit val responseReads: Reads[Seq[Meeting]] = Reads.seq(meetingReads)

  implicit val meetingClassReads: Reads[MeetingClass] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "name").read[String] and
    (JsPath \ "discipline").read[String] and
    (JsPath \ "start_at").read[DateTime] and
    (JsPath \ "class_sections" \ 0 \ "section_id" ).read[Int])(MeetingClass.apply _)

  implicit val executionContext = ec

  override def getRecentMeetings() : Future[List[Meeting]] = {
    ws.url("https://online.equipe.com/api/v1/meetings/recent").get().map { response =>
      val json = response.json
      json.as[Seq[Meeting]].toList
    }
  }

  override def getMeetingClasses(meetingId: Int): Future[List[MeetingClass]] = {
    val url = s"https://online.equipe.com/api/v1/meetings/$meetingId/schedule"
    ws.url(url).get().map {response =>
      val json = response.json
      val daysJson = json.\("days").as[JsArray]
      val meetingClassJson = (daysJson \\  "meeting_classes").map(meetingClassJson => meetingClassJson)
      meetingClassJson.map(m => m.as[MeetingClass]).filter(x => !x.discipline.equals("list")).toList
    }
  }
}