 
package com.example.moviebooking;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {
    "http://localhost:5175",
    "http://127.0.0.1:5175",
    "http://localhost:5500",
    "http://127.0.0.1:5500"
})
public class BookingController {

    private final BookingRepository bookingRepository;

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Get all bookings
    @GetMapping
    public List<Booking> getBookings() {
        return bookingRepository.findAll();
    }

    // Get booked seats for a particular movie, date and show time
    @GetMapping("/seats")
    public Map<String, Object> getSeats(
            @RequestParam String movieName,
            @RequestParam LocalDate showDate,
            @RequestParam String showTime) {

        List<Booking> bookings = bookingRepository.findAll()
                .stream()
                .filter(b -> movieName.equals(b.getMovieName())
                        && showDate.equals(b.getShowDate())
                        && showTime.equals(b.getShowTime()))
                .collect(Collectors.toList());

        Set<String> bookedSeats = new HashSet<>();

        for (Booking booking : bookings) {
            if (booking.getSelectedSeats() != null
                    && !booking.getSelectedSeats().isBlank()) {
                bookedSeats.addAll(
                    Arrays.asList(
                        booking.getSelectedSeats().split(",")
                    )
                );
            }
        }

        List<String> allSeats = new ArrayList<>();

        for (char row = 'A'; row <= 'E'; row++) {
            for (int number = 1; number <= 8; number++) {
                allSeats.add(row + String.valueOf(number));
            }
        }

        return Map.of(
            "allSeats", allSeats,
            "bookedSeats", bookedSeats
        );
    }

    // Create a booking
    @PostMapping
    public Map<String, Object> createBooking(
            @RequestBody Booking booking) {

        if (booking.getCustomerName() == null
                || booking.getCustomerName().isBlank()
                || booking.getMovieName() == null
                || booking.getTickets() < 1
                || booking.getShowDate() == null
                || booking.getShowTime() == null
                || booking.getSelectedSeats() == null
                || booking.getSelectedSeats().isBlank()) {

            return Map.of(
                "error",
                "Please enter all booking details and select seats."
            );
        }

        List<String> selectedSeats = Arrays.stream(
                booking.getSelectedSeats().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (selectedSeats.size() != booking.getTickets()) {
            return Map.of(
                "error",
                "Please select exactly as many seats as tickets."
            );
        }

        Set<String> validSeats = new HashSet<>();

        for (char row = 'A'; row <= 'E'; row++) {
            for (int number = 1; number <= 8; number++) {
                validSeats.add(row + String.valueOf(number));
            }
        }

        if (!validSeats.containsAll(selectedSeats)) {
            return Map.of("error", "Invalid seat selected.");
        }

        BigDecimal price;

        switch (booking.getMovieName()) {
            case "Avengers Endgame":
                price = BigDecimal.valueOf(250);
                break;
            case "RRR":
                price = BigDecimal.valueOf(200);
                break;
            case "Pushpa 2":
                price = BigDecimal.valueOf(220);
                break;
            case "Devara":
                price = BigDecimal.valueOf(180);
                break;
            default:
                return Map.of("error", "Invalid movie selected.");
        }

        // Check whether selected seats are already booked
        List<Booking> existingBookings = bookingRepository.findAll();

        for (Booking existing : existingBookings) {
            boolean sameShow =
                    booking.getMovieName().equals(existing.getMovieName())
                    && booking.getShowDate().equals(existing.getShowDate())
                    && booking.getShowTime().equals(existing.getShowTime());

            if (sameShow && existing.getSelectedSeats() != null) {
                Set<String> alreadyBooked = Arrays.stream(
                        existing.getSelectedSeats().split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());

                for (String seat : selectedSeats) {
                    if (alreadyBooked.contains(seat)) {
                        return Map.of(
                            "error",
                            "Seat " + seat
                            + " is already booked. Please select another seat."
                        );
                    }
                }
            }
        }

        booking.setSelectedSeats(
            String.join(",", selectedSeats)
        );

        booking.setTotalAmount(
            price.multiply(BigDecimal.valueOf(booking.getTickets()))
        );

        Booking savedBooking = bookingRepository.save(booking);

        return Map.of(
            "message", "Booking successful!",
            "bookingId", savedBooking.getId(),
            "customerName", savedBooking.getCustomerName(),
            "movieName", savedBooking.getMovieName(),
            "tickets", savedBooking.getTickets(),
            "selectedSeats", savedBooking.getSelectedSeats(),
            "totalAmount", savedBooking.getTotalAmount()
        );
    }
}