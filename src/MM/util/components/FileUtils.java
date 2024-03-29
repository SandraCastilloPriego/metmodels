/*
 * Copyright 2007-2012 
 * This file is part of MetModels.
 *
 * MetModels is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MetModels is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package MM.util.components;

import MM.data.Dataset;
import MM.data.DatasetType;
import MM.data.Row;
import MM.data.impl.datasets.SimpleBasicDataset;
import MM.data.impl.peaklists.SimplePeakListRowOther;
import MM.util.Tables.DataTableModel;

/**
 *
 * @author scsandra
 */
public class FileUtils {

    public static Row getPeakListRow(DatasetType type) {
        switch (type) {           
            case MODELS:
                return new SimplePeakListRowOther();           
        }
        return null;
    }

    public static Dataset getDataset(Dataset dataset, String Name) {
        Dataset newDataset = null;
        switch (dataset.getType()) {            
            case MODELS:
                newDataset = new SimpleBasicDataset(Name + dataset.getDatasetName());
                break;            
        }
        newDataset.setType(dataset.getType());
        return newDataset;
    }
   
}
