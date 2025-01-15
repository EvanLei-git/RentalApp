package gr.hua.dit.rentalapp.services;

import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class PostgresLargeObjectService {
    private static final Logger log = LoggerFactory.getLogger(PostgresLargeObjectService.class);

    @Autowired
    private DataSource dataSource;

    @Transactional
    public Long saveImage(byte[] imageData) throws SQLException, IOException {
        log.debug("Saving image of size: {} bytes", imageData != null ? imageData.length : 0);
        if (imageData == null || imageData.length == 0) {
            log.warn("Attempted to save null or empty image data");
            return null;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            PGConnection pgConn = conn.unwrap(PGConnection.class);
            LargeObjectManager lobj = pgConn.getLargeObjectAPI();
            
            long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            log.debug("Created large object with OID: {}", oid);

            try {
                LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
                try {
                    byte[] buf = new byte[2048];
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
                        int size;
                        while ((size = bis.read(buf, 0, 2048)) > 0) {
                            obj.write(buf, 0, size);
                        }
                    }
                    log.debug("Successfully wrote {} bytes to large object {}", imageData.length, oid);
                } finally {
                    obj.close();
                }
                conn.commit();
                return oid;
            } catch (Exception e) {
                log.error("Error writing to large object {}, attempting rollback", oid, e);
                try {
                    conn.rollback();
                    lobj.unlink(oid);
                } catch (SQLException se) {
                    log.error("Error during rollback for OID: {}", oid, se);
                }
                throw e;
            }
        }
    }

    @Transactional
    public byte[] getImage(Long oid) throws SQLException, IOException {
        log.debug("Retrieving image with OID: {}", oid);
        if (oid == null) {
            log.warn("Attempted to retrieve image with null OID");
            return null;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            PGConnection pgConn = conn.unwrap(PGConnection.class);
            LargeObjectManager lobj = pgConn.getLargeObjectAPI();

            try {
                // Try to open the object first to check if it exists
                LargeObject obj = lobj.open(oid, LargeObjectManager.READ);
                try {
                    // Get the object size
                    int size = obj.size();
                    log.debug("Large object {} size: {} bytes", oid, size);
                    
                    // Read the data in chunks
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = obj.read(buf, 0, 2048)) > 0) {
                        bos.write(buf, 0, bytesRead);
                    }
                    
                    byte[] imageData = bos.toByteArray();
                    log.debug("Successfully read {} bytes from large object {}", imageData.length, oid);
                    return imageData;
                } finally {
                    obj.close();
                }
            } catch (SQLException e) {
                log.error("Error reading large object {}: {}", oid, e.getMessage());
                if (e.getMessage().contains("does not exist")) {
                    log.warn("Large object {} does not exist, returning null", oid);
                    return null;
                }
                throw e;
            }
        }
    }

    @Transactional
    public void deleteImage(Long oid) throws SQLException {
        log.debug("Deleting image with OID: {}", oid);
        if (oid == null) {
            log.warn("Attempted to delete image with null OID");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            PGConnection pgConn = conn.unwrap(PGConnection.class);
            LargeObjectManager lobj = pgConn.getLargeObjectAPI();
            
            try {
                lobj.unlink(oid);
                conn.commit();
                log.debug("Successfully deleted large object {}", oid);
            } catch (SQLException e) {
                log.error("Error deleting large object {}", oid, e);
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    log.error("Error during rollback for delete of OID: {}", oid, se);
                }
                // Don't throw if the object doesn't exist
                if (!e.getMessage().contains("does not exist")) {
                    throw e;
                }
            }
        }
    }
}
