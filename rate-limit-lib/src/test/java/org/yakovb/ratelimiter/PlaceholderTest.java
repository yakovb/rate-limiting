package org.yakovb.ratelimiter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PlaceholderTest {

  private Placeholder placeholder = new Placeholder();

  @Test
  public void aTest() {
    assertThat(placeholder.giveString(), is(equalTo("x")));
  }
}