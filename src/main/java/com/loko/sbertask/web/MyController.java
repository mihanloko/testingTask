package com.loko.sbertask.web;

import org.springframework.web.bind.annotation.*;

import java.sql.*;

@RestController
public class MyController {
    private String url = "jdbc:mysql://localhost:3306/";
    private String dbName = "data";
    private String userName = "loko";
    private String password = "rusakov";
    private String[] headers = {"Название", "Цена", "Количество", "Запланированное время", "Периодичность дней"};
    private String[] labels = {"nameOfThing", "price", "amount", "finishTime", "isRegular"};

    private boolean restoreDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            Connection connection = DriverManager.getConnection(url, userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create database data");
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return restorePersonTable() && restorePurchaseTAble();
    }

    private boolean restorePersonTable() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean restorePurchaseTAble() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
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
                    "  isRegular   int          not null,\n" +
                    "  amount      int          not null\n" +
                    ");");
            statement.close();
            connection.close();
        } catch (SQLException e) {
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
                                 @RequestParam(value = "amount") String amountParam,
                                 @RequestParam(value = "regular", required = false, defaultValue = "0") String regularParam,
                                 @RequestParam(value = "name") String nameParam) {

        

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
                    + loginParam + "\' and pass = \'" + passParam + "\';");
            if (!rs.next()) {
                rs.close();
                statement.close();
                connection.close();
                return "ошибка авторизации";
            }
            rs.close();

            rs = statement.executeQuery("select * from purchaseTable " +
                    "where nameOfThing = \'"+nameParam+"\' " +
                    "and finishTime = \'" + timeParam + "\' " +
                    "and isRegular = \'" + regularParam +" \' " +
                    "and price = \'" + priceParam + "\' " +
                    "and (select id from personTable where login = \'" + loginParam + "\') = ownerId" +
                    " and status = 1;");

            if (rs.next()) {
                statement.executeUpdate("update purchaseTable " +
                        "set amount = amount + \'" + amountParam + "\'" +
                        "where id = " + rs.getString("id") + ";");
            }
            else {
                statement.executeUpdate("insert into purchaseTable(finishtime, nameofthing, price, status, ownerid, isregular, amount)" +
                        "values(\'" + timeParam + "\', \'" + nameParam + "\', \'" + priceParam + "\'," +
                        "\'" + "1" + "\', " +
                        "(select id from personTable where login = \'" + loginParam + "\')"
                        + ", \'" + regularParam + "\', \'" + amountParam + "\');");
            }

            rs.close();
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

    @RequestMapping(method = RequestMethod.POST, value = "/get")
    public String getAllPurchases(@RequestParam(value = "login") String loginParam,
                                  @RequestParam(value = "password") String passParam) {
        

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

        StringBuilder result = new StringBuilder();
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from personTable where login = \'"
                    + loginParam + "\' and pass = \'" + passParam + "\';");
            if (!rs.next()) {
                rs.close();
                statement.close();
                connection.close();
                return "ошибка авторизации";
            }
            rs.close();

            rs = statement.executeQuery("select * from purchaseTable where now() <= finishTime and status = true " +
                    "and (select id from personTable where login = \'" + loginParam + "\') = ownerId;");
            result.append("<center><h1>Актуальные покупки</h1></center>");
            result.append("<table align = \"center\"><tr>");
            int columns = headers.length;
            for (int i = 0; i < columns; i++)
                result.append("<td>").append(headers[i]).append("</td>");
            result.append("</tr>");
            while (rs.next()) {
                result.append("<tr>");
                for (int i = 0; i < columns; i++)
                    result.append("<td>").append(rs.getString(labels[i])).append("</td>");
                /*String reg = rs.getString(labels[labels.length - 1]);
                if (reg.equals("1"))
                    result.append("<td>Да</td>");
                else
                    result.append("<td>Нет</td>");*/
                result.append("</tr>");
            }
            result.append("</table>");
            rs.close();

            result.append("<center><h1>Незавершенные покупки</h1></center>");
            result.append("<table align = \"center\"><tr>");
            rs = statement.executeQuery("select * from purchaseTable where now() > finishTime and status = true " +
                    "and (select id from personTable where login = \'" + loginParam + "\') = ownerId;");
            for (int i = 0; i < columns; i++)
                result.append("<td>").append(headers[i]).append("</td>");
            result.append("</tr>");
            while (rs.next()) {
                result.append("<tr>");
                for (int i = 0; i < columns; i++)
                    result.append("<td>").append(rs.getString(labels[i])).append("</td>");
                /*String reg = rs.getString(labels[labels.length - 1]);
                if (!reg.equals("0"))
                    result.append("<td></td>");
                else
                    result.append("<td>").append(rs.getString(labels[labels.length - 1])).append("</td>");*/
                result.append("</tr>");
            }
            result.append("</table>");
            rs.close();

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            restorePurchaseTAble();
            restorePersonTable();
            return "SQL error";
        }

        return result.toString();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    public String deleteOldPurchases(@RequestParam(value = "login") String loginParam,
                                     @RequestParam(value = "password") String passParam,
                                     @RequestParam(value = "time", required = false, defaultValue = "") String timeParam,
                                     @RequestParam(value = "name", required = false, defaultValue = "") String nameParam) {
        

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
                    + loginParam + "\' and pass = \'" + passParam + "\';");
            if (!rs.next()) {
                rs.close();
                statement.close();
                connection.close();
                return "ошибка авторизации";
            }
            rs.close();

            if (!nameParam.equals("") && !timeParam.equals("")) {
                statement.executeUpdate("update purchaseTable " +
                        "set status = false " +
                        "where isRegular = 0 and nameOfThing = \'" + nameParam + "\' " +
                        "and finishTime = \'" + timeParam + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + loginParam + "\');");
                statement.executeUpdate("update purchaseTable " +
                        "set finishTime = finishTime + interval isRegular day " +
                        "where isRegular > 0 and nameOfThing = \'" + nameParam + "\' " +
                        "and finishTime = \'" + timeParam + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + loginParam + "\');");
            }
            else if (!nameParam.equals("")) {
                statement.executeUpdate("update purchaseTable " +
                        "set status = false " +
                        "where isRegular = 0 and nameOfThing = \'" + nameParam + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + loginParam + "\');");
                statement.executeUpdate("update purchaseTable " +
                        "set finishTime = finishTime + interval isRegular day " +
                        "where isRegular > 0 and nameOfThing = \'" + nameParam + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + loginParam + "\');");
            }
            else if (!timeParam.equals("")) {
                statement.executeUpdate("update purchaseTable " +
                        "set status = false " +
                        "where isRegular = 0 " +
                        "and finishTime = \'" + timeParam + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + loginParam + "\');");
                statement.executeUpdate("update purchaseTable " +
                        "set finishTime = finishTime + interval isRegular day " +
                        "where isRegular > 0 " +
                        "and finishTime = \'" + timeParam + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + loginParam + "\');");
            }

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            restorePurchaseTAble();
            restorePersonTable();
            return "SQL error";
        }

        return "Удачно";

    }

    @RequestMapping(method = RequestMethod.POST, value = "/deleteReg")
    public String deleteRegularPurchases(@RequestParam(value = "login") String loginParam,
                                     @RequestParam(value = "password") String passParam,
                                     @RequestParam(value = "name", required = false, defaultValue = "") String nameParam) {
        

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
                    + loginParam + "\' and pass = \'" + passParam + "\';");
            if (!rs.next()) {
                rs.close();
                statement.close();
                connection.close();
                return "ошибка авторизации";
            }
            rs.close();

            statement.executeUpdate("update purchaseTable " +
                    "set status = false " +
                    "where isRegular > 0 and nameOfThing = \'" + nameParam + "\' and " +
                    "ownerId = (select id from personTable where login = \'" + loginParam + "\');");

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            restorePurchaseTAble();
            restorePersonTable();
            return "SQL error";
        }

        return "Удачно";

    }
}
