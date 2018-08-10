implicit val ec = ExecutionContext.global
 or
 import scala.concurrent.ExecutionContext.Implicits.global
 -> ForkJoinPool with the number of threads of your processor.
ForkJoinPool is good for mainly non-blocking code (otherwise too many threads might be generated)
If mostly blocking, newFixedThreadPool is a good choice.
newCachedThreadPool is somewhat in between
Use Future{blocking{}} to generate more threads -> But it only works for the global execution context (but does not harm and could be useful for documentation purpose)
import scala.concurrent.blocking

Note: 1 thread = 1MB RAM

val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

If you just need to change the thread pool count, just use the global executor and pass the following system properties.

-Dscala.concurrent.context.numThreads=8 -Dscala.concurrent.context.maxThreads=8

In build.sbt:
fork in run := true

javaOptions += "-Dscala.concurrent.context.maxThreads=1"
 

implicit val executor =
    ExecutionContext.fromExecutor(new Executor {
      def execute(command: Runnable): Unit = {
        val thread = new Thread {
          override def run(): Unit = {
            command.run()
          }
        }
        thread.start()
      }
    })
