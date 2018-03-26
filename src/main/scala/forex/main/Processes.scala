package forex.main

import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend

import forex.{ services => s }
import forex.{ processes => p }
import org.zalando.grafter.macros._

import akka.actor.{ ActorRef, ActorSystem, Props }
import forex.adapter.http.HttpClient
import forex.config.{ ApplicationConfig, OneforgeConfig }
import forex.services.oneforge.{ ApiKeyManager, store }
import forex.services.oneforge.api.OneforgeApi
import monix.cats._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

@readerOf[ApplicationConfig]
case class Processes(
    oneforgeConfig: OneforgeConfig
) {

  implicit final lazy val sttpBackend: SttpBackend[Task, Nothing] = AsyncHttpClientMonixBackend()

  implicit final lazy val oneForge: s.OneForge[AppEffect] =
    oneforgeConfig.interpreter match {
      case "dummy" => s.OneForge.dummy[AppStack]
      case "live"  =>
        val system = ActorSystem("system")
        implicit val apiKeyManager: ActorRef = system.actorOf(Props(classOf[ApiKeyManager], oneforgeConfig), name = "apiKeyManager")
        lazy val httpClient = new HttpClient[Task]
        lazy val api = new OneforgeApi(oneforgeConfig, httpClient)

        // warmUp
        oneforgeConfig.apiKeys.map { apiKey ⇒
          for {
            quotaE ← api.quota(apiKey)
          } yield {
            quotaE.map { quota ⇒
              apiKeyManager ! store(Map(apiKey → quota))
            }
          }
        }.map(_.runSyncMaybe)

        s.OneForge.live[AppStack](api, apiKeyManager)
      case _       => throw new RuntimeException(s"no such interpreter[${oneforgeConfig.interpreter}]")
    }

  final val Rates = p.Rates[AppEffect]

}