package futures

import HomeworksUtils.TaskSyntax

import scala.concurrent.{ExecutionContext, Future}

object task_futures_sequence {

  /**
   * В данном задании Вам предлагается реализовать функцию fullSequence,
   * похожую на Future.sequence, но в отличии от нее,
   * возвращающую все успешные и не успешные результаты.
   * Возвращаемое тип функции - кортеж из двух списков,
   * в левом хранятся результаты успешных выполнений,
   * в правово результаты неуспешных выполнений.
   * Не допускается использование методов объекта Await и мутабельных переменных var
   */
  /**
   * @param futures список асинхронных задач
   * @return асинхронную задачу с кортежом из двух списков
   */
  def fullSequence[A](futures: List[Future[A]])
                     (implicit ex: ExecutionContext): Future[(List[A], List[Throwable])] = {
                      def folder[A] (acc: Future[(List[A],List[Throwable])], next: Future[A]) : Future[(List[A],List[Throwable])] = 
                        acc.flatMap(c => next.flatMap(r => Future.successful(c._1 :+ r, c._2)).recover(exception => (c._1, c._2 :+ exception)))
                      futures.foldLeft(Future.successful((List[A](),List[Throwable]()))) (folder)
                     }
    //task"Реализуйте метод `fullSequence`"()
}
