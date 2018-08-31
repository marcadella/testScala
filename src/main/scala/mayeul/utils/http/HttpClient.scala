package mayeul.utils.http

import java.io.{FileOutputStream, InputStream}
import java.nio.file.Path

import mayeul.utils.StringUtils
import mayeul.utils.json.Json
import mayeul.utils.logging.Logging
import org.apache.commons.io.IOUtils
import scalaj.http._

import scala.concurrent.duration._

class HttpClient() extends Logging {
  import HttpClient._

  /**
    * Create the request
    * @param uri Part of the url before the '?'
    * @param params Key-values added after '?' separated with '&'
    * @param headers Key-value headers
    * @param connTimeout Timeout for establishment of a connection
    * @param readTimeout Timeout from when the connection is established and when then data start arriving
    * @return HttpRequest
    *
    * Note the whole url (uri + params) is obtained with request.urlBuilder(request)
    */
  protected def request(uri: String,
                        params: Map[String, String] = Map(),
                        headers: Map[String, String] = Map(),
                        connTimeout: Duration = 1.second,
                        readTimeout: Duration = 5.seconds): HttpRequest = {
    HttpClient.wrappedRequest(uri, params, headers, connTimeout, readTimeout)
  }

  /**
    * HEAD
    */
  final def head(uri: String,
                 params: Map[String, String] = Map(),
                 headers: Map[String, String] = Map(),
                 connTimeout: Duration = 1.second): HttpRequest = {
    request(uri, params, headers, connTimeout)
      .method("HEAD")
  }

  /**
    * GET
    */
  final def get(uri: String,
                params: Map[String, String] = Map(),
                headers: Map[String, String] = Map(),
                connTimeout: Duration = 1.second,
                readTimeout: Duration = 5.seconds): HttpRequest = {
    request(uri, params, headers, connTimeout, readTimeout)
      .method("GET")
  }

  /**
    * PUT
    */
  final def put(uri: String,
                data: String,
                params: Map[String, String] = Map(),
                headers: Map[String, String] = Map(),
                connTimeout: Duration = 1.second,
                readTimeout: Duration = 5.seconds): HttpRequest = {
    request(uri, params, headers, connTimeout, readTimeout)
      .put(data)
  }
  final def putArray(uri: String,
                     data: Array[Byte],
                     params: Map[String, String] = Map(),
                     headers: Map[String, String] = Map(),
                     connTimeout: Duration = 1.second,
                     readTimeout: Duration = 5.seconds): HttpRequest = {
    request(uri, params, headers, connTimeout, readTimeout)
      .put(data)
  }

  /**
    * POST
    */
  final def post(uri: String,
                 data: String,
                 params: Map[String, String] = Map(),
                 headers: Map[String, String] = Map(),
                 connTimeout: Duration = 1.second,
                 readTimeout: Duration = 5.seconds): HttpRequest = {
    request(uri, params, headers, connTimeout, readTimeout)
      .postData(data)
  }
  final def postArray(uri: String,
                      data: Array[Byte],
                      params: Map[String, String] = Map(),
                      headers: Map[String, String] = Map(),
                      connTimeout: Duration = 1.second,
                      readTimeout: Duration = 5.seconds): HttpRequest = {
    request(uri, params, headers, connTimeout, readTimeout)
      .postData(data)
  }

  /**
    * DELETE
    */
  final def delete(uri: String,
                   params: Map[String, String] = Map(),
                   headers: Map[String, String] = Map(),
                   connTimeout: Duration): HttpRequest = {
    request(uri, params, headers, connTimeout)
      .method("DELETE")
  }

  /**
    * Blocking
    * Use resultAs or rawResult unless what you want is to deal with InputStream directly
    * You can find parsers in HttpConstants or in Http below
    * Use '.body' on the result to get the body
    */
  def execute[T: Manifest](request: HttpRequest,
                           parser: InputStream => T,
                           description: String = ""): HttpResponse[T] = {
    val resp = request.execute[T](parser)
    validateResponseCode(resp, request, description)
    resp
  }

  /**
    * Blocking
    * Smarter than using execute with 'HttpConstants.readString'
    * since parse http body as String using server charset or configured charset
    * Use '.body' on the result to get the body
    */
  def rawResult(request: HttpRequest,
                description: String = ""): HttpResponse[String] = {
    val resp = request.asString
    validateResponseCode(resp, request, description)
    resp
  }

  /**
    * Blocking
    * Smarter than using execute with 'HttpClient.jsonParser'
    * since parse http body as String using server charset or configured charset
    * Use '.body' on the result to get the body
    */
  def resultAs[T: Manifest](request: HttpRequest,
                            description: String = ""): HttpResponse[T] = {
    val resp = rawResult(request, description)
    resp.copy(body = Json.parse[T](resp.body))
  }

  def download(uri: String, path: Path): Unit = {
    val req = get(uri)
    execute(req, HttpClient.copyToFile(path.toString), s"downloading $uri")
  }
}

object HttpClient extends Logging {

  /**
    * Use false in production!
    */
  val trustAllCertificates: Boolean = false

  if (trustAllCertificates)
    log.error(s"Flag 'trustAllCertificates' is true. Only for debugging!!")

  /**
    * Constructor
    */
  lazy val stdClient = new HttpClient()

  /**
    * Utilities
    */
  def prettyPrintRequest(request: HttpRequest): String = {
    s"\nHttp ${request.method} to ${request
      .urlBuilder(request)}\n${request.headers.mkString("\n")}"
  }

  /**
    * Do not use me directly unless you know what you are doing.
    * Use stdClient or oAuthClient instead
    */
  def wrappedRequest(uri: String,
                     params: Map[String, String] = Map(),
                     headers: Map[String, String] = Map(),
                     connTimeout: Duration = 1.second,
                     readTimeout: Duration = 5.seconds): HttpRequest = {
    val req: HttpRequest = Http(uri)
      .params(params)
      .timeout(connTimeout.toMillis.toInt, readTimeout.toMillis.toInt)
      .headers(headers)
    if (trustAllCertificates) {
      log.debug(s"Flag 'trustAllCertificates' is true. Only for debugging!!")
      req.option(HttpOptions.allowUnsafeSSL)
    } else req
  }

  def validateResponseCode(response: HttpResponse[_],
                           request: HttpRequest,
                           description: => String): Unit = {
    val code = response.code
    if (HttpErrorCode.isFailure(code)) {
      val msg =
        s"Invalid response code (${HttpErrorCode(code)}) when $description${prettyPrintRequest(request)}"
      log.warn(msg)
      log.warn(
        s"Response: ${StringUtils.truncateString(response.body.toString)}")
      throw new RuntimeException(msg)
    } else {
      log.info(s"Request: ${prettyPrintRequest(request)}")
      log.debug(
        s"Response: ${StringUtils.truncateString(response.body.toString)}")
    }
  }

  /********* Parsers *********/
  //Cf HttpConstants for more parsers
  def jsonParser[T: Manifest](in: InputStream): T = {
    Json.parse[T](HttpConstants.readString(in))
  }

  def copyToFile(dest: String): InputStream => Unit = { in: InputStream =>
    {
      val os = new FileOutputStream(dest.toString)
      try {
        IOUtils.copyLarge(in, os)
      } finally {
        os.close()
      }
    }
  }
}
