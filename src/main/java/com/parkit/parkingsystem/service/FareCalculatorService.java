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

        final int MILLISECONDS_IN_SECOND = 1000;
        final int SECONDS_IN_MINUTES = 60;
        final int MINUTES_IN_HOUR = 60;
        final int MILLISECONDS_IN_HOUR = MILLISECONDS_IN_SECOND * SECONDS_IN_MINUTES * MINUTES_IN_HOUR;
        final double FREE_LIMIT_PARKING_TIME_IN_HOUR = 0.5;

        double durationInHours = (outHour - inHour) / MILLISECONDS_IN_HOUR;

        if (durationInHours < FREE_LIMIT_PARKING_TIME_IN_HOUR) {
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

    public void calculateFare(Ticket ticket, boolean discount) {

    }
}