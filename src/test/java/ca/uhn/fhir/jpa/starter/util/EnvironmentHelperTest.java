package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.jpa.starter.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnvironmentHelperTest {

	@Test
	void getAllPropertiesReturnsEntriesFromEnumerableSources() {
		StandardEnvironment env = new StandardEnvironment();
		env.getPropertySources().addFirst(new MapPropertySource("source-a", Map.of("key.one", "value-one")));

		Map<String, Object> result = EnvironmentHelper.getAllProperties(env);

		assertEquals("value-one", result.get("key.one"));
	}

	@Test
	void getAllPropertiesFirstSourceWinsOnDuplicateKeys() {
		StandardEnvironment env = new StandardEnvironment();
		env.getPropertySources().addFirst(new MapPropertySource("low-priority", Map.of("shared.key", "loser")));
		env.getPropertySources().addFirst(new MapPropertySource("high-priority", Map.of("shared.key", "winner")));

		Map<String, Object> result = EnvironmentHelper.getAllProperties(env);

		assertEquals("winner", result.get("shared.key"));
	}

	@Test
	void getAllPropertiesFlattensCompositePropertySources() {
		CompositePropertySource composite = new CompositePropertySource("composite");
		composite.addPropertySource(new MapPropertySource("inner-a", Map.of("composite.key", "inner-a-value")));
		composite.addPropertySource(new MapPropertySource("inner-b", Map.of("composite.other", "inner-b-value")));

		Map<String, Object> result = EnvironmentHelper.getAllProperties(composite);

		assertEquals("inner-a-value", result.get("composite.key"));
		assertEquals("inner-b-value", result.get("composite.other"));
	}

	@Test
	void getAllPropertiesCompositeFirstNestedSourceWinsOnDuplicateKeys() {
		CompositePropertySource composite = new CompositePropertySource("composite");
		composite.addPropertySource(new MapPropertySource("inner-first", Map.of("dup.key", "first")));
		composite.addPropertySource(new MapPropertySource("inner-second", Map.of("dup.key", "second")));

		Map<String, Object> result = EnvironmentHelper.getAllProperties(composite);

		assertEquals("first", result.get("dup.key"));
	}

	@Test
	void getAllPropertiesIgnoresNonEnumerableSources() {
		PropertySource<Object> nonEnumerable = new PropertySource<>("opaque", new Object()) {
			@Override
			public Object getProperty(String name) {
				return "hidden";
			}
		};

		Map<String, Object> result = EnvironmentHelper.getAllProperties(nonEnumerable);

		assertTrue(result.isEmpty());
	}

	@Test
	void getPropertiesStartingWithFiltersByPrefix() {
		StandardEnvironment env = new StandardEnvironment();
		env.getPropertySources()
				.addFirst(new MapPropertySource(
						"source",
						Map.of(
								"hapi.fhir.fhir_version", "R4",
								"hapi.fhir.server_address", "http://localhost:8080/fhir",
								"spring.datasource.url", "jdbc:h2:mem:test")));

		Map<String, Object> result = EnvironmentHelper.getPropertiesStartingWith(env, "hapi.fhir");

		assertEquals(2, result.size());
		assertEquals("R4", result.get("hapi.fhir.fhir_version"));
		assertNull(result.get("spring.datasource.url"));
	}

	@Test
	void getPropertiesStartingWithReturnsEmptyMapWhenNoMatch() {
		StandardEnvironment env = new StandardEnvironment();
		env.getPropertySources().addFirst(new MapPropertySource("source", Map.of("other.key", "value")));

		Map<String, Object> result = EnvironmentHelper.getPropertiesStartingWith(env, "hapi.fhir");

		assertTrue(result.isEmpty());
	}

	@Test
	void getConfigurationBindsPropertiesToTargetClass() {
		StandardEnvironment env = new StandardEnvironment();
		env.getPropertySources()
				.addFirst(new MapPropertySource(
						"source", Map.of("hapi.fhir.server_address", "http://example.org/fhir")));
		ConditionContext context = mock(ConditionContext.class);
		when(context.getEnvironment()).thenReturn(env);

		AppProperties result = EnvironmentHelper.getConfiguration(context, "hapi.fhir", AppProperties.class);

		assertNotNull(result);
		assertEquals("http://example.org/fhir", result.getServer_address());
	}

	@Test
	void getConfigurationReturnsNullWhenNoPropertiesBound() {
		StandardEnvironment env = new StandardEnvironment();
		ConditionContext context = mock(ConditionContext.class);
		when(context.getEnvironment()).thenReturn(env);

		AppProperties result = EnvironmentHelper.getConfiguration(context, "hapi.fhir", AppProperties.class);

		assertNull(result);
	}
}
