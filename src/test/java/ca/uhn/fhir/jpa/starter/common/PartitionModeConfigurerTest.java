package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.interceptor.PatientIdPartitionInterceptor;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.partition.PartitionManagementProvider;
import ca.uhn.fhir.jpa.searchparam.extractor.ISearchParamExtractor;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.partition.RequestTenantPartitionInterceptor;
import ca.uhn.fhir.rest.server.tenant.UrlBaseTenantIdentificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartitionModeConfigurerTest {

	@Mock
	private ISearchParamExtractor mySearchParamExtractor;

	@Mock
	private PartitionSettings myPartitionSettings;

	@Mock
	private RestfulServer myRestfulServer;

	@Mock
	private PartitionManagementProvider myPartitionManagementProvider;

	@Mock
	private DaoRegistry myDaoRegistry;

	private AppProperties myAppProperties;
	private AppProperties.Partitioning myPartitioning;

	@BeforeEach
	void setUp() {
		myAppProperties = new AppProperties();
		myPartitioning = new AppProperties.Partitioning();
		myAppProperties.setPartitioning(myPartitioning);
		lenient().when(myRestfulServer.getFhirContext()).thenReturn(FhirContext.forR4Cached());
	}

	private PartitionModeConfigurer newConfigurer() {
		return new PartitionModeConfigurer(
				myAppProperties,
				mySearchParamExtractor,
				myPartitionSettings,
				myRestfulServer,
				myPartitionManagementProvider,
				myDaoRegistry);
	}

	@Test
	void patientIdModeRegistersPatientIdInterceptorAndEnablesUnnamedPartitions() {
		myPartitioning.setPatient_id_partitioning_mode(true);
		myPartitioning.setRequest_tenant_partitioning_mode(false);

		newConfigurer();

		verify(myRestfulServer).registerInterceptor(isA(PatientIdPartitionInterceptor.class));
		verify(myPartitionSettings).setUnnamedPartitionMode(true);
		verify(myRestfulServer, never()).setTenantIdentificationStrategy(any());
		verify(myRestfulServer).registerProviders(myPartitionManagementProvider);
	}

	@Test
	void requestTenantModeRegistersTenantInterceptorAndUrlStrategy() {
		myPartitioning.setPatient_id_partitioning_mode(false);
		myPartitioning.setRequest_tenant_partitioning_mode(true);

		newConfigurer();

		verify(myRestfulServer).registerInterceptor(isA(RequestTenantPartitionInterceptor.class));
		verify(myRestfulServer).setTenantIdentificationStrategy(isA(UrlBaseTenantIdentificationStrategy.class));
		verify(myPartitionSettings, never()).setUnnamedPartitionMode(true);
		verify(myRestfulServer).registerProviders(myPartitionManagementProvider);
	}

	@Test
	void patientIdModeTakesPrecedenceWhenBothModesEnabled() {
		myPartitioning.setPatient_id_partitioning_mode(true);
		myPartitioning.setRequest_tenant_partitioning_mode(true);

		newConfigurer();

		verify(myRestfulServer).registerInterceptor(isA(PatientIdPartitionInterceptor.class));
		verify(myRestfulServer, never()).setTenantIdentificationStrategy(any());
	}

	@Test
	void noInterceptorRegisteredWhenBothModesDisabled() {
		myPartitioning.setPatient_id_partitioning_mode(false);
		myPartitioning.setRequest_tenant_partitioning_mode(false);

		newConfigurer();

		verify(myRestfulServer, never()).registerInterceptor(any());
		verify(myRestfulServer, never()).setTenantIdentificationStrategy(any());
		verify(myPartitionSettings, never()).setUnnamedPartitionMode(true);
		verify(myRestfulServer).registerProviders(myPartitionManagementProvider);
	}
}
