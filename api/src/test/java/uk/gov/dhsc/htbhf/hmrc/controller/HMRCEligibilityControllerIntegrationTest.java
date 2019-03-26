package uk.gov.dhsc.htbhf.hmrc.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.dhsc.htbhf.errorhandler.ErrorResponse;
import uk.gov.dhsc.htbhf.hmrc.model.EligibilityRequest;
import uk.gov.dhsc.htbhf.hmrc.model.EligibilityResponse;
import uk.gov.dhsc.htbhf.hmrc.service.EligibilityService;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.dhsc.htbhf.assertions.IntegrationTestAssertions.assertValidationErrorInResponse;
import static uk.gov.dhsc.htbhf.hmrc.testhelper.EligibilityRequestTestFactory.anEligibilityRequest;
import static uk.gov.dhsc.htbhf.hmrc.testhelper.EligibilityRequestTestFactory.buildDefaultRequest;
import static uk.gov.dhsc.htbhf.hmrc.testhelper.EligibilityResponseTestFactory.anEligibilityResponse;
import static uk.gov.dhsc.htbhf.hmrc.testhelper.PersonTestFactory.aPersonWithNoNino;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HMRCEligibilityControllerIntegrationTest {

    private static final URI ENDPOINT = URI.create("/v1/hmrc/eligibility");

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private EligibilityService eligibilityService;

    @Test
    void shouldReturnEligibilityResponse() {
        EligibilityRequest eligibilityRequest = anEligibilityRequest();
        given(eligibilityService.checkEligibility(any())).willReturn(anEligibilityResponse());

        ResponseEntity<EligibilityResponse> response = restTemplate.postForEntity(ENDPOINT, eligibilityRequest, EligibilityResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(anEligibilityResponse());
        verify(eligibilityService).checkEligibility(eligibilityRequest);
    }

    @Test
    void shouldReturnBadRequestForInvalidEligibilityRequest() {
        EligibilityRequest request = buildDefaultRequest().person(aPersonWithNoNino()).build();

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity(ENDPOINT, request, ErrorResponse.class);

        assertValidationErrorInResponse(errorResponse, "person.nino", "must not be null");
    }

}
