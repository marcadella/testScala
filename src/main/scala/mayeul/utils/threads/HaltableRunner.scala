package mayeul.utils.threads

/**
  * Spawns a thread which can be halted.
  * logic() should throw an InterruptedException when the thread is interrupted. This can be done as follows:
  * if (Thread.interrupted())
  *   throw new InterruptedException
  * (not needed if Thread.sleep() is used as it does just that)
  * Reminder: calling Thread.interrupt() only sets the flag Thread.interrupted(). If you don't make the code exit the thread will not actually terminate!
  * In the cleanUp() method, `if (Thread.interrupted())` can be used to differentiate between a normal termination or a halt.
  * More info: https://www.yegor256.com/2015/10/20/interrupted-exception.html
  */
abstract class HaltableRunner(autoStart: Boolean = true) {
  protected def logic(): Unit
  protected def cleanUp(): Unit = ()
  protected val prefix: String = ""

  final protected val thread = new Thread(new Runnable {
    def run(): Unit = {
      try {
        logic()
      } catch {
        case e: InterruptedException =>
          Thread
            .currentThread()
            .interrupt() //We set the flag again since Thread.interrupted() resets the flag
      } finally {
        cleanUp()
      }
    }
  })

  if (prefix != "")
    thread.setName(s"$prefix-${thread.getName}")

  final def start(): Unit = thread.start()
  final def halt(): Unit = thread.interrupt()

  if (autoStart)
    start()
}
