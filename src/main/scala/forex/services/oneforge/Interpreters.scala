package forex.services.oneforge

import forex.domain._
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import forex.services.oneforge.api.OneforgeApi

object Interpreters {

  def dummy[R](
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Dummy[R]

  def live[R](
      oneforgeApi: OneforgeApi[Task]
  )(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Live[R](oneforgeApi)
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
    api: OneforgeApi[Task]
)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]]
    with ErrorAccumulatingCirceSupport {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] =
    for {
      quote ← fromTask(api.quote(pair))
    } yield quote.map(q ⇒ Rate(pair, Price(value = q.price), Timestamp.now))
}
