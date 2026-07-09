package com.pvr.primenaturals.service;

import com.pvr.primenaturals.entity.ServiceablePincode;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.ServiceablePincodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
@Transactional
public class PincodeService {

    @Autowired
    private ServiceablePincodeRepository serviceablePincodeRepository;

    public Optional<ServiceablePincode> checkPincode(String pincode) {
        Optional<ServiceablePincode> dbResult = serviceablePincodeRepository.findByPincode(pincode);
        if (dbResult.isPresent()) {
            return dbResult;
        }
        // If not in database but is a valid 6-digit Indian pincode, return a default serviceable response
        if (pincode != null && pincode.matches("^[1-9][0-9]{5}$")) {
            ServiceablePincode defaultPincode = new ServiceablePincode();
            defaultPincode.setPincode(pincode);
            defaultPincode.setServiceable(true);
            defaultPincode.setEstimatedDays(5); // Default 5 days delivery for other regions
            defaultPincode.setDeliveryCharge(new java.math.BigDecimal("50.00")); // Standard delivery charge
            defaultPincode.setCodAvailable(true);
            return Optional.of(defaultPincode);
        }
        return Optional.empty();
    }

    public List<ServiceablePincode> getAllPincodes() {
        return serviceablePincodeRepository.findAll();
    }

    public ServiceablePincode addOrUpdatePincode(ServiceablePincode pincodeData) {
        Optional<ServiceablePincode> existing = serviceablePincodeRepository.findByPincode(pincodeData.getPincode());
        if (existing.isPresent()) {
            ServiceablePincode p = existing.get();
            p.setServiceable(pincodeData.isServiceable());
            p.setEstimatedDays(pincodeData.getEstimatedDays());
            p.setDeliveryCharge(pincodeData.getDeliveryCharge());
            p.setCodAvailable(pincodeData.isCodAvailable());
            return serviceablePincodeRepository.save(p);
        }
        return serviceablePincodeRepository.save(pincodeData);
    }

    public void deletePincode(Long id) {
        if (!serviceablePincodeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pincode not found with id " + id);
        }
        serviceablePincodeRepository.deleteById(id);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Optional<String> reverseGeocode(double latitude, double longitude) {
        try {
            org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(3000);
            factory.setReadTimeout(3000);
            
            RestTemplate restTemplate = new RestTemplate(factory);
            String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude + "&zoom=18&addressdetails=1";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "PvrPrimeNaturalsApp/1.0 (contact@pvrprimenaturals.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                if (body.containsKey("address")) {
                    Map<String, Object> address = (Map<String, Object>) body.get("address");
                    if (address.containsKey("postcode")) {
                        String postcode = String.valueOf(address.get("postcode"));
                        if (postcode != null) {
                            String cleaned = postcode.replaceAll("\\s+", "").replaceAll("-", "");
                            return Optional.of(cleaned);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reverse geocoding coordinates (" + latitude + ", " + longitude + "): " + e.getMessage());
        }
        return Optional.empty();
    }
}
