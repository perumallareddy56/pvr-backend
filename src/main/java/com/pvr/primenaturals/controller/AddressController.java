package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.dto.request.AddressRequest;
import com.pvr.primenaturals.dto.response.AddressResponseDTO;
import com.pvr.primenaturals.entity.Address;
import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.AddressRepository;
import com.pvr.primenaturals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/addresses")
@PreAuthorize("isAuthenticated()")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    private AddressResponseDTO mapToDTO(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .streetAddress(address.getStreetAddress())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .phoneNumber(address.getPhoneNumber())
                .addressLabel(address.getAddressLabel())
                .isDefault(address.isDefault())
                .build();
    }

    @GetMapping
    public List<AddressResponseDTO> getUserAddresses(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public AddressResponseDTO saveAddress(Authentication authentication, @RequestBody AddressRequest request) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(addr -> {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .streetAddress(request.getStreetAddress())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .phoneNumber(request.getPhoneNumber())
                .addressLabel(request.getAddressLabel())
                .isDefault(request.isDefault())
                .build();

        return mapToDTO(addressRepository.save(address));
    }

    @PutMapping("/{id}")
    public AddressResponseDTO updateAddress(Authentication authentication, @PathVariable Long id, @RequestBody AddressRequest request) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to modify this address");
        }

        if (request.isDefault() && !address.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(addr -> {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    });
        }

        address.setReceiverName(request.getReceiverName());
        address.setStreetAddress(request.getStreetAddress());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLabel(request.getAddressLabel());
        address.setDefault(request.isDefault());

        return mapToDTO(addressRepository.save(address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(Authentication authentication, @PathVariable Long id) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this address");
        }

        addressRepository.delete(address);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/default")
    public AddressResponseDTO setDefaultAddress(Authentication authentication, @PathVariable Long id) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to modify this address");
        }

        addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .ifPresent(addr -> {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                });

        address.setDefault(true);
        return mapToDTO(addressRepository.save(address));
    }
}
