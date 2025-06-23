package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
