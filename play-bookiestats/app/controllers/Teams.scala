package controllers

import javax.inject.Inject

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{ Action, BodyParsers, Call, Controller, Result }

import reactivemongo.bson.{ BSONObjectID, BSONDocument }
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import reactivemongo.api.commands.WriteResult

import play.modules.reactivemongo.{
  MongoController, ReactiveMongoApi, ReactiveMongoComponents
}

/**
  * Created by allenkopic on 9/3/16.
  */

class Teams @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents{

  import controllers.TeamFields._

  def teamRepo = new backend.TeamMongoRepo(reactiveMongoApi)

  def list = Action.async {implicit request =>
    teamRepo.find()
      .map(teams => Ok(Json.toJson(teams.reverse)))
      .recover {case PrimaryUnavailableException => InternalServerError("Please install MongoDB")}
  }

  def update(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    val value = (request.body \ Name).as[String]
    teamRepo.update(BSONDocument(Id -> BSONObjectID(id)), BSONDocument("$set" -> BSONDocument(Name -> value)))
      .map(le => Ok(Json.obj("success" -> le.ok)))
  }

  def delete(id: String) = Action.async {
    teamRepo.remove(BSONDocument(Id -> BSONObjectID(id)))
      .map(le => RedirectAfterPost(le, routes.Teams.list()))
  }

  private def RedirectAfterPost(result: WriteResult, call: Call): Result =
    if (result.inError) InternalServerError(result.toString)
    else Redirect(call)

  def add = Action.async(BodyParsers.parse.json) { implicit request =>
    val name = (request.body \ Name).as[String]
    val oppName = (request.body \ OppName).as[String]
    val location = (request.body \ Location).as[String]
    val goalsScored = (request.body \ GoalsScored).as[String]
    val goalsReceived = (request.body \ GoalsReceived).as[String]
    val rating = (request.body \ Rating).as[String]
    val totalShots = (request.body \ TotalShots).as[String]
    val shotsOnTarget = (request.body \ ShotsOnTarget).as[String]
    val shotsOffTarget = (request.body \ ShotsOffTarget).as[String]
    val possession = (request.body \ Possession).as[String]
    val totalPasses = (request.body \ TotalPasses).as[String]
    val accuratePasses = (request.body \ AccuratePasses).as[String]
    val wrongPasses = (request.body \ WrongPasses).as[String]
    val tackles = (request.body \ Tackles).as[String]
    val corners = (request.body \ Corners).as[String]
    val offsides = (request.body \ Offsides).as[String]
    val fouls = (request.body \ Fouls).as[String]


    teamRepo.save(BSONDocument(
      Name -> name,
      OppName -> oppName,
      Location -> location,
      GoalsScored -> goalsScored,
      GoalsReceived -> goalsReceived,
      Rating -> rating,
      TotalShots -> totalShots,
      ShotsOnTarget -> shotsOnTarget,
      ShotsOffTarget -> shotsOffTarget,
      Possession -> possession,
      TotalPasses -> totalPasses,
      AccuratePasses -> accuratePasses,
      WrongPasses -> wrongPasses,
      Tackles -> tackles,
      Corners -> corners,
      Offsides -> offsides,
      Fouls -> fouls
    )).map(le => Redirect(routes.Teams.list()))
  }

}

object TeamFields {
  val Id = "_id"
  val Name = "name"
  val OppName = "opp_name"
  val Location = "location"
  val GoalsScored = "goals_scored"
  val GoalsReceived = "goals_received"
  val Rating = "rating"
  val TotalShots = "total_shots"
  val ShotsOnTarget = "shots_on_target"
  val ShotsOffTarget = "shots_off_target"
  val Possession = "possession_percent"
  val TotalPasses = "total_passes"
  val AccuratePasses = "accurate_passes"
  val WrongPasses = "wrong_passes"
  val Tackles = "tackles"
  val Corners = "corners"
  val Offsides = "offsides"
  val Fouls = "fouls"
}