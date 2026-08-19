package ca.uhn.fhir.jpa.starter.cdshooks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CdsHooksConfigConditionTest {

	private final CdsHooksConfigCondition myCondition = new CdsHooksConfigCondition();
	private final AnnotatedTypeMetadata myMetadata = mock(AnnotatedTypeMetadata.class);
	private ConditionContext myContext;
	private MockEnvironment myEnvironment;

	@BeforeEach
	void setUp() {
		myEnvironment = new MockEnvironment();
		myContext = mock(ConditionContext.class);
		when(myContext.getEnvironment()).thenReturn(myEnvironment);
	}

	@Test
	void matchesWhenEnabled() {
		myEnvironment.setProperty("hapi.fhir.cdshooks.enabled", "true");

		assertTrue(myCondition.matches(myContext, myMetadata));
	}

	@Test
	void matchesIsCaseInsensitive() {
		myEnvironment.setProperty("hapi.fhir.cdshooks.enabled", "TRUE");

		assertTrue(myCondition.matches(myContext, myMetadata));
	}

	@Test
	void doesNotMatchWhenDisabled() {
		myEnvironment.setProperty("hapi.fhir.cdshooks.enabled", "false");

		assertFalse(myCondition.matches(myContext, myMetadata));
	}

	@Test
	void doesNotMatchWhenPropertyMissing() {
		assertFalse(myCondition.matches(myContext, myMetadata));
	}

	@Test
	void doesNotMatchOnMalformedValue() {
		myEnvironment.setProperty("hapi.fhir.cdshooks.enabled", "yes");

		assertFalse(myCondition.matches(myContext, myMetadata));
	}
}
