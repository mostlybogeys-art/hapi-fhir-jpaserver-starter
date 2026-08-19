package ca.uhn.fhir.jpa.starter.annotations;

import ca.uhn.fhir.jpa.starter.common.OnPartitionModeEnabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppPropertiesConditionsTest {

	private ConditionContext myContext;
	private MockEnvironment myEnvironment;
	private final AnnotatedTypeMetadata myMetadata = mock(AnnotatedTypeMetadata.class);

	@BeforeEach
	void setUp() {
		myEnvironment = new MockEnvironment();
		myContext = mock(ConditionContext.class);
		when(myContext.getEnvironment()).thenReturn(myEnvironment);
	}

	@Test
	void onCorsPresentFalseWhenNoHapiFhirPropertiesBound() {
		assertFalse(new OnCorsPresent().matches(myContext, myMetadata));
	}

	@Test
	void onCorsPresentFalseWhenCorsNotConfigured() {
		myEnvironment.setProperty("hapi.fhir.fhir_version", "R4");

		assertFalse(new OnCorsPresent().matches(myContext, myMetadata));
	}

	@Test
	void onCorsPresentTrueWhenCorsConfigured() {
		myEnvironment.setProperty("hapi.fhir.cors.allow_Credentials", "true");

		assertTrue(new OnCorsPresent().matches(myContext, myMetadata));
	}

	@Test
	void onImplementationGuidesPresentFalseWhenNoHapiFhirPropertiesBound() {
		assertFalse(new OnImplementationGuidesPresent().matches(myContext, myMetadata));
	}

	@Test
	void onImplementationGuidesPresentFalseWhenNoGuidesConfigured() {
		myEnvironment.setProperty("hapi.fhir.fhir_version", "R4");

		assertFalse(new OnImplementationGuidesPresent().matches(myContext, myMetadata));
	}

	@Test
	void onImplementationGuidesPresentTrueWhenGuideConfigured() {
		myEnvironment.setProperty("hapi.fhir.implementationguides.fhir_r4_core.name", "hl7.fhir.r4.core");
		myEnvironment.setProperty("hapi.fhir.implementationguides.fhir_r4_core.version", "4.0.1");

		assertTrue(new OnImplementationGuidesPresent().matches(myContext, myMetadata));
	}

	@Test
	void onPartitionModeEnabledFalseWhenNoHapiFhirPropertiesBound() {
		assertFalse(new OnPartitionModeEnabled().matches(myContext, myMetadata));
	}

	@Test
	void onPartitionModeEnabledFalseWhenPartitioningNotConfigured() {
		myEnvironment.setProperty("hapi.fhir.fhir_version", "R4");

		assertFalse(new OnPartitionModeEnabled().matches(myContext, myMetadata));
	}

	@Test
	void onPartitionModeEnabledTrueWhenPartitioningConfigured() {
		myEnvironment.setProperty("hapi.fhir.partitioning.default_partition_id", "0");

		assertTrue(new OnPartitionModeEnabled().matches(myContext, myMetadata));
	}
}
