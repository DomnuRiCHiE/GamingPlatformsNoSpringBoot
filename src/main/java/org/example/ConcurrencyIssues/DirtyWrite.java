package org.example.ConcurrencyIssues;

import org.example.MySQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DirtyWrite {
    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            int initialValue = getGameAgeRestriction(connection, "New Game");
            System.out.println("Initial Age Restriction: " + initialValue);

            Thread thread1 = new Thread(() -> updateGameAgeRestriction(connection, "New Game", 16));
            Thread thread2 = new Thread(() -> updateGameAgeRestriction(connection, "New Game", 21));

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            int finalValue = getGameAgeRestriction(connection, "New Game");
            System.out.println("Final Age Restriction: " + finalValue);
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
}

    private static int getGameAgeRestriction(Connection connection, String gameName) throws SQLException {
        String sql = "SELECT AgeRestriction FROM Game WHERE Name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gameName);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("AgeRestriction");
                } else {
                    throw new SQLException("Game not found");
                }
            }
        }
    }

    private static void updateGameAgeRestriction(Connection connection, String gameName, int newAgeRestriction) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String sql = "UPDATE Game SET AgeRestriction = ? WHERE Name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newAgeRestriction);
            statement.setString(2, gameName);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Game not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
