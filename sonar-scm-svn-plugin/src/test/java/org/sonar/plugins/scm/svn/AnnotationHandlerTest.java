package org.sonar.plugins.scm.svn;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class AnnotationHandlerTest {

  @Test
  public void normalizeAuthor() {
    assertThat(AnnotationHandler.normalizeAuthor(null), is(nullValue()));
    assertThat(AnnotationHandler.normalizeAuthor(""), is(equalTo("")));
    assertThat(AnnotationHandler.normalizeAuthor("."), is(equalTo("")));
    assertThat(AnnotationHandler.normalizeAuthor("name"), is(equalTo("name")));
    assertThat(AnnotationHandler.normalizeAuthor("Two names"), is(equalTo("names two")));
    assertThat(AnnotationHandler.normalizeAuthor("five-WORDS.with,a delimiter"), is(equalTo("a delimiter five-words with")));
    assertThat(AnnotationHandler.normalizeAuthor(",multiple . .delimiters  "), is(equalTo("delimiters multiple")));
    assertThat(AnnotationHandler.normalizeAuthor("an.email@addr.ess"), is(equalTo("an email")));
    assertThat(AnnotationHandler.normalizeAuthor("äöüÄÖÜß"), is(equalTo("äöüäöüß")));
  }
}
