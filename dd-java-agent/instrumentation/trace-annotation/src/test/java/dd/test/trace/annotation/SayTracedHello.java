// Modified by SignalFx
package dd.test.trace.annotation;

import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activeSpan;

import datadog.trace.api.DDTags;
import datadog.trace.api.Trace;
import java.util.concurrent.Callable;

public class SayTracedHello {

  @Trace
  public static String sayHello() {
    activeSpan().setTag(DDTags.SERVICE_NAME, "test");
    return "hello!";
  }

  @Trace(resourceName = "WORLD")
  public static String sayHelloOnlyResourceSet() {
    activeSpan().setTag(DDTags.SERVICE_NAME, "test");
    return "hello!";
  }

  @Trace(operationName = "SAY_HA")
  public static String sayHA() {
    if (activeSpan() != null) {
      activeSpan().setTag(DDTags.SERVICE_NAME, "test");
      activeSpan().setTag(DDTags.SPAN_TYPE, "DB");
    }
    return "HA!!";
  }

  @com.signalfx.tracing.api.Trace()
  public static String sayHelloAlt() {
    activeSpan().setTag(DDTags.SERVICE_NAME, "test");
    return "hello!";
  }

  @com.signalfx.tracing.api.Trace(operationName = "farewell")
  public static String sayBye() {
    return "bye";
  }

  @Trace(operationName = "SAY_HA", resourceName = "EARTH")
  public static String sayHAWithResource() {
    activeSpan().setTag(DDTags.SERVICE_NAME, "test");
    activeSpan().setTag(DDTags.SPAN_TYPE, "DB");
    return "HA EARTH!!";
  }

  @Trace(operationName = "NEW_TRACE")
  public static String sayHELLOsayHA() {
    activeSpan().setTag(DDTags.SERVICE_NAME, "test2");
    return sayHello() + sayHA();
  }

  @Trace(operationName = "NEW_TRACE", resourceName = "WORLD")
  public static String sayHELLOsayHAWithResource() {
    if (activeSpan() != null) {
      activeSpan().setTag(DDTags.SERVICE_NAME, "test2");
    }
    return sayHello() + sayHA();
  }

  @Trace(operationName = "NEW_TRACE", resourceName = "WORLD")
  public static String sayHELLOsayHAMixedResourceChildren() {
    activeSpan().setTag(DDTags.SERVICE_NAME, "test2");
    return sayHello() + sayHAWithResource();
  }

  // We are testing `-Dsignalfx.trace.methods` over annotating directly
  public static String sayERROR() {
    throw new RuntimeException();
  }

  @Trace(operationName = "ERROR", resourceName = "WORLD")
  public static String sayERRORWithResource() {
    throw new RuntimeException();
  }

  public static String fromCallable() throws Exception {
    return new Callable<String>() {
      @com.newrelic.api.agent.Trace
      @Override
      public String call() throws Exception {
        return "Howdy!";
      }
    }.call();
  }

  public static String fromCallableWhenDisabled() throws Exception {
    return new Callable<String>() {
      @com.newrelic.api.agent.Trace
      @Override
      public String call() throws Exception {
        return "Howdy!";
      }
    }.call();
  }

  // We are testing `-Dsignalfx.trace.methods` instead of annotating directly
  public static class SomeInnerClass {
    public static String one() {
      return two();
    }

    public static String two() {
      return "Two!";
    }
  }
}
