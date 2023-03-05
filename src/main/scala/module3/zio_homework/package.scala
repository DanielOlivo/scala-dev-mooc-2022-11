package module3

import zio.{Has, Task, ULayer, ZIO, ZLayer, Layer}
import zio.{URIO,UIO}
import zio.clock.{Clock, sleep}
import zio.console._
import zio.console.{Console}
import zio.duration.durationInt
import zio.macros.accessible
import zio.random._

import java.io.IOException
import java.util.concurrent.TimeUnit
import scala.io.StdIn
import scala.language.postfixOps
import org.scalactic.Bool

package object zio_homework {
  /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в когнсоль угадал или нет. Подумайте, на какие наиболее простые эффекты ее можно декомпозировать.
   */



  lazy val guessProgram = 
    for {
      rnd <- nextIntBetween(1,4).map(_.toString())
      _ <- putStrLn("your guess?")
      usersGuess <- getStrLn
      _ <- putStrLn(if (usersGuess == rnd) "you lucky!" else s"phhhhhh, it was $rnd")
    } yield()

  /**
   * 2. реализовать функцию doWhile (общего назначения), которая будет выполнять эффект до тех пор, пока его значение в условии не даст true
   * 
   */

  def getCondition: Task[Boolean] = ???
  def doWhile : Task[Unit]= 
    getCondition.flatMap(b => if (!b) ZIO.effect(println("that's all")) else doWhile)
  /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из файла, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "load" из пакета config
   */


  def loadConfigOrDefault = {
    import config._
    for{
      config <- config.load.orElse(ZIO.succeed(AppConfig("default","default")))
    } yield config
  }


  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   * 4.1 Создайте эффект, который будет возвращать случайеым образом выбранное число от 0 до 10 спустя 1 секунду
   * Используйте сервис zio Random
   */
  lazy val eff = 
    for{
      fiber <- sleep(1 second).fork
      _ <- fiber.join
      rnd <- nextIntBetween(0,11)
    } yield rnd

  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  lazy val effects = (0 to 9).map(_ => eff)

  
  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекци "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */

  lazy val app : URIO[Clock with Random, Int]= 
    zioConcurrency.printEffectRunningTime(ZIO.collectAll(effects).map(_.toList.sum))


  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */

  lazy val appSpeedUp : URIO[Clock with Random, Int] = 
    zioConcurrency.printEffectRunningTime(ZIO.collectAllPar(effects).map(_.toList.sum))


  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * молжно было использовать аналогично zio.console.putStrLn например
   */

  type RunningTimeLogService = Has[RunningTimeLogService.Service]

  object RunningTimeLogService {

    trait Service{
      def printEffectRunningTime(zio: URIO[Clock with Random,Int]): URIO[Clock with Random,Int]
      //def printEffectRunningTime(zio: UIO[Int]): URIO[Clock, Int]
    }

    //val any : ZLayer[RunningTimeLogService, Nothing, RunningTimeLogService] = 
    //  ZLayer.requires[RunningTimeLogService]

    val live /*: ZLayer[Clock with Random, Nothing, RunningTimeLogService] */= ZLayer.succeed{
      new Service {
        override def printEffectRunningTime(zio: URIO[Clock with Random, Int]): URIO[Clock with Random, Int] = 
          zioConcurrency.printEffectRunningTime(zio)
      }
    }

    def printEffectRunningTime(zio: UIO[Int]) = 
      ZIO.accessM( (x: RunningTimeLogService) => x.get.printEffectRunningTime(zio))

  }

  lazy val runningEffectService = Clock.live ++ Random.live ++ RunningTimeLogService.live

   /**
     * 6.
     * Воспользуйтесь написанным сервисом, чтобы созадть эффект, который будет логировать время выполнения прогаммы из пункта 4.3
     *
     * 
     */

  //lazy val appWithTimeLogg = ZLayer.fromEffect(app) >>> runningEffectService
  val appWithTimeLogg: ZIO[Clock with Random with RunningTimeLogService, Nothing, Unit] = 
    for {
     service <- ZIO.environment[RunningTimeLogService].map(_.get)
     _ <- service.printEffectRunningTime(app)
    } yield ()

  /**
    * 
    * Подготовьте его к запуску и затем запустите воспользовавшись ZioHomeWorkApp
    */
  lazy val runApp = appWithTimeLogg.provideLayer(runningEffectService) 

}
