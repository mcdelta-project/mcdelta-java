/*
 * Copyright (C) 2015 CMAN Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cman;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author CMAN Team
 */
@SuppressWarnings("serial")
public class Visual extends JFrame
{
	final List<String> holder = new LinkedList<String>();
	JTextField text = new JTextField("");
	JTextArea area = new JTextArea(25, 55);
	JScrollPane scroll = new JScrollPane(area);
	private boolean enterkey = false;
	private String out = "";
	
	public String getLine()
	{	    
		synchronized (holder) {
        // wait for input from field
        while (holder.isEmpty())
        {
			try 
	        {
				holder.wait();
			} 
	        catch (InterruptedException e1) 
	        {
				e1.printStackTrace();
			}
        }
        out = holder.remove(0);
        this.text.setText("");
        System.out.println(out);
		}
		return out;
	}
	
	public Visual(String version)
	{
		System.setOut(new PrintStream(new OutputStream() {
			
			@Override
			public void write(int b) throws IOException 
			{
				area.append(String.valueOf((char)b));
				area.setCaretPosition(area.getDocument().getLength());
			}
		}));
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scroll);
		this.add(text);
		this.area.setEditable(false);
		text.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) 
			{
				synchronized (holder)
				{
					holder.add(text.getText());
					holder.notify();
				}
			}
		});	
		this.pack();
		this.setResizable(false);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int desktopwidth = gd.getDisplayMode().getWidth();
		int desktopheight = gd.getDisplayMode().getHeight();
		this.setLocation((desktopwidth/2)-(this.getWidth()/2), (desktopheight/2)-(this.getHeight()/2));
		this.setTitle("CMAN-Java " + version);
		try 
		{
			this.setIconImage(ImageIO.read(getClass().getResource("/cman/cmanicon.png")));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}
