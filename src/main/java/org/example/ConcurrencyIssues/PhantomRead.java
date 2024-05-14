package org.example.ConcurrencyIssues;

import org.example.MySQLConnection;

import java.sql.*;
import java.util.concurrent.TimeUnit;

public class PhantomRead {
    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            int initialCount = countGames(connection);
            System.out.println("Initial Count: " + initialCount);

            Thread thread = new Thread(() -> insertGame(connection, "New Game"));
            thread.start();

            TimeUnit.SECONDS.sleep(2);

            int finalCount = countGames(connection);
            System.out.println("Final Count: " + finalCount);

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int countGames(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Game";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count");
                } else {
                    throw new SQLException("Count not retrieved");
                }
            }
        }
    }

    private static void insertGame(Connection connection, String gameName) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String sql = "INSERT INTO Game (Name, Price) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gameName);
            statement.setInt(2, 50);
            statement.executeUpdate();
            System.out.println("New Game inserted successfully by Thread-" + Thread.currentThread().getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
