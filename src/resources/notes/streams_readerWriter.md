# Java Stream vs Reader/Writer

### Streams, Reader, Writers

`Input/OutputStream` -> stream of __bytes__  
`Reader/Writer` -> stream of __characters__

`InputStream` <> `Reader`  
`OutputStream` <> `Writer`

In the rest of the document we write `stream*` a stream of bytes or characters

### Buffered

All the classes above flush the stream after every write/read. This can be costly so it is a good idea to wrap your
Stream/Reader/Writer into a buffered Stream/Reader/Writer which buffers all reads/writes into a buffer and flushes it only when full (or upon closing).

`BufferedInputStream` <> `BufferedReader`  
`BufferedOutputStream` <> `BufferedWriter`

### Filters

`Filter` -> wrapper mapping a `stream*` and/or adding methods

`FilterInputStream` <> `FilterReader`  
`FilterOutputStream` <> `FilterWriter`

### Bridging both

To convert an `Input/OutputStream` into a `Reader/Writer`, wrap it into a `InputStreamReader`/`OutputStreamWriter`.  
To convert a `Reader/Writer` into an `Input/OutputStream`, wrap it into a `ReaderInputStream`/`WriterInputStream`.

There is also a `PrintStream` which is a chimere: an OutputStream when using `write` and a BufferedWriter when using `print`.
It is supposed to be a way to print streams of bytes as characters and with formatting but I find it confusing and I wonder
if the flushing mechanism is not buggy...

### Files

To read/write a file use:
`FileInputStream` <> `FileReader`  
`FileOutputStream` <> `FileWriter`

(remember to wrap them in a buffered implementation)
