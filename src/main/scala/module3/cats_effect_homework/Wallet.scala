package module3.cats_effect_homework

import cats.effect.Sync
import cats.implicits._
import Wallet._
import java.nio.file._

// DSL управления электронным кошельком
trait Wallet[F[_]] {
  // возвращает текущий баланс
  def balance: F[BigDecimal]
  // пополняет баланс на указанную сумму
  def topup(amount: BigDecimal): F[Unit]
  // списывает указанную сумму с баланса (ошибка если средств недостаточно)
  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]]
}

// Игрушечный кошелек который сохраняет свой баланс в файл
// todo: реализовать используя java.nio.file._
// Насчёт безопасного конкуррентного доступа и производительности не заморачиваемся, делаем максимально простую рабочую имплементацию. (Подсказка - можно читать и сохранять файл на каждую операцию).
// Важно аккуратно и правильно завернуть в IO все возможные побочные эффекты.
//
// функции которые пригодятся:
// - java.nio.file.Files.write
// - java.nio.file.Files.readString
// - java.nio.file.Files.exists
// - java.nio.file.Paths.get
final class FileWallet[F[_]: Sync](id: WalletId) extends Wallet[F] {
	def delay[A](a : A) = Sync[F].delay(a)

	def path = for{
		pathString <- this.delay(s"${System.getProperty("user.dir")}\\src\\main\\scala\\module3\\cats_effect_homework/$id.txt")
		path <- delay(Paths.get(pathString))
	} yield path

	def pathExists(path: Path) = delay(Files.exists(path))
	 
	def getLine(path : Path) = 
		for{
			lines <- delay(Files.readAllLines(path))
			firstLine <- delay(lines.get(0))
		} yield firstLine
	def toDecimal (str : String) = delay(BigDecimal(str))
	
	def overwrite (path: Path, amount: BigDecimal) = delay(Files.write(path, amount.toString().getBytes()))

	def balance : F[BigDecimal] = 
		for{
			pathString <- Sync[F].delay(s"${System.getProperty("user.dir")}\\src\\main\\scala\\module3\\cats_effect_homework/$id.txt")
			path <- Sync[F].delay(Paths.get(pathString))
			exists <- Sync[F].delay(Files.exists(path))
			s <- 
				if (!exists) Sync[F].delay("0")
				else getLine(path)
			b <- Sync[F].delay(BigDecimal(s))
		} yield b
	
	def topup(amount: BigDecimal): F[Unit] = 
		for{
			path <- this.path
			exists <- pathExists(path)
			content <- getLine(path)
			current <- toDecimal(content)
			updated <- delay(current + amount)
			_ <- overwrite(path,updated)
		} yield ()

	def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]] = 
		for{
			path <- this.path
			exists <- pathExists(path)
			content <- getLine(path)
			current <- toDecimal(content)
			updated <- 
				if(current > amount) delay(current - amount)			
				else delay(current)
			result <- 
				if(updated < current) {
					delay(overwrite(path, updated)) *> delay(Right(println("Balance was updated")))
				} else 
					delay(Left(BalanceTooLow))
		} yield result
}

object Wallet {

  // todo: реализовать конструктор
  // внимание на сигнатуру результата - инициализация кошелька имеет сайд-эффекты
  // Здесь нужно использовать обобщенную версию уже пройденного вами метода IO.delay,
  // вызывается она так: Sync[F].delay(...)
  // Тайпкласс Sync из cats-effect описывает возможность заворачивания сайд-эффектов
	def fileWallet[F[_]: Sync](id: WalletId): F[Wallet[F]] = Sync[F].delay(new FileWallet(id))

  type WalletId = String

  sealed trait WalletError
  case object BalanceTooLow extends WalletError
}
