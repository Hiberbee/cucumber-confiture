package com.hiberbee.cucumber.gherkin.dsl;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public enum Maybe {
  SHOULD,
  SHOULD_NOT,
  IS_NOT,
  IS,
  HAS_NOT,
  HAS,
  HAVE,
  ARE,
  ARE_NOT;

  public @NotNull Predicate<Boolean> predicate() {
    return Predicate.isEqual(
        Predicate.isEqual(IS)
            .or(Predicate.isEqual(ARE))
            .or(Predicate.isEqual(HAS))
            .or(Predicate.isEqual(HAVE))
            .or(Predicate.isEqual(SHOULD))
            .test(this));
  }

  public @NotNull Boolean yes() {
    return this.predicate().test(true);
  }

  public @NotNull Boolean no() {
    return this.predicate().test(false);
  }

  public @NotNull Optional<Boolean> optional() {
    return Optional.of(this.yes());
  }
}
