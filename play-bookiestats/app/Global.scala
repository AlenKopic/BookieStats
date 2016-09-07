import javax.inject.Inject

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.{ Logger, Application, GlobalSettings }

import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection

class Global @Inject() (
  val reactiveMongoApi: ReactiveMongoApi) extends GlobalSettings {

  def collection = reactiveMongoApi.db.collection[JSONCollection]("teams")

  val teams = List(
    Json.obj(
      "name" -> "SS LAZIO",
      "location" -> "Rome"
    ),
    Json.obj(
      "name" -> "JUVENTUS FC",
      "location" -> "Torino"
    )
  )

  override def onStart(app: Application) {
    Logger.info("Application has started")

    collection.bulkInsert(teams.toStream, ordered = true).
      foreach(i => Logger.info("Database was initialized"))
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")

    collection.drop().onComplete {
      case _ => Logger.info("Database collection dropped")
    }
  }
}
