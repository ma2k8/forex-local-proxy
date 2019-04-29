package forex.services.oneforge

import akka.actor._
import cats.Monad
import monix.cats._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import forex.config.OneforgeConfig
import forex.domain.oneforge.Quota


case class store(apiKeyMap: Map[String, Quota])

class ApiKeyManager(oneforgeConfig: OneforgeConfig)(
) extends Actor with ErrorAccumulatingCirceSupport {

  var apiKeyMaps: Map[String, Quota] = Map.empty

  override def receive: Receive = {
    case "warmUp"           ⇒ println("hello Akka!!!!!!!")
    case "findEffectiveKey" ⇒ sender ! findKey
    case store(apiKeyMap) => store(apiKeyMap)
  }

  private def store(apiKeyMap: Map[String, Quota]) =
    apiKeyMaps = apiKeyMaps ++ apiKeyMap

  private def findKey =
    apiKeyMaps
      .find(_._2.quota_remaining != 0)
      .map(_._1)
}

object ApiKeyManager {

  val Commands = Seq(
    "warmUp",
    "findEffectiveKey"
  )

}
