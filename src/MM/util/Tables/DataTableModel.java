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
package MM.util.Tables;

import MM.data.DatasetType;
import java.awt.Color;
import javax.swing.table.TableModel;

/**
 * interface of a model
 *
 * @author scsandra
 */
public interface DataTableModel extends TableModel {

        /**
         * Returns the number of columns corresponding to the fix parameters of
         * each kind of dataset. For example, in LC-MS data sets they would be
         * "retention time", "name", .., and in GCxGX-tof data sets "RT1", "RT2",...
         *
         * @return Number of parameter columns
         */
        public int getFixColumns();

        /**
         * Removes selected rows from the table. It only removes the rows which are
         * selected in the "Selection" column.
         *
         */
        public void removeRows();

        /**
         * Returns the type of data set which correspons to this data model
         *
         * @return Data set type
         */
        public DatasetType getType();

        /**
         * Adds a new column in the table.
         *
         * @param columnName Name of the new column
         */
        public void addColumn(String columnName);

        public void addRowColor(Color[] color);

        public Color getRowColor(int row);

        public Color getCellColor(int row, int column);
}
