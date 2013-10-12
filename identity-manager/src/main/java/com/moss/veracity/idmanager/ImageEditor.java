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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.moss.veracity.api.VtImage;

public class ImageEditor extends JButton {
	
	private final byte[] defaultIconData;
	private VtImage image;
	
	public ImageEditor() {
		
		setText("");
		addActionListener(new LoadIconListener());
		
		try {
			
			String basePackage = this.getClass().getPackage().getName().toString().replaceAll("\\.", "/");
			String defaultIconResource = basePackage + "/default-icon.png";
			InputStream defaultIconStream = this.getClass().getClassLoader().getResourceAsStream(defaultIconResource);
			
			defaultIconData = getBytes(defaultIconStream);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		VtImage defaultImage = new VtImage();
		defaultImage.setData(defaultIconData);
		
		setModel(defaultImage);
	}
	
	public void setModel(VtImage image) {
		
		this.image = null;
		
		if (image.getData() == null) {
			image.setData(defaultIconData);
			setIcon(new ImageIcon(defaultIconData));
		}
		else {
			setIcon(new ImageIcon(image.getData()));			
		}
		
		this.image = image;
	}
	
	private class LoadIconListener implements ActionListener {
		
		private JFileChooser fc;
		
		public LoadIconListener() {
			
			fc = new JFileChooser("Choose Your Profile Icon");
			
			fc.setFileFilter(new FileFilter(){
				
				public boolean accept(File f) {
					
					if (f.isDirectory()) {
						return true;
					}
					
					String name = f.getName();
					String[] suffixes = new String[]{ ".png", ".jpg", ".gif" };
					
					for (String suffix : suffixes) {
						if (name.toLowerCase().endsWith(suffix)) {
							return true;
						}
					}
					
					return false;
				}
				
				public String getDescription() {
					return "Image files";
				}
			});
		}
		
		public void actionPerformed(ActionEvent e) {
			
			if (image == null) {
				return;
			}
			
			int response = fc.showOpenDialog(ImageEditor.this);
			
			if (response != JFileChooser.APPROVE_OPTION) {
				return;
			}
			
			File file = fc.getSelectedFile();
			
			try {
				byte[] bytes = getBytes(new FileInputStream(file));
				setIcon(new ImageIcon(bytes));
				image.setData(bytes);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(ImageEditor.this, "Could not load icon " + file);
			}
		}
	}
	
	private byte[] getBytes(InputStream in) throws IOException {
		
		if (in == null) {
			throw new RuntimeException("Cannot load an icon from a null stream");
		}
		
		byte[] data;
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024 * 10]; //10k buffer
			for(int numRead = in.read(buffer); numRead!=-1; numRead = in.read(buffer)){
				out.write(buffer, 0, numRead);
			}

			in.close();
			out.close();
			
			data = out.toByteArray();
		}
		
		return data;
	}
	
	public static void main(String[] args) {
		new ImageEditor();
	}
}
