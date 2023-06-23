package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        final int MILLISECONDS_IN_SECOND = 1000;
        final int SECONDS_IN_MINUTES = 60;
        final int MINUTES_IN_HOUR = 60;
        final int MILLISECONDS_IN_HOUR = MILLISECONDS_IN_SECOND * SECONDS_IN_MINUTES * MINUTES_IN_HOUR;

        double durationInHours = (outHour - inHour) / MILLISECONDS_IN_HOUR;

        if (durationInHours < 0.5) {
            ticket.setPrice(0);
        } else {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    double price3Decimals = (double)Math.round(durationInHours * Fare.CAR_RATE_PER_HOUR * 1000) / 1000;
                    ticket.setPrice(price3Decimals);
                    break;
                }
                case BIKE: {
                    double price3Decimals = (double)Math.round(durationInHours * Fare.BIKE_RATE_PER_HOUR * 1000) / 1000;
                    ticket.setPrice(price3Decimals);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }
}