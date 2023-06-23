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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static junit.framework.Assert.*;
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
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
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
        assertEquals("ABCDEF",ticket.getVehicleRegNumber());
        assertEquals(ParkingType.CAR ,ticket.getParkingSpot().getParkingType());
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
        assertTrue(consoleOutput.endsWith("Unable to update ticket information. Error occurred"));

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
        assertEquals(2, parkingSpot.getId());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Given
        when(inputReaderUtil.readSelection()).thenReturn(1); // Vehicle is a Car
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

        // When
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Then
        assertNull(parkingSpot);
    }

}
