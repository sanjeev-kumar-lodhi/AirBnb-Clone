package com.sanjeev.projects.airBnbApp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanjeev.projects.airBnbApp.dto.BookingDto;
import com.sanjeev.projects.airBnbApp.dto.BookingRequest;
import com.sanjeev.projects.airBnbApp.dto.GuestDto;
import com.sanjeev.projects.airBnbApp.dto.HotelReportDto;
import com.sanjeev.projects.airBnbApp.entity.*;
import com.sanjeev.projects.airBnbApp.entity.enums.BookingStatus;
import com.sanjeev.projects.airBnbApp.exceptions.ResourceNotFoundException;
import com.sanjeev.projects.airBnbApp.repository.*;
import com.sanjeev.projects.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.sanjeev.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImp implements BookingService{
    private final GuestRepository guestRepository;
    private final InventoryRepository inventoryRepository;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {

        log.info("Initialising booking for hotel : {}, room {}, date {}-{} ",bookingRequest.getHotelId()

        ,bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).
                orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID : "+ bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId()).
                orElseThrow(()-> new ResourceNotFoundException("Room not found with ID : "+bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

        long dateCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;

        if(inventoryList.size() != dateCount){
            throw new IllegalStateException("Room is not available anymore ");
        }

        // reserve the room/ update the bookedCount of inventories

        inventoryRepository.initBooking(bookingRequest.getRoomId(),bookingRequest.getCheckInDate()
        ,bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice=priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        // create the Booking



        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .roomsCount(bookingRequest.getRoomsCount())
                .user(getCurrentUser())
                .amount(totalPrice)
                .build();
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {

        log.info("Adding guests for Booking with Id : "+bookingId);
        Booking booking = bookingRepository.findById(bookingId).
                orElseThrow(()-> new ResourceNotFoundException("Booking not found with id  :"+bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
                throw new AccessDeniedException("Booking does not belong to this user with id : "+booking.getUser().getId());
        }

        if(hasBookingExpired(booking)){
             throw new IllegalStateException("Booking has already expired ");
        }
        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state ,cannot add guests ");
        }
        for (GuestDto guestDto:guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuest().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).
                orElseThrow(()-> new ResourceNotFoundException("Booking not found with id  :"+bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new AccessDeniedException("Booking does not belong to this user with id : "+booking.getUser().getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired ");
        }
        String sessionURL = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payment/success",frontendUrl+"/payment/failure");

        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);
        return sessionURL;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {

        if("checkout.session.completed".equals(event.getType())){


//            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            Session session = retrieveSessionFromEvent(event);
                if(session == null|| session.getId() == null)return;

                String sessionId = session.getId();
                Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(
                        ()-> new ResourceNotFoundException("Booking not found for session Id :"+sessionId)
                );

                booking.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate()
                        ,booking.getCheckOutDate(),booking.getRoomsCount());
                inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate()
                        ,booking.getCheckOutDate(),booking.getRoomsCount());

                log.info("Successfully confirmed the booking for Booking Id : {}",booking.getId());
        }
        else {
            log.warn("Unhandled event Type : {}",event.getType());
        }

    }



    private Session retrieveSessionFromEvent(Event event) {
        log.info("inside  retrieveSessionFromEvent");
        try {

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isPresent()) {
                return (Session) deserializer.getObject().get();
            } else {
                String rawJson = event.getData().getObject().toJson();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(rawJson);
                String sessionId = jsonNode.get("id").asText();

                return Session.retrieve(sessionId);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to retrieve session data");
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).
                orElseThrow(()-> new ResourceNotFoundException("Booking not found with id  :"+bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new AccessDeniedException("Booking does not belong to this user with id : "+booking.getUser().getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only Confirmed booking can be canceled ");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate()
                ,booking.getCheckOutDate(),booking.getRoomsCount());
        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate()
                ,booking.getCheckOutDate(),booking.getRoomsCount());

        // handle the refund
        try {

            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundCreateParams);
        }
        catch (StripeException e){
            throw new RuntimeException(e);
        }
        log.info("Payment has successfully refunded ");

    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).
                orElseThrow(()-> new ResourceNotFoundException("Booking not found with id  :"+bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new AccessDeniedException("Booking does not belong to this user with id : "+booking.getUser().getId());
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with Id :"+hotelId));

        User user=getCurrentUser();

        log.info("Getting all booking for the hotel with Id : {}",hotelId);

        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("You are not the owner of hotel with ID : "+hotelId);
        }

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings.stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());

    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with Id :"+hotelId));

        User user = getCurrentUser();

        log.info("Generating the report  for hotel with Id : {}",hotelId);

        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("You are not the owner of hotel with ID : "+hotelId);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking>  bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);

        Long totalConfirmedBooking=bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBooking == 0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBooking), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBooking,totalRevenueOfConfirmedBookings,avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findByUser(user)
                .stream()
                .map(element-> modelMapper.map(element,BookingDto.class))
                .collect(Collectors.toList());
    }

//    @Scheduled(cron = "*/1 * * * * *  ")
//    public Void resetBookings(){
//        User user = getCuurentUser();
//        user.get
//    }





    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
