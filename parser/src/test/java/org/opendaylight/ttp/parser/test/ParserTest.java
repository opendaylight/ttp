/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.ttp.parser.test;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.opendaylight.ttp.parser.Main;

public class ParserTest {

    /**
     * Test's the parser command for validate on a modified OF-DPA 2.0 TTP in JSON.
     *
     * TODO: add actual asserts, not just checking for a lack of exceptions.
     *
     * @throws WebApplicationException
     * @throws IOException
     */
    @Test
    public void testOFDPAv2Validate() throws WebApplicationException, IOException {
        String[] args = {"validate","ofdpa-v2.0.0.2.odl-ttp.json"};
        Main.main(args);
    }

    /**
     * Test's the parser command for dot on a modified OF-DPA 2.0 TTP in JSON.
     *
     * TODO: add actual asserts, not just checking for a lack of exceptions.
     *
     * @throws WebApplicationException
     * @throws IOException
     */
    @Test
    public void testOFDPAv2Dot() throws WebApplicationException, IOException {
        String[] args = {"dot","ofdpa-v2.0.0.2.odl-ttp.json"};
        Main.main(args);
    }
}
