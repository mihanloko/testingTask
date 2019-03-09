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

        return restorePersonTable() && restorePurchaseTAble();
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
            Connection connection = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table purchaseTable\n" +
                    "(\n" +
                    "  id          int auto_increment\n" +
                    "    primary key,\n" +
                    "  finishTime  date         not null,\n" +
                    "  nameOfThing varchar(100) not null,\n" +
                    "  price       double       null,\n" +
                    "  status      tinyint(1)   not null,\n" +
                    "  ownerId     int          not null,\n" +
                    "  isRegular   tinyint(1)   not null\n" +
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

    @RequestMapping(method = RequestMethod.POST, value = "/add")
    public String addNewPurchase(@RequestParam(value = "login") String loginParam,
                                  @RequestParam(value = "password") String passParam,
                                  @RequestParam(value = "time") String timeParam,
                                  @RequestParam(value = "price") String priceParam,
                                  @RequestParam(value = "type") String typeParam,
                                  @RequestParam(value = "name") String nameParam) {

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
                    + loginParam + "\' and pass = \'" +  passParam + "\';");
            if (!rs.next()) {
                rs.close();
                statement.close();
                connection.close();
                return "ошибка авторизации";
            }
            rs.close();

            System.out.println("insert into purchaseTable(finishtime, nameofthing, price, statusid, ownerid, isregular)" +
                    "values(\'" + timeParam + "\', \'" + nameParam +"\', \'" + priceParam + "\'," +
                    "\'" + "0" + "\', " +
                    "(select id from personTable where login = \'" + loginParam + "\')"
                    + ", \'" + typeParam + "\');");
            statement.executeUpdate("insert into purchaseTable(finishtime, nameofthing, price, status, ownerid, isregular)" +
                    "values(\'" + timeParam + "\', \'" + nameParam +"\', \'" + priceParam + "\'," +
                    "\'" + "0" + "\', " +
                    "(select id from personTable where login = \'" + loginParam + "\')"
                    + ", \'" + typeParam + "\');");

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            restorePurchaseTAble();
            restorePersonTable();
            return "SQL error";
        }

        return "Успешно добавлено";

    }

}
