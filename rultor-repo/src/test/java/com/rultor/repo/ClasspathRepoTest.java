/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.repo;

import com.jcabi.aspects.Immutable;
import com.jcabi.urn.URN;
import com.rultor.spi.Instance;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ClasspathRepo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class ClasspathRepoTest {

    /**
     * ClasspathRepo can make a spec.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesSpecFromText() throws Exception {
        final Repo repo = new ClasspathRepo();
        final String[] texts = new String[] {
            "com.rultor.repo.ClasspathRepoTest$Foo(55)",
            "java.lang.String( 'te   \n st' )",
            "java.lang.Integer ( 123 )",
            "java.lang.Long(-44L)",
            "foo.SomeClass(1, FALSE, TRUE, 8L, 'test')",
            "java.lang.Double(-44.66)",
            "com.rultor.repo.ClasspathRepoTest$Foo (55\n)",
            "com.first(com.second(com.third(), com.forth()))",
            "[com.second(com.third(), com.forth()), 4]",
            "{\"test\": com.second(com.third(), com.forth()), \"c\": 77}",
            "\"\"\"\nsome\nunformatted\ttext\t\u20ac\u0433\n\"\"\"",
        };
        final User user = Mockito.mock(User.class);
        final URN urn = new URN("urn:facebook:1");
        Mockito.doReturn(urn).when(user).urn();
        for (String text : texts) {
            MatcherAssert.assertThat(
                repo.make(user, new Spec.Simple(text)).asText(),
                Matchers.equalTo(
                    repo.make(user, new Spec.Simple(text)).asText()
                )
            );
        }
    }

    /**
     * ClasspathRepo can make a simple instance.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesInstanceFromSimpleSpec() throws Exception {
        final Repo repo = new ClasspathRepo();
        final String[] texts = new String[] {
            "java.lang.Double(-1.5)",
            "java.lang.Boolean(TRUE)",
            "\"\"\"\nsome\u20actext\n\t\"\"\"",
        };
        final User user = Mockito.mock(User.class);
        final URN urn = new URN("urn:facebook:2");
        Mockito.doReturn(urn).when(user).urn();
        for (String text : texts) {
            final Spec spec = new Spec.Simple(text);
            final Object obj = repo.make(user, spec);
            MatcherAssert.assertThat(obj, Matchers.notNullValue());
        }
    }

    /**
     * ClasspathRepo can make a plain text spec.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesSpecFromPlainText() throws Exception {
        final String text = "\"\"\"\ns\nnome\t\tued\ntext\u20ac\n\"\"\"";
        final Repo repo = new ClasspathRepo();
        final User user = Mockito.mock(User.class);
        final URN urn = new URN("urn:facebook:5");
        Mockito.doReturn(urn).when(user).urn();
        MatcherAssert.assertThat(
            repo.make(user, new Spec.Simple(text)),
            Matchers.notNullValue()
        );
        MatcherAssert.assertThat(
            repo.make(user, new Spec.Simple(text)).asText(),
            Matchers.equalTo(text)
        );
    }

    /**
     * ClasspathRepo can make an instance.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesInstanceFromSpec() throws Exception {
        final Repo repo = new ClasspathRepo();
        final Spec spec = new Spec.Simple(
            "com.rultor.repo.ClasspathRepoTest$Foo(2L)"
        );
        ClasspathRepoTest.Foo.COUNTER.set(0);
        final User user = Mockito.mock(User.class);
        final URN urn = new URN("urn:facebook:77");
        Mockito.doReturn(urn).when(user).urn();
        final Users users = Mockito.mock(Users.class);
        final Object instance = repo.make(user, spec)
            .instantiate(users, new Work.None());
        MatcherAssert.assertThat(
            ClasspathRepoTest.Foo.COUNTER.get(),
            Matchers.equalTo(2L)
        );
        Instance.class.cast(instance).pulse();
        MatcherAssert.assertThat(
            ClasspathRepoTest.Foo.COUNTER.get(),
            Matchers.equalTo(-1L)
        );
    }

    /**
     * Test class.
     */
    @Immutable
    public static final class Foo implements Instance {
        /**
         * Static counter.
         */
        public static final AtomicLong COUNTER = new AtomicLong();
        /**
         * Public ctor.
         * @param number The number
         */
        public Foo(final long number) {
            ClasspathRepoTest.Foo.COUNTER.addAndGet(number);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void pulse() {
            ClasspathRepoTest.Foo.COUNTER.set(-1);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return ClasspathRepoTest.Foo.COUNTER.toString();
        }
    }

}
