package com.upgrad.FoodOrderingApp.service.constants;

public interface Messages {

    String SIGNUP_SUCCESS = "USER SUCCESSFULLY REGISTERED";
    String USR_SIGNIN_SUCCESS = "USER SUCCESSFULLY LOGGED IN";
    String ADD_UPDATE_SUCCESS = "ADDRESS SUCCESSFULLY REGISTERED";
    String ADD_DELETE_SUCCESS = "ADDRESS DELETED SUCCESSFULLY";
    String ORDER_SUCCESS = "ORDER SUCCESSFULLY PLACED";

    String DUPLICATE_CONTACT = "Try any other contact number, this contact number has already been taken";
    String INVALID_EMAIL = "Invalid email-id format!";
    String INVALID_CONTACT_NBR = "Invalid contact number!";
    String WEAK_PASSWORD = "Weak password!";
    String MISSING_EMAIL = "Missing email!";

    String CUST_NOT_SIGNED_IN = "Customer is not Logged in.";
    String CUST_SIGNED_OUT = "Customer is logged out. Log in again to access this endpoint.";
    String CUST_SESSION_EXPIRED = "Your session is expired. Log in again to access this endpoint.";


}
