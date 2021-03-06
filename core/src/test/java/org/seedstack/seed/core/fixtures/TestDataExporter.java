/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import com.google.common.collect.Lists;
import org.seedstack.seed.DataExporter;
import org.seedstack.seed.DataSet;

import java.util.Iterator;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 12/03/14
 */
@DataSet(group="group1", name="test1")
public class TestDataExporter implements DataExporter<TestDTO> {

    @Override
    public Iterator<TestDTO> exportData() {
        return Lists.newArrayList(new TestDTO("toto", "titi"), new TestDTO("machin", "truc")).iterator();
    }
}
