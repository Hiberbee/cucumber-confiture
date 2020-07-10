package com.hiberbee.cucumber.annotations;

import com.hiberbee.cucumber.configurations.CucumberConfiguration;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.MagicConstant;
import org.springframework.cache.annotation.*;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@CachePut(CucumberConfiguration.Caches.FEATURE)
public @interface FeatureState {

  @AliasFor(annotation = CachePut.class, attribute = "key")
  @Language("SpEL")
  String value();
}
