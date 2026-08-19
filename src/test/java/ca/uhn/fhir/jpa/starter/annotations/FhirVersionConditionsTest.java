package ca.uhn.fhir.jpa.starter.annotations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FhirVersionConditionsTest {

	private ConditionContext myContext;
	private MockEnvironment myEnvironment;
	private final AnnotatedTypeMetadata myMetadata = mock(AnnotatedTypeMetadata.class);

	@BeforeEach
	void setUp() {
		myEnvironment = new MockEnvironment();
		myContext = mock(ConditionContext.class);
		when(myContext.getEnvironment()).thenReturn(myEnvironment);
	}

	private boolean matches(Condition theCondition, String theVersion) {
		if (theVersion != null) {
			myEnvironment.setProperty("hapi.fhir.fhir_version", theVersion);
		}
		return theCondition.matches(myContext, myMetadata);
	}

	@Test
	void onR4ConditionMatchesR4() {
		assertTrue(matches(new OnR4Condition(), "R4"));
	}

	@Test
	void onR4ConditionMatchesLowercaseVersionString() {
		assertTrue(matches(new OnR4Condition(), "r4"));
	}

	@Test
	void onR4ConditionRejectsOtherVersions() {
		assertFalse(matches(new OnR4Condition(), "R5"));
		myEnvironment.setProperty("hapi.fhir.fhir_version", "DSTU3");
		assertFalse(new OnR4Condition().matches(myContext, myMetadata));
	}

	@Test
	void onR4ConditionRejectsUnknownVersionString() {
		assertFalse(matches(new OnR4Condition(), "NOT_A_VERSION"));
	}

	@Test
	void onR4ConditionThrowsWhenVersionPropertyMissing() {
		// Documents current failure mode: a missing hapi.fhir.fhir_version property
		// causes an NPE rather than a clean non-match
		assertThrows(NullPointerException.class, () -> new OnR4Condition().matches(myContext, myMetadata));
	}

	@Test
	void onR5ConditionMatchesOnlyR5() {
		assertTrue(matches(new OnR5Condition(), "R5"));
		myEnvironment.setProperty("hapi.fhir.fhir_version", "R4");
		assertFalse(new OnR5Condition().matches(myContext, myMetadata));
	}

	@Test
	void onR4BConditionMatchesOnlyR4B() {
		assertTrue(matches(new OnR4BCondition(), "R4B"));
		myEnvironment.setProperty("hapi.fhir.fhir_version", "R4");
		assertFalse(new OnR4BCondition().matches(myContext, myMetadata));
	}

	@Test
	void onDstu2ConditionMatchesOnlyDstu2() {
		assertTrue(matches(new OnDSTU2Condition(), "DSTU2"));
		myEnvironment.setProperty("hapi.fhir.fhir_version", "DSTU3");
		assertFalse(new OnDSTU2Condition().matches(myContext, myMetadata));
	}

	@Test
	void onDstu3ConditionMatchesOnlyDstu3() {
		assertTrue(matches(new OnDSTU3Condition(), "DSTU3"));
		myEnvironment.setProperty("hapi.fhir.fhir_version", "DSTU2");
		assertFalse(new OnDSTU3Condition().matches(myContext, myMetadata));
	}
}
