package org.example.ConcurrencyIssues;

import org.example.MySQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class DirtyRead {
    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            int initialPrice = getGamePrice(connection, "New Game");
            System.out.println("Initial Price: " + initialPrice);

            Thread thread = new Thread(() -> updateGamePrice(connection, "New Game", initialPrice + 10));
            thread.start();

            TimeUnit.SECONDS.sleep(1);

            int dirtyReadPrice = getGamePrice(connection, "New Game");
            System.out.println("Dirty Read Price: " + dirtyReadPrice);

            thread.join();

            int finalPrice = getGamePrice(connection, "New Game");
            System.out.println("Final Price: " + finalPrice);

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getGamePrice(Connection connection, String gameName) throws SQLException {
        String sql = "SELECT Price FROM Game WHERE Name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gameName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("Price");
                } else {
                    throw new SQLException("Game not found");
                }
            }
        }
    }

    private static void updateGamePrice(Connection connection, String gameName, int newPrice) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String sql = "UPDATE Game SET Price = ? WHERE Name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newPrice);
            statement.setString(2, gameName);
            statement.executeUpdate();
            System.out.println("Price updated successfully by Thread-" + Thread.currentThread().getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
