/*
 * Copyright 2015-2018 original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.rest.webmvc.json;

import static org.assertj.core.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.hateoas.mediatype.MessageResolver;

/**
 * Unit tests for {@link EnumTranslator}.
 *
 * @author Oliver Gierke
 */
class EnumTranslatorUnitTests {

	StaticMessageSource messageSource;
	EnumTranslator configuration;

	@BeforeEach
	void setUp() {

		LocaleContextHolder.setLocale(Locale.US);

		this.messageSource = new StaticMessageSource();
		this.messageSource.addMessage(MyEnum.class.getName().concat(".").concat(MyEnum.FIRST_VALUE.name()), Locale.US,
				"Translated");
		this.configuration = new EnumTranslator(MessageResolver.of(messageSource));
	}

	@Test // DATAREST-654
	void rejectsNullMessageSourceAccessor() {

		assertThatIllegalArgumentException() //
				.isThrownBy(() -> new EnumTranslator(null));
	}

	@Test // DATAREST-654
	void parsesNullForNullSource() {
		assertThat(configuration.fromText(MyEnum.class, null)).isNull();
	}

	@Test // DATAREST-654
	void parsesNullForEmptySource() {
		assertThat(configuration.fromText(MyEnum.class, null)).isNull();
	}

	@Test // DATAREST-654
	void parsesNullForUnknownValue() {
		assertThat(configuration.fromText(MyEnum.class, "Foobar")).isNull();
	}

	@Test // DATAREST-654
	void returnsEnumNameIfDefaultTranslationIsDisabled() {

		configuration.setEnableDefaultTranslation(false);

		assertThat(configuration.asText(MyEnum.SECOND_VALUE)).isEqualTo(MyEnum.SECOND_VALUE.name());
	}

	@Test // DATAREST-654
	void returnsDefaultTranslationByDefault() {

		assertThat(configuration.asText(MyEnum.SECOND_VALUE)).isEqualTo("Second value");
	}

	@Test // DATAREST-654
	void parsesEnumNameIfDefaultTranslationIsDisabled() {

		configuration.setEnableDefaultTranslation(false);

		assertThat(configuration.fromText(MyEnum.class, "FIRST_VALUE")).isEqualTo(MyEnum.FIRST_VALUE);
	}

	@Test // DATAREST-654
	void parsesStandardTranslationAndEnumNameByDefault() {

		assertThat(configuration.fromText(MyEnum.class, "FIRST_VALUE")).isEqualTo(MyEnum.FIRST_VALUE);
		assertThat(configuration.fromText(MyEnum.class, "Second value")).isEqualTo(MyEnum.SECOND_VALUE);
	}

	@Test // DATAREST-654
	void translatesEnumName() {

		LocaleContextHolder.setLocale(Locale.US);

		messageSource.addMessage(MyEnum.class.getName().concat(".").concat(MyEnum.FIRST_VALUE.name()), Locale.US,
				"Translated");

		assertThat(configuration.asText(MyEnum.FIRST_VALUE)).isEqualTo("Translated");
	}

	@Test // DATAREST-654
	void parsesEnumNameByDefaultEvenIfMessageDefined() {

		// Parses resolved message and enum name
		assertThat(configuration.fromText(MyEnum.class, "Translated")).isEqualTo(MyEnum.FIRST_VALUE);
		assertThat(configuration.fromText(MyEnum.class, "FIRST_VALUE")).isEqualTo(MyEnum.FIRST_VALUE);

		// Does not parse default translation as explicit translation is available
		assertThat(configuration.fromText(MyEnum.class, "First value")).isNull();

		// Parses default translation as no explicit translation is available
		assertThat(configuration.fromText(MyEnum.class, "Second value")).isEqualTo(MyEnum.SECOND_VALUE);
		assertThat(configuration.fromText(MyEnum.class, "SECOND_VALUE")).isEqualTo(MyEnum.SECOND_VALUE);
	}

	@Test // DATAREST-654
	void parsesEnumWithDefaultTranslationDisabled() {

		configuration.setEnableDefaultTranslation(false);

		// Parses default translation as no explicit translation is available
		assertThat(configuration.fromText(MyEnum.class, "Second value")).isNull();
		assertThat(configuration.fromText(MyEnum.class, "SECOND_VALUE")).isEqualTo(MyEnum.SECOND_VALUE);
	}

	@Test
	void doesNotResolveEnumNameAsFallbackIfConfigured() {

		configuration.setParseEnumNameAsFallback(false);

		// Parses resolved message and enum name
		assertThat(configuration.fromText(MyEnum.class, "Translated")).isEqualTo(MyEnum.FIRST_VALUE);
		assertThat(configuration.fromText(MyEnum.class, "FIRST_VALUE")).isNull();

		// Parses default translation as no explicit translation is available
		assertThat(configuration.fromText(MyEnum.class, "Second value")).isEqualTo(MyEnum.SECOND_VALUE);
		assertThat(configuration.fromText(MyEnum.class, "SECOND_VALUE")).isNull();
	}

	static enum MyEnum {
		FIRST_VALUE, SECOND_VALUE;
	}
}
