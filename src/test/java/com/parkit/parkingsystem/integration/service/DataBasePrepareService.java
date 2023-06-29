package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();

            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }

    public void addTwoTicketsForRegABCDEF() {
        Connection connection = null;
        try {
            connection = dataBaseTestConfig.getConnection();
            connection.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(1,\"ABCDEF\",1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)").execute();
            connection.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(1,\"ABCDEF\",1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)").execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(connection);
        }

    }

    public void removeOneHourFormInTimeTicket(Ticket ticket) {
        Connection connection = null;
        try {
            connection = dataBaseTestConfig.getConnection();
            PreparedStatement ps = connection.prepareStatement("update ticket set IN_TIME = ? where ID = ?");
            ps.setTimestamp(1, new Timestamp(ticket.getInTime().getTime() - 60 * 60 * 1000));
            ps.setInt(2,ticket.getId());
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }


}
