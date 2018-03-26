package forex.services.oneforge

import scala.concurrent.duration._
import scala.language.postfixOps
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import cats.Monad
import monix.cats._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import forex.domain.oneforge.{ Quota, Quote }
import forex.services.oneforge.api.OneforgeApi

import cats.syntax.either._
import forex.domain._
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._
import forex.domain.oneforge._

object Interpreters {

  def dummy[R](
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Dummy[R]

  def live[R](
      oneforgeApi: OneforgeApi[Task],
      apiKeyManager: ActorRef
  )(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Live[R](oneforgeApi, apiKeyManager)
}

final class Dummy[R] private[oneforge] (
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] =
    for {
      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield Right(result)
}

final class Live[R] private[oneforge] (
    api: OneforgeApi[Task],
    apiKeyManager: ActorRef
)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]]
    with ErrorAccumulatingCirceSupport {

  implicit val timeout = Timeout(100 milliseconds)

  var apiKeyMaps: Map[String, Quota] = Map.empty

  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] =
    for {
      apiKeyE ← fromTask {
        Task
          .fromFuture(
            (apiKeyManager ? "findEffectiveKey").mapTo[Option[String]]
          )
          .map { apiKeyOpt ⇒
            Either.fromOption(apiKeyOpt, Error.Api("effective api key not found."))
          }
      }
      quoteE ← fromTask {
        apiKeyE match {
          case Right(apiKey) ⇒ api.quote(apiKey, pair)
          case Left(e) ⇒
            val i = Monad[Task].pure(Left(e))
            i
        }
      }
      newQuotaE ← fromTask {
        apiKeyE match {
          case Right(apiKey) ⇒ api.quota(apiKey)
          case Left(e)       ⇒ Monad[Task].pure(Left(e))
        }
      }
    } yield {
      (apiKeyE, newQuotaE) match {
        case (Right(apiKey), Right(newQuota)) ⇒ apiKeyManager ! store(Map(apiKey → newQuota))
        case _                                ⇒ ()
      }
      quoteE.map(q ⇒ Rate(pair, Price(value = q.price), Timestamp.now))
    }

}
