package com.aengdulab.ticket.service.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NamedLockServiceWrapper {

    private Logger logger = LoggerFactory.getLogger(NamedLockServiceWrapper.class);

    private static final String GET_LOCK_SQL = "select get_lock(?, ?)";
    private static final String RELEASE_LOCK_SQL = "select release_lock(?)";
    private static final int LOCK_TIMEOUT = 1000;

    private final DataSource dataSource;

    public void executeInNamedLock(String lockName, Runnable runnable) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                getLock(connection, lockName);
                runnable.run();
            } finally {
                releaseLock(connection, lockName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getLock(Connection connection, String lockName) {
        try (PreparedStatement pstmt = connection.prepareStatement(GET_LOCK_SQL)) {
            pstmt.setString(1, lockName);
            pstmt.setInt(2, LOCK_TIMEOUT);
            checkLockAcquired(pstmt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void releaseLock(Connection connection, String lockName) {
        try (PreparedStatement pstmt = connection.prepareStatement(RELEASE_LOCK_SQL)) {
            pstmt.setString(1, lockName);
            checkLockAcquired(pstmt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkLockAcquired(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            if (!rs.next()) {
                throw new RuntimeException("Lock acquire failed");
            }
            int result = rs.getInt(1);
            if (result != 1) {
                throw new RuntimeException("Lock acquire failed");
            }
        }
    }
}
