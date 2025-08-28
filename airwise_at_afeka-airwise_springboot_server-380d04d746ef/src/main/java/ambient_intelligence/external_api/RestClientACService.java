package ambient_intelligence.external_api;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import ambient_intelligence.logic.boundaries.ACResponse;
import ambient_intelligence.logic.exceptions.ExternalApiException;
import ambient_intelligence.logic.exceptions.InvalidRequestInputException;
import ambient_intelligence.logic.exceptions.ObjectNotFoundException;

import java.util.HashMap;
import java.util.Map;

@Service
public class RestClientACService {

    private final RestClient restClient;
    private final String baseUrl;

    public RestClientACService(RestClient restClient,
                               @Value("${external.api.demoac.base_url:http://localhost:3001/api/ac}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public ACResponse getACStateBySerial(String serial) {
        
    	try {
    		
            return restClient.get()
                    .uri(baseUrl + "/" + serial)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ACResponse.class);
            
        } catch (HttpClientErrorException.NotFound e) {
        	
            throw new ObjectNotFoundException("AC not found for serial: " + serial);
            
        } catch (Exception e) {
        	e.printStackTrace();
            throw new ExternalApiException("Error calling external API", e);
            
        }
    }

    public ACResponse setACState(String serial, boolean power, double temperature, String mode, String fanSpeed) {
        
    	Map<String, Object> body = new HashMap<>();
        
        body.put("power", power);
        body.put("temperature", temperature);
        body.put("mode", mode);
        body.put("fanSpeed", fanSpeed);

        try {
        	
            return restClient.post()
                    .uri(baseUrl + "/" + serial + "/set")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(ACResponse.class);
            
        } catch (HttpClientErrorException.BadRequest e) {
        	
            throw new InvalidRequestInputException("Invalid input: " + e.getResponseBodyAsString(), e);
            
        } catch (HttpClientErrorException.NotFound e) {
        	
            throw new ObjectNotFoundException("AC not found for serial: " + serial);
            
        } catch (Exception e) {
        	e.printStackTrace();
            throw new ExternalApiException("Error calling external API", e);
            
        }
    }
}
