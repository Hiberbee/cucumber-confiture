package com.hiberbee.cucumber.annotations;

import org.springframework.cache.annotation.Cacheable;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Cacheable(value = "scenario", keyGenerator = "methodNameGenerator")
public @interface ScenarioState {}
