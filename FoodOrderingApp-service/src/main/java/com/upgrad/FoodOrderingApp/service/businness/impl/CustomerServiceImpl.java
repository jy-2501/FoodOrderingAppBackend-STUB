package com.upgrad.FoodOrderingApp.service.businness.impl;

import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.JwtTokenProvider;
import com.upgrad.FoodOrderingApp.service.constants.MessageCodes;
import com.upgrad.FoodOrderingApp.service.constants.Messages;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAuthRepository;
import com.upgrad.FoodOrderingApp.service.dao.CustomerRepository;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import static java.util.Base64.getDecoder;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerAuthRepository customerAuthRepository;

    public CustomerEntity getCustomer(String accessToken) throws AuthorizationFailedException {
        return getCustomerAuth(accessToken).getCustomer();
    }

    private CustomerAuthEntity getCustomerAuth(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity custAuth = customerAuthRepository.findByAccessToken(accessToken);
        if(null == custAuth) {
            throw new AuthorizationFailedException(MessageCodes.ATHR_001, Messages.CUST_NOT_SIGNED_IN);
        }
        if(null == custAuth.getLogoutAt()) {
            throw new AuthorizationFailedException(MessageCodes.ATHR_002, Messages.CUST_SIGNED_OUT);
        }
        if(custAuth.getExpiresAt().compareTo(ZonedDateTime.now()) <= 0) {
            throw new AuthorizationFailedException(MessageCodes.ATHR_003, Messages.CUST_SESSION_EXPIRED);
        }
        return custAuth;
    }

    public CustomerEntity saveCustomer(CustomerEntity customer) throws SignUpRestrictedException {
        if(StringUtils.isEmpty(customer.getEmailAddress())) {
            throw new SignUpRestrictedException(MessageCodes.MISSING_EMAIL, Messages.MISSING_EMAIL);
        }
        if(isValidEmail(customer.getEmailAddress())) {
            throw new SignUpRestrictedException(MessageCodes.INVALID_EMAIL, Messages.INVALID_EMAIL);
        }
        if(StringUtils.isEmpty(customer.getContactNumber()) || customer.getContactNumber().length() != 10) {
            throw new SignUpRestrictedException(MessageCodes.INVALID_CONTACT_NBR, Messages.INVALID_CONTACT_NBR);
        }
        if(StringUtils.isEmpty(customer.getPassword()) || customer.getPassword().length() < 6) {
            throw new SignUpRestrictedException(MessageCodes.WEAK_PASSWORD, Messages.WEAK_PASSWORD);
        }
        if(null != customerRepository.findByContactNumber(customer.getContactNumber())) {
            throw new SignUpRestrictedException(MessageCodes.DUPLICATE_CONTACT, Messages.DUPLICATE_CONTACT);
        }
        return customerRepository.save(customer);
    }

    private boolean isValidEmail(String email) {
        return true;
    }

    /* public CustomerEntity saveCustomer()*/

    public CustomerAuthEntity authenticate(String contact, String encodedPwd) throws AuthenticationFailedException {
        if(StringUtils.isEmpty(contact) || StringUtils.isEmpty(encodedPwd)) {
            throw new AuthenticationFailedException(MessageCodes.INVALID_AUTH, "");
        }
        CustomerEntity cust = customerRepository.findByContactNumber(contact);
        if(null == cust) {
            throw new AuthenticationFailedException(MessageCodes.AUTH_NO_CONTACT, "");
        }
        if(!cust.getPassword().equals(encodedPwd)) {
            throw new AuthenticationFailedException(MessageCodes.AUTH_WRONG_PASSWORD, "");
        }


        final ZonedDateTime issuedDateTime = ZonedDateTime.now();
        final ZonedDateTime expiresDateTime = ZonedDateTime.now().plusMinutes(10);

        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encodedPwd);
        String token = jwtTokenProvider.generateToken(cust.getUuid(), issuedDateTime, expiresDateTime);

        CustomerAuthEntity auth = new CustomerAuthEntity();

        auth.setAccessToken(token);
        auth.setExpiresAt(expiresDateTime);
        auth.setLoginAt(issuedDateTime);
        auth.setUuid(cust.getUuid());
        auth.setCustomer(cust);
        auth = customerAuthRepository.save(auth);

        return auth;
    }

    public CustomerEntity updateCustomer(CustomerEntity customer) throws AuthorizationFailedException {
        return customerRepository.save(customer);
    }

    public CustomerAuthEntity logout(String accessToken) throws AuthorizationFailedException {

        CustomerAuthEntity custAuth = getCustomerAuth(accessToken);
        custAuth.setLogoutAt(ZonedDateTime.now());
        custAuth = customerAuthRepository.save(custAuth);
        return custAuth;
    }

    public CustomerEntity updateCustomerPassword(String oldPwd, String newPwd, CustomerEntity customer) throws UpdateCustomerException {

        if(oldPwd.length() < 6) {
            throw new UpdateCustomerException(MessageCodes.UCR_001, Messages.WEAK_PASSWORD);
        }
        customer.setPassword(newPwd);
        customer = customerRepository.save(customer);
        return customer;
    }


    private String[] getCustomerAuthCreds(String authorization) {
        String base64Credentials = authorization.substring("Basic".length()).trim();
        byte[] credDecoded = getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        // credentials = username:password
        return credentials.split(":", 2);
    }

}
