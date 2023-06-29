package com.parkit.parkingsystem.unittest;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.util.NumberUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        fareCalculatorService.calculateFare(ticket);

        // Then
        assertThat(ticket.getPrice()).isEqualTo(NumberUtil.roundDoubleToNDecimals(Fare.CAR_RATE_PER_HOUR, 3));

    }

    @Test
    public void calculateFareBike(){
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        fareCalculatorService.calculateFare(ticket);

        // Then
        assertThat(ticket.getPrice()).isEqualTo(NumberUtil.roundDoubleToNDecimals(Fare.BIKE_RATE_PER_HOUR, 3));
    }

    @Test
    public void calculateFareUnknownType() {
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        Throwable thrown = catchThrowable(() -> fareCalculatorService.calculateFare(ticket));

        // Then
        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        Throwable thrown = catchThrowable(() -> fareCalculatorService.calculateFare(ticket));

        // Then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        fareCalculatorService.calculateFare(ticket);

        // Then
        double expectedPrice = NumberUtil.roundDoubleToNDecimals(0.75 * Fare.BIKE_RATE_PER_HOUR, 3);
        assertThat(ticket.getPrice()).isEqualTo(expectedPrice);
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        fareCalculatorService.calculateFare(ticket);

        // Then
        double expectedPrice = NumberUtil.roundDoubleToNDecimals(0.75 * Fare.CAR_RATE_PER_HOUR, 3);
        assertThat(ticket.getPrice()).isEqualTo(expectedPrice);
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        fareCalculatorService.calculateFare(ticket);

        // Then
        double expectedPrice = NumberUtil.roundDoubleToNDecimals(24 * Fare.CAR_RATE_PER_HOUR, 3);
        assertThat(ticket.getPrice()).isEqualTo(expectedPrice);
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        // Given
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000)); //29 minutes parking time should be free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        fareCalculatorService.calculateFare(ticket);

        // Then
        assertThat(ticket.getPrice()).isEqualTo(0);
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        // Given
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000)); //29 minutes parking time should be free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // When
        fareCalculatorService.calculateFare(ticket);

        // Then
        assertThat(ticket.getPrice()).isEqualTo(0);
    }

    @Test
    public void calculateFareCarWithDiscount() {
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        boolean discount = true;

        // When
        fareCalculatorService.calculateFare(ticket, discount);

        // Then
        double priceWithDiscount = Fare.CAR_RATE_PER_HOUR * 0.95;
        assertThat(ticket.getPrice()).isEqualTo(NumberUtil.roundDoubleToNDecimals(priceWithDiscount, 3));
    }

    @Test
    public void calculateFareBikeWithDiscount() {
        // Given
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        boolean discount = true;

        // When
        fareCalculatorService.calculateFare(ticket, discount);

        // Then
        double priceWithDiscount = Fare.BIKE_RATE_PER_HOUR * 0.95;
        assertThat(ticket.getPrice()).isEqualTo(NumberUtil.roundDoubleToNDecimals(priceWithDiscount, 3));
    }

}
