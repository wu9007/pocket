package org.hv.pocket.lunch;

import org.hv.pocket.exception.PocketMapperException;

/**
 * @author wujianchuan
 */
public interface PocketConfig {
    /**
     * Initialize the persistent resource
     *
     * @throws PocketMapperException e
     */
    void init() throws PocketMapperException;

    /**
     * Initializes the DES key
     *
     * @param desKey des key
     * @return config
     */
    PocketConfig setDesKey(String desKey);

    /**
     * Initializes the DES key
     *
     * @param sm4Key sm4 key
     * @return config
     */
    PocketConfig setSm4Key(String sm4Key);
}
