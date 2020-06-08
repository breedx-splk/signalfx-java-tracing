// Modified by SignalFx
import datadog.opentracing.decorators.ErrorFlag
import datadog.trace.agent.test.AgentTestRunner
import datadog.trace.agent.test.utils.ConfigUtils
import datadog.trace.api.Trace
import datadog.trace.bootstrap.instrumentation.api.Tags
import dd.test.trace.annotation.SayTracedHello

import spock.lang.Shared

import java.util.concurrent.Callable

class TraceAnnotationsTest extends AgentTestRunner {

  static {
    ConfigUtils.updateConfig {
      System.clearProperty("dd.trace.annotations")
    }
  }

  @Shared
  def methodsBlacklisted

  def setupSpec() {
    methodsBlacklisted = System.getProperty("signalfx.trace.annotated.method.blacklist") != null
  }

  def "test simple case annotations"() {
    setup:
    // Test single span in new trace
    SayTracedHello.sayHello()

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          serviceName "test"
          resourceName "SayTracedHello.sayHello"
          operationName "trace.annotation"
          parent()
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }

  def "test simple case with only operation name set"() {
    setup:
    // Test single span in new trace
    SayTracedHello.sayHA()

    expect:
    if (methodsBlacklisted) {
      assertTraces(0) {}
    } else {
      assertTraces(1) {
        trace(0, 1) {
          span(0) {
            serviceName "test"
            resourceName "SayTracedHello.sayHA"
            operationName "SAY_HA"
            spanType "DB"
            parent()
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
        }
      }
    }
  }

  def "test simple case with only resource name set"() {
    setup:
    // Test single span in new trace
    SayTracedHello.sayHelloOnlyResourceSet()

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          serviceName "test"
          resourceName "WORLD"
          operationName "trace.annotation"
          parent()
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }

  def "test simple case with both resource and operation name set"() {
    setup:
    // Test single span in new trace
    SayTracedHello.sayHAWithResource()

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          serviceName "test"
          resourceName "EARTH"
          operationName "SAY_HA"
          spanType "DB"
          parent()
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }

  def "test alternative simple case annotations"() {
    setup:
    // Test single span in new trace
    SayTracedHello.sayHelloAlt()

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          serviceName "test"
          resourceName "SayTracedHello.sayHelloAlt"
          operationName "SayTracedHello.sayHelloAlt"
          parent()
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }

  def "test alternative complex case annotations"() {
    setup:
    // Test single span in new trace
    SayTracedHello.sayBye()

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          serviceName "test"
          resourceName "SayTracedHello.sayBye"
          operationName "farewell"
          parent()
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }

  def "test complex case annotations"() {
    when:
    // Test new trace with 2 children spans
    SayTracedHello.sayHELLOsayHA()

    then:
    assertTraces(1) {
      if (methodsBlacklisted) {
        trace(0, 2) {
          span(0) {
            resourceName "SayTracedHello.sayHELLOsayHA"
            operationName "NEW_TRACE"
            spanType "DB"
            parent()
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
          span(1) {
            serviceName "test"
            resourceName "SayTracedHello.sayHello"
            operationName "trace.annotation"
            childOf span(0)
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
        }
      } else {
        trace(0, 3) {
          span(0) {
            resourceName "SayTracedHello.sayHELLOsayHA"
            operationName "NEW_TRACE"
            parent()
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
          span(1) {
            resourceName "SayTracedHello.sayHA"
            operationName "SAY_HA"
            spanType "DB"
            childOf span(0)
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
          span(2) {
            serviceName "test"
            resourceName "SayTracedHello.sayHello"
            operationName "trace.annotation"
            childOf span(0)
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
        }
      }
    }
  }

  def "test complex case with resource name at top level"() {
    when:
    // Test new trace with 2 children spans
    SayTracedHello.sayHELLOsayHAWithResource()

    then:
    if (methodsBlacklisted) {
      assertTraces(1) {
        trace(0, 1) {
          span(0) {
            resourceName "SayTracedHello.sayHello"
            operationName "trace.annotation"
            parent()
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
        }
      }
    } else {
      assertTraces(1) {
        trace(0, 3) {
          span(0) {
            resourceName "WORLD"
            operationName "NEW_TRACE"
            parent()
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
          span(1) {
            resourceName "SayTracedHello.sayHA"
            operationName "SAY_HA"
            spanType "DB"
            childOf span(0)
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
          span(2) {
            serviceName "test"
            resourceName "SayTracedHello.sayHello"
            operationName "trace.annotation"
            childOf span(0)
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
        }
      }
    }
  }

  def "test complex case with resource name at various levels"() {
    when:
    // Test new trace with 2 children spans
    SayTracedHello.sayHELLOsayHAMixedResourceChildren()

    then:
    assertTraces(1) {
      trace(0, 3) {
        span(0) {
          resourceName "WORLD"
          operationName "NEW_TRACE"
          parent()
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
        span(1) {
          resourceName "EARTH"
          operationName "SAY_HA"
          spanType "DB"
          childOf span(0)
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
        span(2) {
          serviceName "test"
          resourceName "SayTracedHello.sayHello"
          operationName "trace.annotation"
          childOf span(0)
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }

  def "test exception exit"() {
    setup:

    TEST_TRACER.addDecorator(new ErrorFlag())

    Throwable error = null
    try {
      SayTracedHello.sayERROR()
    } catch (final Throwable ex) {
      error = ex
    }

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          resourceName "SayTracedHello.sayERROR"
          operationName "SayTracedHello.sayERROR"
          errored true
          tags {
            "$Tags.COMPONENT" "trace"
            errorTags(error.class)
            defaultTags()
          }
        }
      }
    }
  }

  def "test exception exit with resource name"() {
    setup:

    TEST_TRACER.addDecorator(new ErrorFlag())

    Throwable error = null
    try {
      SayTracedHello.sayERRORWithResource()
    } catch (final Throwable ex) {
      error = ex
    }

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          resourceName "WORLD"
          operationName "ERROR"
          errored true
          tags {
            "$Tags.COMPONENT" "trace"
            errorTags(error.class)
            defaultTags()
          }
        }
      }
    }
  }

  def "test annonymous class annotations"() {
    setup:
    // Test anonymous classes with package.
    SayTracedHello.fromCallable()

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          resourceName "SayTracedHello\$1.call"
          operationName "SayTracedHello\$1.call"
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }

    when:
    // Test anonymous classes with no package.
    new Callable<String>() {
      @Trace
      @Override
      String call() throws Exception {
        return "Howdy!"
      }
    }.call()
    TEST_WRITER.waitForTraces(2)

    then:
    assertTraces(2) {
      trace(0, 1) {
        span(0) {
          resourceName "SayTracedHello\$1.call"
          operationName "SayTracedHello\$1.call"
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
        trace(1, 1) {
          span(0) {
            resourceName "TraceAnnotationsTest\$1.call"
            operationName "trace.annotation"
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
        }
      }
    }
  }

  def "test inner class tracing"() {
    when:
    SayTracedHello.SomeInnerClass.one()

    then:
    if (methodsBlacklisted) {
      assertTraces(0) {}
    } else {
      assertTraces(1) {
        trace(0, 2) {
          span(0) {
            resourceName "SomeInnerClass.one"
            operationName "SomeInnerClass.one"
            parent()
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
          span(1) {
            resourceName "SomeInnerClass.two"
            operationName "SomeInnerClass.two"
            childOf span(0)
            errored false
            tags {
              "$Tags.COMPONENT" "trace"
              defaultTags()
            }
          }
        }
      }
    }
  }
}
