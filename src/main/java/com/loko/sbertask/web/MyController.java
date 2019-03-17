package com.loko.sbertask.web;

import org.springframework.web.bind.annotation.*;

import java.sql.*;

@RestController
public class MyController {

    //адресс до базы данных
    private String url = "jdbc:mysql://localhost:3306/";
    private String dbName = "data";
    private String userName = "loko";//логин и пароль для бд
    private String password = "rusakov";
    //заголовки для таблиц
    private String[] headers = {"Название", "Цена", "Количество", "Запланированное время", "Периодичность дней"};
    private String[] labels = {"nameOfThing", "price", "amount", "finishTime", "isRegular"};

    //функция, которая преобразует результат запроса в html таблицу
    private StringBuilder ResultSetToHTML(ResultSet rs) throws SQLException {
        StringBuilder result = new StringBuilder();
        int columns = headers.length;
        result.append("<table width = \"80%\" align = \"center\"><tr>");
        for (String header : headers)
            result.append("<td>").append(header).append("</td>");
        result.append("</tr>");
        while (rs.next()) {
            result.append("<tr>");
            for (int i = 0; i < columns; i++)
                result.append("<td>").append(rs.getString(labels[i])).append("</td>");
            result.append("</tr>");
        }
        result.append("</table>");
        rs.close();
        return result;
    }
    //проверка соеденения и при необходимости восстановление таблиц
    private Connection initConnection() {
        Connection connection;
        try {
            connection = DriverManager.getConnection(url + dbName, userName, password);
        } catch (SQLException e) {
            restoreDatabase();
            try {
                connection = DriverManager.getConnection(url + dbName, userName, password);
            } catch (SQLException e1) {
                e1.printStackTrace();
                return null;
            }
        }
        return connection;
    }
    //проверка логина и пароля. true - если такой логин с паролем есть
    private boolean checkAuthorization(Connection connection, String login, String password) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("select * from personTable where " +
                "login = ? and pass = ?;");
        preparedStatement.setString(1, login);
        preparedStatement.setString(2, password);
        ResultSet rs = preparedStatement.executeQuery();
        if (!rs.next()) {
            rs.close();
            preparedStatement.close();
            return true;
        }
        rs.close();
        preparedStatement.close();
        return false;
    }

    //восстановление базы данных и таблиц
    private void restoreDatabase() {

        try {
            Connection connection = DriverManager.getConnection(url, userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create database data");
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        restorePersonTable();
        restorePurchaseTAble();
    }
    private void restorePersonTable() {
        //в базе пользователей храянятся имя, фамилия, логин и пароль
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
        }
    }
    private void restorePurchaseTAble() {
        //в таблице покупок хранятся крайнее время покупки, название, цена, статус выполнена ли она
        //владелец, регулярная ли она (если да, то регулярность в днях), необходимое количество
        try {
            Connection connection = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists purchaseTable\n" +
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
        }
    }

    //запись нового пользователя
    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public String registerNewUser(@RequestParam(value = "name") String name,
                                  @RequestParam(value = "secondName") String secondName,
                                  @RequestParam(value = "login") String login,
                                  @RequestParam(value = "password") String pass) {
        
        //установить соеденение с бд
        Connection connection = initConnection();
        if (connection == null)
            return "Ошибка соеденения";

        Statement statement;

        try {
            //проверить не повторяется ли логин
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from personTable where login = \'"
                    + login + "\';");
            if (rs.next()) {
                rs.close();
                statement.close();
                connection.close();
                return "логин " + login + " уже существует";
            }

            //если нет то добавить нового
            PreparedStatement preparedStatement =
                    connection.prepareStatement("insert into personTable(name, secondName, login, pass) " +
                            "values (?, ?, ?, ?);");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, secondName);
            preparedStatement.setString(3, login);
            preparedStatement.setString(4, pass);
            preparedStatement.executeUpdate();
            preparedStatement.close();
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

    //добавление новой покупки
    @RequestMapping(method = RequestMethod.POST, value = "/add")
    public String addNewPurchase(@RequestParam(value = "login") String login,
                                 @RequestParam(value = "password") String pass,
                                 @RequestParam(value = "time") String time,
                                 @RequestParam(value = "price") Double price,
                                 @RequestParam(value = "amount") Integer amount,
                                 @RequestParam(value = "regular", required = false, defaultValue = "0") Integer regular,
                                 @RequestParam(value = "name") String name) {



        Connection connection = initConnection();
        if (connection == null)
            return "Ошибка соеденения";

        Statement statement;
        try {
            //проверка, что такой пользователь есть
            statement = connection.createStatement();
            if (checkAuthorization(connection, login, pass)) {
                statement.close();
                connection.close();
                return "Ошибка авторизации";
            }


            //посмотреть, есть ли уже такая активная покупка
            PreparedStatement preparedStatement =
                    connection.prepareStatement("select * from purchaseTable where nameOfThing = ? and " +
                            "finishTime = ? and isRegular = ? and price = ? and (select id from personTable " +
                            "where login = ? ) = ownerId and status = 1;");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, time);
            preparedStatement.setInt(3, regular);
            preparedStatement.setDouble(4, price);
            preparedStatement.setString(5, login);
            ResultSet rs = preparedStatement.executeQuery();

            //если есть, то просто добавить количество в существующую
            if (rs.next()) {
                statement.executeUpdate("update purchaseTable " +
                        "set amount = amount + " + amount +
                        " where id = " + rs.getString("id") + ";");
            }
            else {
                //иначе добавить ее
                preparedStatement = connection.prepareStatement("insert into " +
                        "purchaseTable(finishtime, nameofthing, price, status, ownerid, isregular, amount) " +
                        "values (?, ?, ?, 1, (select id from personTable where login = ?), ?, ?);");
                preparedStatement.setString(1, time);
                preparedStatement.setString(2, name);
                preparedStatement.setDouble(3, price);
                preparedStatement.setString(4, login);
                preparedStatement.setInt(5, regular);
                preparedStatement.setInt(6, amount);
                preparedStatement.executeUpdate();
            }

            rs.close();
            statement.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            restorePurchaseTAble();
            restorePersonTable();
            return "SQL error";
        }

        return "Успешно добавлено";
    }

    //получение списка актуальных покупок
    @RequestMapping(method = RequestMethod.POST, value = "/get")
    public String getAllPurchases(@RequestParam(value = "login") String login,
                                  @RequestParam(value = "password") String pass) {


        Connection connection = initConnection();
        if (connection == null)
            return "Ошибка соеденения";

        Statement statement;
        StringBuilder result = new StringBuilder();
        try {
            //проверка пользователя
            statement = connection.createStatement();
            if (checkAuthorization(connection, login, pass)) {
                statement.close();
                connection.close();
                return "Ошибка авторизации";
            }

            //запросить покупки актуальные покупки и записать их в виде таблицы
            ResultSet rs = statement.executeQuery("select * from purchaseTable where now() <= finishTime and status = true " +
                    "and (select id from personTable where login = \'" + login + "\') = ownerId;");
            result.append("<center><h1>Актуальные покупки</h1></center>");
            result.append(ResultSetToHTML(rs));

            //хапросить незавершенные покупки, и также их оформить
            rs = statement.executeQuery("select * from purchaseTable where now() > finishTime and status = true " +
                    "and (select id from personTable where login = \'" + login + "\') = ownerId;");
            result.append("<center><h1>Незавершенные покупки</h1></center>");
            result.append(ResultSetToHTML(rs));

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

    //заверщение покупки
    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    public String deleteOldPurchases(@RequestParam(value = "login") String login,
                                     @RequestParam(value = "password") String pass,
                                     @RequestParam(value = "time", required = false, defaultValue = "") String time,
                                     @RequestParam(value = "name", required = false, defaultValue = "") String name) {


        Connection connection = initConnection();
        if (connection == null)
            return "Ошибка соеденения";

        String result = "";
        Statement statement;
        try {
            statement = connection.createStatement();
            if (checkAuthorization(connection, login, pass)) {
                statement.close();
                connection.close();
                return "Ошибка авторизации";
            }

            //покупка указывается названием или датой, или одновременно обоими параметрами
            //если покупка нерегулярная то она помечается как выполненная
            //регулярные же обновляют дату следующего выполнения
            ResultSet resultSet;
            String query = "select * from purchaseTable " +
                    "where status = true and " +
                    "ownerId = (select id from personTable where login = \'" + login + "\')";
            if (!name.equals("") && !time.equals("")) {
                query += " and finishTime = \'" + time + "\' and nameOfThing = \'" + name + "\';";
                resultSet = statement.executeQuery(query);
                result = ResultSetToHTML(resultSet).toString();
                statement.executeUpdate("update purchaseTable " +
                        "set status = false " +
                        "where isRegular = 0 and nameOfThing = \'" + name + "\' " +
                        "and finishTime = \'" + time + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + login + "\');");
                statement.executeUpdate("update purchaseTable " +
                        "set finishTime = finishTime + interval isRegular day " +
                        "where isRegular > 0 and nameOfThing = \'" + name + "\' " +
                        "and finishTime = \'" + time + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + login + "\');");
            }
            else if (!name.equals("")) {
                query += " and nameOfThing = \'" + name + "\';";
                resultSet = statement.executeQuery(query);
                result = ResultSetToHTML(resultSet).toString();
                statement.executeUpdate("update purchaseTable " +
                        "set status = false " +
                        "where isRegular = 0 and nameOfThing = \'" + name + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + login + "\');");
                statement.executeUpdate("update purchaseTable " +
                        "set finishTime = finishTime + interval isRegular day " +
                        "where isRegular > 0 and nameOfThing = \'" + name + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + login + "\');");
            }
            else if (!time.equals("")) {
                query += " and finishTime = \'" + time + "\';";
                resultSet = statement.executeQuery(query);
                result = ResultSetToHTML(resultSet).toString();
                statement.executeUpdate("update purchaseTable " +
                        "set status = false " +
                        "where isRegular = 0 " +
                        "and finishTime = \'" + time + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + login + "\');");
                statement.executeUpdate("update purchaseTable " +
                        "set finishTime = finishTime + interval isRegular day " +
                        "where isRegular > 0 " +
                        "and finishTime = \'" + time + "\' and " +
                        "ownerId = (select id from personTable where login = \'" + login + "\');");
            }


            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            restorePurchaseTAble();
            restorePersonTable();
            return "SQL error";
        }

        return "Удачно изменены записи\n" + result;
    }

    //закрытие регулярных покупок
    @RequestMapping(method = RequestMethod.POST, value = "/deleteReg")
    public String deleteRegularPurchases(@RequestParam(value = "login") String login,
                                     @RequestParam(value = "password") String pass,
                                     @RequestParam(value = "name") String name) {


        Connection connection = initConnection();
        if (connection == null)
            return "Ошибка соеденения";

        String result = "";
        Statement statement;
        try {
            statement = connection.createStatement();
            if (checkAuthorization(connection, login, pass)) {
                statement.close();
                connection.close();
                return "Ошибка авторизации";
            }

            ResultSet resultSet = statement.executeQuery("select * from purchaseTable " +
                    "where isRegular > 0 and nameOfThing = \'" + name + "\' and " +
                    "ownerId = (select id from personTable where login = \'" + login + "\') " +
                    "and status = true;");
            result = ResultSetToHTML(resultSet).toString();
            //поиск идет только по названию. помечается как выполненная
            statement.executeUpdate("update purchaseTable " +
                    "set status = false " +
                    "where isRegular > 0 and nameOfThing = \'" + name + "\' and " +
                    "ownerId = (select id from personTable where login = \'" + login + "\');");

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            restorePurchaseTAble();
            restorePersonTable();
            return "SQL error";
        }

        return "Удачно изменены записи " + result;
    }
}
