/*
 *  Copyright 2010-2017 Thibaut Voquet <contact@kuwaiba.org>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.core.services.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Provides a menu scroller for those which has too many items
 * @author Thibaut Voquet <contact@kuwaiba.org>
 */
public class MenuScroller
{
	private JPopupMenu menu;
	private Component[] menuItems;
	private MenuScrollItem upItem;
	private MenuScrollItem downItem;
	private final MenuScrollListener menuListener = new MenuScrollListener();
	private int scrollCount;
	private int interval;
	private int topFixedCount;
	private int bottomFixedCount;
	private int firstIndex = 0;
	private int keepVisibleIndex = -1;
	
	public MenuScroller(JMenu menu)
	{
		this(menu, 15);
	}
	
	public static MenuScroller setScrollerFor(JMenu menu)
	{
		return new MenuScroller(menu);
	}
	
	public MenuScroller(JPopupMenu menu)
	{
		this(menu, 15);
	}
	
	public MenuScroller(JMenu menu, int scrollCount)
	{
		this(menu, scrollCount, 150);
	}
	
	public MenuScroller(JPopupMenu menu, int scrollCount)
	{
		this(menu, scrollCount, 150);
	}
	
	public MenuScroller(JMenu menu, int scrollCount, int interval)
	{
		this(menu, scrollCount, interval, 0, 0);
	}
	
	public MenuScroller(JPopupMenu menu, int scrollCount, int interval)
	{
		this(menu, scrollCount, interval, 0, 0);
	}
	
	public MenuScroller(JMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount)
	{
		this(menu.getPopupMenu(), scrollCount, interval, topFixedCount, bottomFixedCount);
	}
	
	public MenuScroller(JPopupMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount)
	{
		if (scrollCount <= 0 || interval <= 0)
		{
			throw new IllegalArgumentException("scrollCount and interval must be greater than 0");
		}
		if (topFixedCount < 0 || bottomFixedCount < 0)
		{
			throw new IllegalArgumentException("topFixedCount and bottomFixedCount cannot be negative");
		}
	
		upItem = new MenuScrollItem(MenuIcon.UP, -1);
		downItem = new MenuScrollItem(MenuIcon.DOWN, +1);
		setScrollCount(scrollCount);
		setInterval(interval);
		setTopFixedCount(topFixedCount);
		setBottomFixedCount(bottomFixedCount);
	
		this.menu = menu;
		menu.addPopupMenuListener(menuListener);
	}
	
	public int getInterval()
	{
		return interval;
	}

	public void setInterval(int interval)
	{
		if (interval <= 0)
		{
			throw new IllegalArgumentException("interval must be greater than 0");
		}
		upItem.setInterval(interval);
		downItem.setInterval(interval);
		this.interval = interval;
	}

	public int getscrollCount()
	{
		return scrollCount;
	}

	public void setScrollCount(int scrollCount)
	{
		if (scrollCount <= 0)
		{
			throw new IllegalArgumentException("scrollCount must be greater than 0");
		}
		this.scrollCount = scrollCount;
		MenuSelectionManager.defaultManager().clearSelectedPath();
	}

	public int getTopFixedCount()
	{
		return topFixedCount;
	}

	public void setTopFixedCount(int topFixedCount)
	{
		if (firstIndex <= topFixedCount)
			firstIndex = topFixedCount;
		else
			firstIndex += (topFixedCount - this.topFixedCount);
		this.topFixedCount = topFixedCount;
	}

	public int getBottomFixedCount()
	{
		return bottomFixedCount;
	}

	public void setBottomFixedCount(int bottomFixedCount)
	{
		this.bottomFixedCount = bottomFixedCount;
	}

	public static MenuScroller setScrollerFor(JPopupMenu menu) {
		return new MenuScroller(menu);
	}

	public static MenuScroller setScrollerFor(JMenu menu, int scrollCount) {
		return new MenuScroller(menu, scrollCount);
	}

	public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount) {
		return new MenuScroller(menu, scrollCount);
	}

	public static MenuScroller setScrollerFor(JMenu menu, int scrollCount, int interval) {
		return new MenuScroller(menu, scrollCount, interval);
	}

	public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount, int interval) {
		return new MenuScroller(menu, scrollCount, interval);
	}

	public static MenuScroller setScrollerFor(JMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount)
	{
		return new MenuScroller(menu, scrollCount, interval, topFixedCount, bottomFixedCount);
	}

	public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount)
	{
		return new MenuScroller(menu, scrollCount, interval, topFixedCount, bottomFixedCount);
	}
	
	public void keepVisible(JMenuItem item)
	{
		if (item == null)
		{
			keepVisibleIndex = -1;
		}
		else
		{
			int index = menu.getComponentIndex(item);
			keepVisibleIndex = index;
		}
	}

	public void keepVisible(int index)
	{
		keepVisibleIndex = index;
	}

	public void dispose()
	{
		if (menu != null)
		{
			menu.removePopupMenuListener(menuListener);
			menu = null;
		}
	}

	@Override
	public void finalize() throws Throwable
	{
		dispose();
	}

	private void refreshMenu()
	{
		if (menuItems != null && menuItems.length > 0)
		{
			firstIndex = Math.max(topFixedCount, firstIndex);
			firstIndex = Math.min(menuItems.length - bottomFixedCount - scrollCount, firstIndex);
	
			upItem.setEnabled(firstIndex > topFixedCount);
			downItem.setEnabled(firstIndex + scrollCount < menuItems.length - bottomFixedCount);
	
			menu.removeAll();
			for (int i = 0; i < topFixedCount; i++)
			{
				menu.add(menuItems[i]);
			}
			if (topFixedCount > 0)
			{
				menu.add(new JSeparator());
			}
	
			menu.add(upItem);
			for (int i = firstIndex; i < scrollCount + firstIndex; i++)
			{
				if( i == scrollCount + firstIndex -1 && menuItems[i] instanceof JPopupMenu.Separator )
					continue; // Skip separator if it is the last component in the scrolling list
				menu.add(menuItems[i]);
			}
			menu.add(downItem);
	
			if (bottomFixedCount > 0)
			{
				menu.add(new JSeparator());
			}
			for (int i = menuItems.length - bottomFixedCount; i < menuItems.length; i++) {
				menu.add(menuItems[i]);
			}
	
			JComponent parent = (JComponent) upItem.getParent();
			parent.revalidate();
			parent.repaint();
		}
	}
	
	private class MenuScrollListener implements PopupMenuListener
	{
		
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e)
		{
			setMenuItems();
		}
		
		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
		{
			restoreMenuItems();
		}
		
		@Override
		public void popupMenuCanceled(PopupMenuEvent e)
		{
			restoreMenuItems();
		}
		
		private void setMenuItems()
		{
			menuItems = menu.getComponents();
		
			if (keepVisibleIndex >= topFixedCount && keepVisibleIndex <= menuItems.length - bottomFixedCount && (keepVisibleIndex > firstIndex + scrollCount || keepVisibleIndex < firstIndex))
			{
				firstIndex = Math.min(firstIndex, keepVisibleIndex);
				firstIndex = Math.max(firstIndex, keepVisibleIndex - scrollCount + 1);
			}
			if (menuItems.length > topFixedCount + scrollCount + bottomFixedCount)
			{
				refreshMenu();
			}
		}
		
		private void restoreMenuItems()
		{
			menu.removeAll();
			for (Component component : menuItems)
			{
				menu.add(component);
			}
		}
	}
	
	private class MenuScrollTimer extends Timer
	{
		public MenuScrollTimer(final int increment, int interval)
		{
			super(interval, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					firstIndex += increment;
					refreshMenu();
				}
			});
		}
	}
	
	private class MenuScrollItem extends JMenuItem implements ChangeListener
	{
		private MenuScrollTimer timer;
		
		public MenuScrollItem(MenuIcon icon, int increment)
		{
			setIcon(icon);
			setDisabledIcon(icon);
			timer = new MenuScrollTimer(increment, interval);
			addChangeListener(this);
		}
		
		public void setInterval(int interval)
		{
			timer.setDelay(interval);
		}
		
		@Override
		public void stateChanged(ChangeEvent e)
		{
			if (isArmed() && !timer.isRunning())
			{
				timer.start();
			}
			if (!isArmed() && timer.isRunning())
			{
				timer.stop();
			}
		}
	}
	
	private enum MenuIcon implements Icon
	{
		
		UP(9, 1, 9),
		DOWN(1, 9, 1);
		final int[] xPoints = {1, 5, 9};
		final int[] yPoints;
		
		MenuIcon(int... yPoints)
		{
			this.yPoints = yPoints;
		}
		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			Dimension size = c.getSize();
			Graphics g2 = g.create(size.width / 2 - 5, size.height / 2 - 5, 10, 10);
			g2.setColor(Color.GRAY);
			g2.drawPolygon(xPoints, yPoints, 3);
			if (c.isEnabled())
			{
				g2.setColor(Color.BLACK);
				g2.fillPolygon(xPoints, yPoints, 3);
			}
			g2.dispose();
		}
		
		@Override
		public int getIconWidth()
		{
			return 0;
		}
		
		@Override
		public int getIconHeight()
		{
			return 10;
		}
	}
}
