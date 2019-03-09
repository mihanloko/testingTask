package com.loko.sbertask.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;

@RestController
public class MyController {
    private String url = "jdbc:mysql://localhost:3306/";
    private String userName = "loko";
    private String password = "rusakov";

    private boolean restoreDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            Connection connection = DriverManager.getConnection(url, userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create database data");
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return restorePersonTAble() && restorePurchaseTAble() && restoreStatusTAble();
    }

    private boolean restorePersonTAble() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            Connection connection = DriverManager.getConnection(url + "data", userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table personTable\n" +
                    "(\n" +
                    "\tid int auto_increment,\n" +
                    "\tname varchar(30) not null,\n" +
                    "\tsecondName varchar(30) not null,\n" +
                    "\tlogin varchar(30) not null,\n" +
                    "\tpass varchar(30) not null,\n" +
                    "\tconstraint personTable_pk\n" +
                    "\t\tprimary key (id)\n" +
                    ");");
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean restorePurchaseTAble() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            Connection connection = DriverManager.getConnection(url + "data", userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table purchaseTable\n" +
                    "(\n" +
                    "\tid int auto_increment,\n" +
                    "\tfinishTime datetime not null,\n" +
                    "\tnameOfThing varchar(100) not null,\n" +
                    "\tprice double null,\n" +
                    "\tstatusId int not null,\n" +
                    "\townerId int not null,\n" +
                    "\tisRegular bool not null,\n" +
                    "\tconstraint purchaseTable_pk\n" +
                    "\t\tprimary key (id)\n" +
                    ");\n" +
                    "\n");
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean restoreStatusTAble() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            Connection connection = DriverManager.getConnection(url + "data", userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table statusTable\n" +
                    "(\n" +
                    "\tid int auto_increment,\n" +
                    "\tname varchar(30) not null,\n" +
                    "\tconstraint statusTable_pk\n" +
                    "\t\tprimary key (id)\n" +
                    ");");
            statement.executeUpdate("insert into statusTable (name) values ('в процессе')");
            statement.executeUpdate("insert into statusTable (name) values ('выполнено')");
            statement.executeUpdate("insert into statusTable (name) values ('просрочено')");
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



}
