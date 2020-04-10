package com.hiberbee.cucumber.annotations;

import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.MagicConstant;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Cacheable("scenario")
public @interface ScenarioState {

  @AliasFor(annotation = Cacheable.class, attribute = "key")
  @MagicConstant
  @Language("SpEL")
  String value();
}
