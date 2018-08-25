# Error vs Exceptions vs NonFatal

Java _Error_ and _Exceptions_ are a mess. In addition, some scala xxxError are actually Exceptions under the hood (ex: MatchError).

So it is better to use the concept of _NonFatal_ defined in Scala -> Every Error/Exceptions except:
  - all under VirtualMachineError, LinkageError and ThreadDeath
  - InterruptedException
  - ControlThrowable (cf. Breaks)
  
Try { } can only catch NonFatals

### Special case with Futures

- NonFatals of type Exception thrown in a future leads to Future.failure( ) BUT... 
- NonFatals of type Errors are NOT thrown directly but are instead packed in an ExecutionException("Boxed Error", e) and this is this Exception that is thrown. The initial Error can than be accessed with .getCause
- Fatals will not even be caught or boxed: they just make the thread crash.

### Catching

Since NonFatal ensures us that we will catch only non-fatal stuff:

`Try {...} recover {
case e: Throwable => ...
}`

or

`try {...} catch { case NonFatal(e) => ...} finally {...}`


### Printing

To print an error message better using `toString` instead of `getMassage`. In case of BoxedError, add `getCause`.