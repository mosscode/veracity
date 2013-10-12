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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageSelector extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JButton button;
	private byte[] defaultImage;
	private byte[] image;
	
	public ImageSelector() {
		
		setLayout(new BorderLayout());
		
		button = new JButton();
		button.setContentAreaFilled(false);
		add(button, BorderLayout.CENTER);
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseImageFromFile();
			}
		});
		
		URL defaultUrl = this.getClass().getClassLoader().getResource("com/moss/veracity/admin/default-profile.png");
		defaultImage = readUrl(defaultUrl);
		button.setIcon(new ImageIcon(defaultImage));
	}
	
	public void setDefault(byte[] image) {
		
		if (image == null) {
			return;
		}
		
		this.defaultImage = image;
		
		if (image == null) {
			image = defaultImage;
			button.setIcon(new ImageIcon(image));			
		}
	}
	
	public void setImage(byte[] image) {
		
		if (image == null) {
			return;
		}
		
		this.image = image;
		button.setIcon(new ImageIcon(image));
	}
	
	public byte[] getImage() {
		
		if (image == null) {
			return defaultImage;
		}
		else {
			return image;
		}
	}
	
	private void chooseImageFromFile() {
		JFileChooser fc = new JFileChooser();
		int result = fc.showOpenDialog(null);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			
			if (file != null) {
				image = readFile(file);
				button.setIcon(new ImageIcon(image));
			}
		}
	}
	
	private byte[] readFile(File file) {
		try {
			return readUrl(file.toURI().toURL());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static byte[] readUrl(URL url) {
		
		try {
			InputStream in = url.openStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024 * 10]; //10k buffer
			for(int numRead = in.read(buffer); numRead!=-1; numRead = in.read(buffer)){
				out.write(buffer, 0, numRead);
			}

			in.close();
			out.close();

			return out.toByteArray();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public JButton getButton() {
		return button;
	}
	
	public static void main(String[] args) {
		
		URL url = ImageSelector.class.getClassLoader().getResource("com/moss/veracity/admin/default-profile.png");
		
		ImageSelector sel = new ImageSelector();
		sel.setDefault(readUrl(url));
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(sel);
		frame.setSize(600, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
