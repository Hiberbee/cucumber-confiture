package com.hiberbee.cucumber.gherkin.dsl;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;

public enum Maybe {
  CONTAINS,
  DOES_NOT_CONTAIN,
  SHOULD,
  SHOULD_NOT,
  IS_NOT,
  IS,
  HAS_NOT,
  HAS,
  HAVE,
  ARE,
  ARE_NOT;

  public @Nonnull Predicate<Boolean> predicate() {
    return Predicate.isEqual(
        Predicate.isEqual(IS)
            .or(Predicate.isEqual(ARE))
            .or(Predicate.isEqual(HAS))
            .or(Predicate.isEqual(HAVE))
            .or(Predicate.isEqual(SHOULD))
            .test(this));
  }

  public @Nonnull Boolean yes() {
    return this.predicate().test(true);
  }

  public @Nonnull Boolean no() {
    return this.predicate().test(false);
  }

  public @Nonnull Optional<Boolean> optional() {
    return Optional.of(this.yes());
  }
}
