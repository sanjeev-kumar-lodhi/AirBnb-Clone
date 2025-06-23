package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.entity.Booking;
import com.sanjeev.projects.airBnbApp.entity.User;
import com.sanjeev.projects.airBnbApp.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImp implements CheckoutService{

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            log.info("Creating session for booking with Id : {}",booking.getId());

            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();
            Customer customer = Customer.create(customerParams);

            SessionCreateParams sessionParam = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(failureUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getHotel().getName()+" : "+booking.getRoom().getType())
                                                                    .setDescription("Booking id : "+booking.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();
            Session session = Session.create(sessionParam);

            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);

            log.info("Session created successfully for booking with Id : {}",booking.getId());
            return session.getUrl();
        }
        catch (StripeException e){
            log.error("StripeException occurred while creating session", e);
            throw new RuntimeException("Failed to create Stripe session: " + e.getMessage(), e);
        }
    }
}
