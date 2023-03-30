package module3.cats_effect_homework

import cats.effect.{IO, IOApp}
import cats.implicits._
import scala.concurrent.duration._

// Поиграемся с кошельками на файлах и файберами.

// Нужно написать программу где инициализируются три разных кошелька и для каждого из них работает фоновый процесс,
// который регулярно пополняет кошелек на 100 рублей раз в определенный промежуток времени. Промежуток надо сделать разный, чтобы легче было наблюдать разницу.
// Для определенности: первый кошелек пополняем раз в 100ms, второй каждые 500ms и третий каждые 2000ms.
// Помимо этих трёх фоновых процессов (подсказка - это файберы), нужен четвертый, который раз в одну секунду будет выводить балансы всех трех кошельков в консоль.
// Основной процесс программы должен просто ждать ввода пользователя (IO.readline) и завершить программу (включая все фоновые процессы) когда ввод будет получен.
// Итого у нас 5 процессов: 3 фоновых процесса регулярного пополнения кошельков, 1 фоновый процесс регулярного вывода балансов на экран и 1 основной процесс просто ждущий ввода пользователя.

// Можно делать всё на IO, tagless final тут не нужен.

// Подсказка: чтобы сделать бесконечный цикл на IO достаточно сделать рекурсивный вызов через flatMap:
// def loop(): IO[Unit] = IO.println("hello").flatMap(_ => loop())
object WalletFibersApp extends IOApp.Simple {


  def printEveryNthSecond(wallets : List[Wallet[IO]], seconds : Int) : IO[Unit] = 
  for{
    _ <- IO.sleep(seconds.seconds)
    _ <- printBalance(wallets(0))
    _ <- printBalance(wallets(1))
    _ <- printBalance(wallets(2))
    _ <- printEveryNthSecond(wallets, seconds)
  } yield()
  
  def printBalance(wallet : Wallet[IO]) = for{
    balance <- wallet.balance
    _ <- IO(println(balance))
  } yield ()

  def topup(wallet: Wallet[IO], amount: Int, sec : Int) : IO[Unit] = IO.defer{
    IO.sleep(sec.seconds) *> IO(wallet.topup(amount)) *> topup(wallet,amount,sec)
  }

  def run: IO[Unit] =
    for {
      _ <- IO.println("Press any key to stop...")
      wallet1 <- Wallet.fileWallet[IO]("1")
      wallet2 <- Wallet.fileWallet[IO]("2")
      wallet3 <- Wallet.fileWallet[IO]("3")
      // todo: запустить все файберы и ждать ввода от пользователя чтобы завершить работу

      topup1 <- topup(wallet1,100,1).start
      topup2 <- topup(wallet2,100,2).start
      topup3 <- topup(wallet3,100,5).start
      print <- printEveryNthSecond(List(wallet1, wallet2, wallet3), 3).start

      _ <- IO.readLine
      
      _ <- print.cancel
      _ <- topup1.cancel
      _ <- topup2.cancel
      _ <- topup3.cancel

      _ <- print.join
      _ <- topup1.join
      _ <- topup2.join
      _ <- topup3.join

      _ <- IO.println("done")
    } yield ()

}
