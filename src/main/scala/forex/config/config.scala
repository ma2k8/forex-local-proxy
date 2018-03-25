package forex.config

import org.zalando.grafter.macros._

import scala.concurrent.duration.{ Duration, FiniteDuration }

@readers
case class ApplicationConfig(
    akka: AkkaConfig,
    api: ApiConfig,
    executors: ExecutorsConfig,
    oneforge: OneforgeConfig
)

case class AkkaConfig(
    name: String,
    exitJvmTimeout: Option[FiniteDuration]
)

case class ApiConfig(
    interface: String,
    port: Int
)

case class ExecutorsConfig(
    default: String
)

case class OneforgeConfig(
    interpreter: String,
    baseUri: String,
    cacheTtl: Duration,
    apiKeys: Seq[String]
)
