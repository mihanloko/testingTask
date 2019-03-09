package com.loko.sbertask.web;

import org.springframework.web.bind.annotation.*;

import java.sql.*;

@RestController
public class MyController {
    private String url = "jdbc:mysql://localhost:3306/";
    private String dbName = "data";
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

        return restorePersonTable() && restorePurchaseTAble() && restoreStatusTAble();
    }

    private boolean restorePersonTable() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            Connection connection = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists personTable\n" +
                    "(\n" +
                    "  id         int auto_increment\n" +
                    "    primary key,\n" +
                    "  name       varchar(30) not null,\n" +
                    "  secondName varchar(30) not null,\n" +
                    "  login      varchar(30) not null,\n" +
                    "  pass       varchar(30) not null,\n" +
                    "  constraint personTable_login_uindex\n" +
                    "    unique (login)\n" +
                    ");\n");
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
            Connection connection = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists purchaseTable \n" +
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
            Connection connection = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists statusTable\n" +
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

    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public String registerNewUser(@RequestParam(value = "name") String nameParam,
                                  @RequestParam(value = "secondName") String secondNameParam,
                                  @RequestParam(value = "login") String loginParam,
                                  @RequestParam(value = "password") String passParam) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return "Driver error";
        }

        Connection connection;
        Statement statement;
        try {
            connection = DriverManager.getConnection(url + dbName, userName, password);
        } catch (SQLException e) {
            restoreDatabase();
            try {
                connection = DriverManager.getConnection(url + dbName, userName, password);
            } catch (SQLException e1) {
                e1.printStackTrace();
                return "SQL error";
            }
        }
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from personTable where login = \'"
            + loginParam + "\';");
            if (rs.next()) {
                rs.close();
                statement.close();
                connection.close();
                return "логин уже существует";
            }

            statement.executeUpdate("insert into personTable(name, secondName, login, pass) " +
                    "values (\'" + nameParam + "\', \'" + secondNameParam +
                    "\', \'" + loginParam + "\', \'" + passParam + "\');");

            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            restorePersonTable();
            return "SQL error";
        }


        return "Регистрация удачна";
    }

}
