package mayeul.utils.http

object HttpErrorCode {
  def apply(code: Int): String = {
    s"$code: ${explanation(code)} -- ${category(code)}"
  }

  def category(code: Int): String = {
    (code / 100) match {
      case 1 => "Informational"
      case 2 => "Success"
      case 3 => "Redirection"
      case 4 => "Client Error"
      case 5 => "Server Error"
      case _ => "Unknown Category"
    }
  }

  def explanation(code: Int): String = {
    code match {
      case 202 => "Accepted"
      case 502 => "Bad Gateway"
      case 400 => "Bad Request"
      case 409 => "Conflict"
      case 100 => "Continue"
      case 201 => "Created"
      case 417 => "Expectation Failed"
      case 424 => "Failed Dependency"
      case 403 => "Forbidden"
      case 504 => "Gateway Timeout"
      case 410 => "Gone"
      case 505 => "HTTP Version Not Supported"
      case 419 => "Insufficient space on resource"
      case 507 => "Insufficient Storage"
      case 500 => "Server Error"
      case 411 => "Length Required"
      case 423 => "Locked"
      case 420 => "Method failure"
      case 405 => "Method Not Allowed"
      case 301 => "Moved Permanently"
      case 302 => "Moved Temporarily"
      case 207 => "Partial Update OK"
      case 300 => "Multiple Choices"
      case 204 => "No Content"
      case 203 => "Non Authoritative Information"
      case 406 => "Not Acceptable"
      case 404 => "Not Found"
      case 501 => "Not Implemented"
      case 304 => "Not Modified"
      case 200 => "OK"
      case 206 => "Partial Content"
      case 402 => "Payment Required"
      case 412 => "Precondition Failed"
      case 102 => "Processing"
      case 407 => "Proxy Authentication Required"
      case 408 => "Request Timeout"
      case 413 => "Request Entity Too Large"
      case 414 => "Request-URI Too Long"
      case 416 => "Requested Range Not Satisfiable"
      case 205 => "Reset Content"
      case 303 => "See Other"
      case 503 => "Service Unavailable"
      case 101 => "Switching Protocols"
      case 307 => "Temporary Redirect"
      case 401 => "Unauthorized"
      case 422 => "Unprocessable Entity"
      case 415 => "Unsupported Media Type"
      case 305 => "Use Proxy"
      case _   => "Unknown Error Code"
    }
  }

  def isFailure(code: Int): Boolean = {
    code < 200 || code >= 300
  }
}
