package com.parkit.parkingsystem.unittest;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static junit.framework.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    private static Ticket ticket;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception{
        // Given
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // When
        parkingService.processExitingVehicle();

        // Then
        assertThat(ticket.getPrice()).isEqualTo(Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void processIncomingBikeTest() throws Exception {
        // Given
        ParkingSpot parkingSpotBike = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setParkingSpot(parkingSpotBike);
        when(inputReaderUtil.readSelection()).thenReturn(2); // Vehicle is a Bike
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // When
        parkingService.processIncomingVehicle();

        // Then
        assertThat(ticket.getParkingSpot().getParkingType()).isEqualTo(ParkingType.BIKE);

    }

    @Test
    public void processIncomingVehicle() throws Exception {
        // Given
        when(inputReaderUtil.readSelection()).thenReturn(1); // Vehicle is a Car
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // When
        parkingService.processIncomingVehicle();

        // Then
        assertThat(ticket.getVehicleRegNumber()).isEqualTo("ABCDEF");
        assertThat(ticket.getParkingSpot().getParkingType()).isEqualTo(ParkingType.CAR);
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        // Specific setup test
        System.setOut(new PrintStream(outputStream));

        // Given
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false); // Enable to update ticket
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // When
        parkingService.processExitingVehicle();
        String consoleOutput = outputStream.toString().trim();

        // Then
        assertThat(consoleOutput.endsWith("Unable to update ticket information. Error occurred")).isTrue();

        // Specific tear down
        System.setOut(originalOut);
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        // Given
        when(inputReaderUtil.readSelection()).thenReturn(1); // Vehicle is a Car
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2); // slot 2 is returned from the data base

        // When
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Then
        assertThat(parkingSpot.getId()).isEqualTo(2);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Given
        when(inputReaderUtil.readSelection()).thenReturn(1); // Vehicle is a Car
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

        // When
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Then
        assertThat(parkingSpot).isNull();
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // Given
        when(inputReaderUtil.readSelection()).thenReturn(3); // Wrong argument

        // When
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Then
        assertThat(parkingSpot).isNull();
    }

    @Test
    public void testWhenIncomingVehicleWithDiscount_thenShowWelcomeMessage() throws Exception {
        // Specific setup test
        System.setOut(new PrintStream(outputStream));

        // Given
        when(inputReaderUtil.readSelection()).thenReturn(1); // Vehicle is a Car
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getNbTicket(any(Ticket.class))).thenReturn(2);

        // When
        parkingService.processIncomingVehicle();
        String consoleOutput = outputStream.toString().trim();

        // Then
        String welcomeMessage = "Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%";
        assertThat(consoleOutput.endsWith(welcomeMessage)).isTrue();

        // Specific tear down
        System.setOut(originalOut);
    }

    @Test
    public void testWhenExitingVehicleWithDiscount_thenTicketPriceShouldBeWithDiscount() throws Exception {
        // Given
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getNbTicket(any(Ticket.class))).thenReturn(2);

        // When
        parkingService.processExitingVehicle();

        // Then
        double priceWithDiscount = (double) Math.round(Fare.CAR_RATE_PER_HOUR * 0.95 * 1000) / 1000;
        assertThat(ticket.getPrice()).isEqualTo(priceWithDiscount);
    }

}
