package controllers

import backend.TeamMongoRepo
import controllers.TeamFields.{Name, Location, Id}
import org.specs2.matcher.{Expectable, Matcher}
import org.specs2.mock.Mockito
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.LastError
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by allenkopic on 9/3/16.
  */
object TeamsSpec extends org.specs2.mutable.Specification with Results with Mockito {

  val mockTeamRepo = mock[TeamMongoRepo]
  val reactiveMongoRepo = mock[ReactiveMongoApi]

  val FirstTeamId = "5559e224cdecd3b535e8b681"
  val SecondTeamId = "5559e224cdecd3b535e8b682"

  val lazioTeam = Json.obj(
    Id -> FirstTeamId,
    Name -> "SS LAZIO",
    Location -> "Rome"
  )
  val juventusTeam = Json.obj(
    Id -> SecondTeamId,
    Name -> "JUVENTUS FC",
    Location -> "Torino"
  )
  val controller = new TestController()
  val nothingHappenedLastError = new LastError(true, None, None, None, 0, None, false, None, None, false, None, None)

  case class PostBSONDocumentMatcher(expected: BSONDocument) extends Matcher[BSONDocument] {
    override def apply[S <: BSONDocument](t: Expectable[S]) = {
      result(evaluate(t),
        t.description + " is valid",
        t.description + " is not valid",
        t)
    }

    def evaluate[S <: BSONDocument](t: Expectable[S]): Boolean = {
      t.value.get(Name) === expected.get(Name) && t.value.get(Location) === expected.get(Location)
    }
  }

  class TestController() extends Teams(reactiveMongoRepo) {
    override def teamRepo: TeamMongoRepo = mockTeamRepo
  }

  "Teams Page#list" should {
    "list teams" in {
      mockTeamRepo.find()(any[ExecutionContext]) returns Future(List(lazioTeam, juventusTeam))

      val result: Future[Result] = controller.list().apply(FakeRequest())

      contentAsJson(result) must be equalTo JsArray(List(lazioTeam, juventusTeam))
    }
  }

//  "Posts Page#delete" should {
//    "remove post" in {
//      mockPostRepo.remove(any[BSONDocument])(any[ExecutionContext]) returns Future(nothingHappenedLastError)
//
//      val result: Future[Result] = controller.delete(FirstPostId).apply(FakeRequest())
//
//      status(result) must be equalTo SEE_OTHER
//      redirectLocation(result) must beSome(routes.Posts.list().url)
//      there was one(mockPostRepo).remove(any[BSONDocument])(any[ExecutionContext])
//    }
//  }


  "Teams Page#add" should {
    "create team" in {
      mockTeamRepo.save(any[BSONDocument])(any[ExecutionContext]) returns Future(nothingHappenedLastError)
      val team = Json.obj(
        Name -> "FK STUPCANICA",
        Location -> "Olovo"
      )

      val request = FakeRequest().withBody(team)
      val result: Future[Result] = controller.add()(request)

      status(result) must be equalTo SEE_OTHER
      redirectLocation(result) must beSome(routes.Teams.list().url)

      val document = BSONDocument(Name -> "FK STUPCANICA", Location -> "Olovo")
      there was one(mockTeamRepo).save(argThat(PostBSONDocumentMatcher(document)))(any[ExecutionContext])
    }
  }

//  "Posts Page#like" should {
//    "like post" in {
//      mockPostRepo.update(any[BSONDocument], any[BSONDocument])(any[ExecutionContext]) returns Future(nothingHappenedLastError)
//
//      val request = FakeRequest().withBody(Json.obj("favorite" -> true))
//      val result: Future[Result] = controller.like(SecondPostId)(request)
//
//      status(result) must be equalTo OK
//      contentAsJson(result) must be equalTo Json.obj("success" -> true)
//      there was one(mockPostRepo).update(any[BSONDocument], any[BSONDocument])(any[ExecutionContext])
//    }
//  }

}
