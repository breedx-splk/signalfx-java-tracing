package datadog.trace.instrumentation.vertx;

import static datadog.trace.agent.tooling.ByteBuddyElementMatchers.safeHasSuperType;
import static java.util.Collections.unmodifiableMap;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import com.google.auto.service.AutoService;
import datadog.trace.agent.tooling.Instrumenter;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(Instrumenter.class)
public final class HandlerInstrumentation extends Instrumenter.Default {

  public HandlerInstrumentation() {
    super("vertx", "vert.x");
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return not(isInterface()).and(safeHasSuperType(named("io.vertx.core.Handler")));
  }

  @Override
  public String[] helperClassNames() {
    return new String[] {
      "datadog.trace.agent.decorator.BaseDecorator",
      "datadog.trace.agent.decorator.ServerDecorator",
      "datadog.trace.agent.decorator.HttpServerDecorator",
      packageName + ".RoutingContextDecorator",
    };
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    return unmodifiableMap(
        Stream.of(
                new AbstractMap.SimpleEntry<>(
                    isMethod()
                        .and(named("handle"))
                        .and(not(takesArgument(0, named("io.vertx.ext.web.RoutingContext")))),
                    VertxHandlerAdvice.class.getName()),
                new AbstractMap.SimpleEntry<>(
                    isMethod()
                        .and(named("handle"))
                        .and(takesArgument(0, named("io.vertx.ext.web.RoutingContext"))),
                    RoutingContextHandlerAdvice.class.getName()))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
  }
}