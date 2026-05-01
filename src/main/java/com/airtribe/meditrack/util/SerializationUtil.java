package com.airtribe.meditrack.util;

import com.airtribe.meditrack.exception.DataPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Utility for Java object serialization and deserialization using {@link java.io.ObjectOutputStream}.
 *
 * <p>Both methods use try-with-resources to guarantee stream closure. I/O and class-not-found
 * failures are wrapped in {@link com.airtribe.meditrack.exception.DataPersistenceException}
 * with exception chaining so the original cause is preserved for diagnostics.
 */
public final class SerializationUtil {

    private static final Logger logger = LoggerFactory.getLogger(SerializationUtil.class);

    private SerializationUtil() {}

    public static <T extends Serializable> void serialize(T object, String filePath)
            throws DataPersistenceException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
            logger.info("Serialized {} to {}", object.getClass().getSimpleName(), filePath);
        } catch (IOException e) {
            throw new DataPersistenceException("serialize",
                    "Failed to write object to: " + filePath, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String filePath) throws DataPersistenceException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            T result = (T) ois.readObject();
            logger.info("Deserialized object from {}", filePath);
            return result;
        } catch (IOException | ClassNotFoundException e) {
            throw new DataPersistenceException("deserialize",
                    "Failed to read object from: " + filePath, e);
        }
    }
}
