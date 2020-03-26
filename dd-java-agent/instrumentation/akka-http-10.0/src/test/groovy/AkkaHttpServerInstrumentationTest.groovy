// Modified by SignalFx
import datadog.trace.agent.test.asserts.TraceAssert
import datadog.trace.agent.test.base.HttpServerTest
import datadog.trace.api.DDSpanTypes
import datadog.trace.instrumentation.akkahttp.AkkaHttpServerDecorator
import datadog.trace.instrumentation.api.Tags

import static datadog.trace.agent.test.base.HttpServerTest.ServerEndpoint.EXCEPTION
import static datadog.trace.agent.test.base.HttpServerTest.ServerEndpoint.SUCCESS

abstract class AkkaHttpServerInstrumentationTest extends HttpServerTest<Object, AkkaHttpServerDecorator> {

  @Override
  AkkaHttpServerDecorator decorator() {
    return AkkaHttpServerDecorator.DECORATE
  }

  @Override
  String expectedOperationName() {
    return "akka-http.request"
  }

  @Override
  boolean testExceptionBody() {
    false
  }

// FIXME: This doesn't work because we don't support bindAndHandle.
//  @Override
//  def startServer(int port) {
//    AkkaHttpTestWebServer.start(port)
//  }
//
//  @Override
//  void stopServer(Object ignore) {
//    AkkaHttpTestWebServer.stop()
//  }

  void serverSpan(TraceAssert trace, int index, String traceID = null, String parentID = null, String method = "GET", ServerEndpoint endpoint = SUCCESS) {
    trace.span(index) {
      serviceName expectedServiceName()
      operationName expectedOperationName()
      resourceName endpoint.status == 404 ? "404" : "${endpoint.resolve(address).path}"
      spanType DDSpanTypes.HTTP_SERVER
      errored endpoint.errored
      if (parentID != null) {
        traceId traceID
        parentId parentID
      } else {
        parent()
      }
      tags {
        defaultTags(true)
        "$Tags.COMPONENT" serverDecorator.component()
        if (endpoint.errored) {
          "$Tags.ERROR" endpoint.errored
          "sfx.error.message" { it == null || it == EXCEPTION.body }
          "sfx.error.object" { it == null || it == Exception.name }
          "sfx.error.kind" { it == null || it instanceof String }
          "sfx.error.stack" { it == null || it instanceof String }
        }
        "$Tags.HTTP_STATUS" endpoint.status
        "$Tags.HTTP_URL" "${endpoint.resolve(address)}"
        "$Tags.HTTP_METHOD" method
        "$Tags.SPAN_KIND" Tags.SPAN_KIND_SERVER
      }
    }
  }
}

class AkkaHttpServerInstrumentationTestSync extends AkkaHttpServerInstrumentationTest {
  @Override
  def startServer(int port) {
    AkkaHttpTestSyncWebServer.start(port)
  }

  @Override
  void stopServer(Object ignore) {
    AkkaHttpTestSyncWebServer.stop()
  }
}

class AkkaHttpServerInstrumentationTestAsync extends AkkaHttpServerInstrumentationTest {
  @Override
  def startServer(int port) {
    AkkaHttpTestAsyncWebServer.start(port)
  }

  @Override
  void stopServer(Object ignore) {
    AkkaHttpTestAsyncWebServer.stop()
  }
}
