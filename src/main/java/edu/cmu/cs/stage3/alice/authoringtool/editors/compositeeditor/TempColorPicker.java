/*
 * Copyright (c) 1999-2003, Carnegie Mellon University. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * 3. Products derived from the software may not be called "Alice",
 *    nor may "Alice" appear in their name, without prior written
 *    permission of Carnegie Mellon University.
 *
 * 4. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    "This product includes software developed by Carnegie Mellon University"
 */

package edu.cmu.cs.stage3.alice.authoringtool.editors.compositeeditor;


/**
 * @author David Culyba, Dennis Cosgrove
 */

import edu.cmu.cs.stage3.lang.Messages;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class TempColorPicker extends javax.swing.JPanel {
    protected CompositeElementEditor editor;
	private class TileColorPicker extends javax.swing.JPanel{
		public java.awt.Color toChange;
		public java.awt.Color foregroundToChange;
		public javax.swing.JLabel tile;
		public javax.swing.JButton openPicker;
		public javax.swing.JButton openPicker2;
		public String nameKey;
		public Object classKey;
		public String foregroundNameKey;


		public TileColorPicker(String nameKey, Object classKey){
			this.toChange = edu.cmu.cs.stage3.alice.authoringtool.AuthoringToolResources.getColor(nameKey);
			this.nameKey = nameKey;
			this.classKey = classKey;
			String name = edu.cmu.cs.stage3.alice.authoringtool.AuthoringToolResources.getReprForValue(classKey);
			tile = new javax.swing.JLabel(name);
			tile.setOpaque(true);
			tile.setBackground(toChange);
			openPicker = new javax.swing.JButton(Messages.getString("select_color")); 
			openPicker.addActionListener(new java.awt.event.ActionListener(){
				public void actionPerformed( java.awt.event.ActionEvent ev ) {
					javax.swing.JColorChooser colorChooser = new javax.swing.JColorChooser();
					java.awt.Color newColor = edu.cmu.cs.stage3.swing.DialogManager.showDialog( colorChooser, Messages.getString("Pick_color_for_")+TileColorPicker.this.tile.getText(), TileColorPicker.this.toChange ); 
					TileColorPicker.this.tile.setBackground(newColor);
					TileColorPicker.this.toChange = newColor;
					edu.cmu.cs.stage3.alice.authoringtool.AuthoringToolResources.putColor(TileColorPicker.this.nameKey, newColor);
					editor.guiInit();
					TileColorPicker.this.repaint();
					editor.repaint();
				}
			});
			this.setLayout(new java.awt.GridBagLayout());
			this.add(tile,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			this.add(openPicker,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 8, 0, 0), 0, 0));
			this.setBorder(javax.swing.BorderFactory.createEmptyBorder(4,4,4,4));
		}

		public TileColorPicker(String nameKey, Class classKey, String foregroundNameKey){
			this(nameKey, classKey);
			this.foregroundNameKey = foregroundNameKey;
			this.foregroundToChange = edu.cmu.cs.stage3.alice.authoringtool.AuthoringToolResources.getColor(foregroundNameKey);
			tile.setForeground(this.foregroundToChange);
			openPicker2 = new javax.swing.JButton(Messages.getString("select_text_color")); 
			openPicker2.addActionListener(new java.awt.event.ActionListener(){
				public void actionPerformed( java.awt.event.ActionEvent ev ) {
					javax.swing.JColorChooser colorChooser = new javax.swing.JColorChooser();
					java.awt.Color newColor = edu.cmu.cs.stage3.swing.DialogManager.showDialog( colorChooser, Messages.getString("Pick_color_for_")+TileColorPicker.thi