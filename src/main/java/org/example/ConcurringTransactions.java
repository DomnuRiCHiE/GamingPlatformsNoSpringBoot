package org.example;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConcurringTransactions implements Runnable {
    private final Connection connection;
    private final String tableName;

    public ConcurringTransactions(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            Thread gameThread = new Thread(new ConcurringTransactions(connection, "Game"));
            Thread dlcThread = new Thread(new ConcurringTransactions(connection, "DLC"));

            gameThread.start();
            dlcThread.start();

            gameThread.join();
            dlcThread.join();
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            switch (tableName) {
                case "Game":
                    insertGameRecord();
                    break;
                case "DLC":
                    insertDLCRecord();
                    break;
                default:
                    System.out.println("Invalid table name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertGameRecord() throws SQLException {
        String sql = "INSERT INTO Game (Name, Price, ReleaseDate, AgeRestriction, GameFileSizeInGB, AchievementNumber) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, "New Game");
        statement.setInt(2, 60);
        statement.setDate(3, Date.valueOf("2024-05-29"));
        statement.setInt(4, 18);
        statement.setInt(5, 57);
        statement.setInt(6, 20);
        statement.executeUpdate();
        statement.close();
        System.out.println("Game record inserted successfully.");
    }

    private void insertDLCRecord() throws SQLException {
        String sql = "INSERT INTO DLC (GameId, Name, Price) VALUES (?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, 1);
        statement.setString(2, "New DLC");
        statement.setInt(3, 20);
        statement.executeUpdate();
        statement.close();
        System.out.println("DLC record inserted successfully.");
    }
}
