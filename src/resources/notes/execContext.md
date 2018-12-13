# ExecutionContext

Cf https://docs.scala-lang.org/overviews/core/futures.html#blocking-inside-a-future

implicit val ec = ExecutionContext.global
 or
 import scala.concurrent.ExecutionContext.Implicits.global
 -> ForkJoinPool with the number of threads of your processor.
 
Cf https://docs.oracle.com/javase/tutorial/essential/concurrency/pools.html
ForkJoinPool is good for mainly non-blocking code (https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html)
If mostly blocking, newFixedThreadPool is a good choice.
newCachedThreadPool is somewhat in between
Use Future{blocking{}} to generate more threads -> But it does not work for all EC (such as newFixedThreadPool for example).
import scala.concurrent.blocking
blocking {} tells the thread manager that this thread can be stolen by another needy thread since it is likely to just be waiting on some I/O anyway.

Note: 1 thread = 1MB RAM

val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

If you just need to change the thread pool count, just use the global executor and pass the following system properties.

-Dscala.concurrent.context.numThreads=8 -Dscala.concurrent.context.maxThreads=8

In build.sbt: (https://stackoverflow.com/a/47172931/4965515)
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
