/**
 * Copyright (C) 2013, Moss Computing Inc.
 *
 * This file is part of veracity.
 *
 * veracity is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * veracity is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with veracity; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package com.moss.veracity.idmanager;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

class FirstMiddleLastView extends JPanel {

	private JTextField fieldLast;
	private JTextField fieldMiddleInitial;
	
	private JTextField fieldFirst;

	FirstMiddleLastView() {
		super();
		setOpaque(false);
		setBorder(new TitledBorder(null, "Name", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {7,7,7,7};
		gridBagLayout.columnWidths = new int[] {7,7,7,7,7,7,7};
		setLayout(gridBagLayout);

		final JLabel firstNameLabel = new JLabel();
		firstNameLabel.setText("First");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridx = 1;
		gridBagConstraints_3.gridy = 0;
		add(firstNameLabel, gridBagConstraints_3);

		final JLabel middleInitialLabel = new JLabel();
		middleInitialLabel.setText("Middle Initial");
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.gridy = 0;
		gridBagConstraints_4.gridx = 3;
		add(middleInitialLabel, gridBagConstraints_4);

		final JLabel lastNameLabel = new JLabel();
		lastNameLabel.setText("Last");
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridy = 0;
		gridBagConstraints_5.gridx = 5;
		add(lastNameLabel, gridBagConstraints_5);

		fieldFirst = new JTextField();
		fieldFirst.setColumns(14);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridx = 1;
		add(fieldFirst, gridBagConstraints);

		fieldMiddleInitial = new JTextField();
		fieldMiddleInitial.setColumns(1);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridy = 2;
		gridBagConstraints_1.gridx = 3;
		add(fieldMiddleInitial, gridBagConstraints_1);

		fieldLast = new JTextField();
		fieldLast.setColumns(14);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.gridy = 2;
		gridBagConstraints_2.gridx = 5;
		add(fieldLast, gridBagConstraints_2);
	}
	protected JTextField getFieldFirst() {
		return fieldFirst;
	}
	protected JTextField getFieldMiddleInitial() {
		return fieldMiddleInitial;
	}
	protected JTextField getFieldLast() {
		return fieldLast;
	}
}
