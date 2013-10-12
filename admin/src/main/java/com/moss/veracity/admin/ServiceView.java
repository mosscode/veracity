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
package com.moss.veracity.admin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class ServiceView extends JPanel {

	private JSpinner fieldSigningKeyRefreshTimeout;
	private JButton buttonUpdate;
	private JButton buttonRefresh;
	private JSpinner fieldEndorsementExpirationTimeout;
	private JSpinner fieldSigningKeyExpirationTimeout;
	private static final long serialVersionUID = 1L;
	public ServiceView() {
		super();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {7,7,7,7,7,7,7,0,7};
		gridBagLayout.columnWidths = new int[] {0,7};
		setLayout(gridBagLayout);

		final JLabel fieldSigningKeyExpirationLabel = new JLabel();
		fieldSigningKeyExpirationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		fieldSigningKeyExpirationLabel.setText("Signing Key Expiration Timeout (ms)");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 0;
		add(fieldSigningKeyExpirationLabel, gridBagConstraints);

		fieldSigningKeyExpirationTimeout = new JSpinner();
		fieldSigningKeyExpirationTimeout.setModel(new SpinnerNumberModel());
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.ipadx = 45;
		gridBagConstraints_1.gridy = 1;
		gridBagConstraints_1.gridx = 2;
		add(fieldSigningKeyExpirationTimeout, gridBagConstraints_1);

		final JLabel labelEndorsementExpirationTimeout = new JLabel();
		labelEndorsementExpirationTimeout.setHorizontalAlignment(SwingConstants.RIGHT);
		labelEndorsementExpirationTimeout.setText("Endorsement Expiration Timeout (ms)");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_2.gridy = 3;
		gridBagConstraints_2.gridx = 0;
		add(labelEndorsementExpirationTimeout, gridBagConstraints_2);

		fieldEndorsementExpirationTimeout = new JSpinner();
		fieldEndorsementExpirationTimeout.setModel(new SpinnerNumberModel());
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.ipadx = 45;
		gridBagConstraints_3.gridy = 3;
		gridBagConstraints_3.gridx = 2;
		add(fieldEndorsementExpirationTimeout, gridBagConstraints_3);

		final JLabel signingKeyRefreshLabel = new JLabel();
		signingKeyRefreshLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		signingKeyRefreshLabel.setText("Signing Key Refresh Timeout (ms)");
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_6.gridy = 5;
		gridBagConstraints_6.gridx = 0;
		add(signingKeyRefreshLabel, gridBagConstraints_6);

fieldSigningKeyRefreshTimeout = new JSpinner();
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_7.gridy = 5;
		gridBagConstraints_7.gridx = 2;
		add(fieldSigningKeyRefreshTimeout, gridBagConstraints_7);

		final JPanel panel = new JPanel();
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.weighty = 1;
		gridBagConstraints_4.gridy = 7;
		gridBagConstraints_4.gridx = 0;
		add(panel, gridBagConstraints_4);

		final JPanel panel_1 = new JPanel();
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridwidth = 3;
		gridBagConstraints_5.gridy = 9;
		gridBagConstraints_5.gridx = 0;
		add(panel_1, gridBagConstraints_5);

		buttonRefresh = new JButton();
		buttonRefresh.setText("Refresh");
		panel_1.add(buttonRefresh);

		buttonUpdate = new JButton();
		buttonUpdate.setText("Update");
		panel_1.add(buttonUpdate);
	}
	public JSpinner getFieldSigningKeyExpirationTimeout() {
		return fieldSigningKeyExpirationTimeout;
	}
	public JSpinner getFieldEndorsementExpirationTimeout() {
		return fieldEndorsementExpirationTimeout;
	}
	public JButton getButtonRefresh() {
		return buttonRefresh;
	}
	public JButton getButtonUpdate() {
		return buttonUpdate;
	}
	protected JSpinner getFieldSigningKeyRefreshTimeout() {
		return fieldSigningKeyRefreshTimeout;
	}
	
}
