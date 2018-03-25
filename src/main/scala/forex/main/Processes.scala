package forex.main

import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend

import forex.{ services => s }
import forex.{ processes => p }
import org.zalando.grafter.macros._

import forex.adapter.http.HttpClient
import forex.config.{ ApplicationConfig, OneforgeConfig }
import forex.services.oneforge.api.OneforgeApi
import monix.cats._
import monix.eval.Task

@readerOf[ApplicationConfig]
case class Processes(
    oneforgeConfig: OneforgeConfig
) {

  implicit final lazy val sttpBackend: SttpBackend[Task, Nothing] = AsyncHttpClientMonixBackend()

  implicit final lazy val oneForge: s.OneForge[AppEffect] =
    oneforgeConfig.interpreter match {
      case "dummy" => s.OneForge.dummy[AppStack]
      case "live"  =>
        lazy val httpClient = new HttpClient[Task]
        lazy val api = new OneforgeApi(oneforgeConfig, httpClient)
        s.OneForge.live[AppStack](api)
      case _       => throw new RuntimeException(s"no such interpreter[${oneforgeConfig.interpreter}]")
    }

  final val Rates = p.Rates[AppEffect]

}