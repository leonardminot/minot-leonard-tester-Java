package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.util.NumberUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static  Date testStartingTime;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();

        testStartingTime = new Date();
        long updateStartingTime = testStartingTime.getTime() - 1000;
        testStartingTime = new Date(updateStartingTime);
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        // Given

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // When
        parkingService.processIncomingVehicle();
        Date testEndingDate = new Date();

        // Then
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertThat(ticket.getInTime()).isBetween(testStartingTime, testEndingDate);
    }

    @Test
    public void testParkingLotExit(){
        // Given
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        dataBasePrepareService.removeOneHourFormInTimeTicket(ticket); // Remove 1 hour to be able to compute the fare

        // When
        parkingService.processExitingVehicle();
        Date testEndingDate = new Date();
        ticket = ticketDAO.getTicket("ABCDEF");

        // Then
        double expectedTicketPrice = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (double)(1000 * 60 * 60) * Fare.CAR_RATE_PER_HOUR;
        assertThat(ticket.getOutTime()).isBetween(testStartingTime, testEndingDate);
        assertThat(ticket.getPrice()).isEqualTo(NumberUtil.roundDoubleToNDecimals(expectedTicketPrice, 3));
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        // Given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle(); // First entrance
        parkingService.processExitingVehicle(); // First Exit
        parkingService.processIncomingVehicle(); // Second entrance
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        dataBasePrepareService.removeOneHourFormInTimeTicket(ticket); // Remove 1 hour to be able to compute the fare

        // When
        parkingService.processExitingVehicle(); // Second exit : should have 5% discount
        ticket = ticketDAO.getTicket("ABCDEF");

        // Then
        double expectedPrice = (ticket.getOutTime().getTime() - ticket.getInTime().getTime() )/ (double) (1000 * 60 * 60) * Fare.CAR_RATE_PER_HOUR * 0.95;
        assertThat(ticket.getPrice()).isEqualTo(NumberUtil.roundDoubleToNDecimals(expectedPrice, 3));
    }

}
