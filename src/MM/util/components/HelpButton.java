/*
 * Copyright 2007-2012 
 * This file is part of MM.
 *
 * MM is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MM is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MM; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package MM.util.components;

import MM.desktop.impl.helpsystem.MMHelpSet;
import MM.desktop.impl.helpsystem.HelpImpl;
import MM.main.MMCore;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.swing.JButton;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 * This class extends JButton class to implement Help system generated
 * programatically. This class permits to get the help system in a dialog modal
 * window, and assign the HelpSystem Action Listener to this button. 
 */
public class HelpButton extends JButton {

        /**
         * This constructor receives as parameter a help ID stored in HelpSet.
         *
         * @param helpID
         */
        public HelpButton(String helpID) {
                super("Help");
                try {

                        HelpImpl helpImp = MMCore.getHelpImpl();

                        if (helpImp == null) {
                                setVisible(false);
                                return;
                        }

                        MMHelpSet hs = helpImp.getHelpSet();

                        if (hs == null) {
                                setEnabled(false);
                                return;
                        }

                        HelpBroker hb = hs.createHelpBroker();
                        hs.setHomeID(helpID);

                        this.addActionListener(new CSH.DisplayHelpFromSource(hb));

                } catch (Exception event) {
                        event.printStackTrace();
                }
        }
}
