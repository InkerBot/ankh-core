package org.inksnow.ankh.core.config.adapter;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.inksnow.ankh.core.common.util.BootstrapUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@UtilityClass
public class ConfigVaildatorUtils {
  private static final Class<? extends ConstrainableExecutable> javaBeanConstrainableExecutableClass =
      sneakyClassForName("org.hibernate.validator.internal.properties.javabean.JavaBeanHelper$JavaBeanConstrainableExecutable");
  private static final MethodHandle javaBeanConstrainableExecutableGetMethod = BootstrapUtil
      .ofGet("Lorg/hibernate/validator/internal/properties/javabean/JavaBeanHelper$JavaBeanConstrainableExecutable;method:Ljava/lang/reflect/Method;")
      .asType(MethodType.methodType(Method.class, ConstrainableExecutable.class));

  @Getter
  private static final Validator validator = Validation.byProvider(HibernateValidator.class)
      .configure()
      .getterPropertySelectionStrategy(new GetterPropertySelectionStrategy() {
        @Override
        @SneakyThrows
        public Optional<String> getProperty(ConstrainableExecutable executable) {
          if (!javaBeanConstrainableExecutableClass.isInstance(executable)) {
            return Optional.empty();
          }
          val method = (Method) javaBeanConstrainableExecutableGetMethod.invokeExact(executable);
          if (method.getDeclaringClass() == Object.class) {
            return Optional.empty();
          }
          return Optional.of(executable.getName());
        }

        @Override
        public Set<String> getGetterMethodNameCandidates(String propertyName) {
          return Collections.singleton(propertyName);
        }
      })
      .messageInterpolator(new ParameterMessageInterpolator())
      .failFast(false)
      .buildValidatorFactory()
      .getValidator();

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static <T> Class<T> sneakyClassForName(String name) {
    return (Class<T>) Class.forName(name);
  }
}
