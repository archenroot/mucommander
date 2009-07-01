/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.button;

import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;

/*
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

/**
 * Provides a way for toolbar buttons to be displayed when the toolbar itself doesn't have enough space for all buttons.
 * The buttons that are cropped are displayed in a secondary toolbar, using a vertical layout.
 * <p>
 * To use this Feature replace:
 * <code>
 *   frame.getContentPane().add(toolbar, BorderLayout.NORTH);
 * </code>
 * with 
 * <code>
 *   frame.getContentPane().add(MoreButton.wrapToolBar(toolBar), BorderLayout.NORTH);
 * </code>
 * </p>
 * <p>
 * This class is based on the code of Santhosh Kumar T, see
 * <a href="http://www.jroller.com/santhosh/entry/jtoolbar_with_more_button">this link</a> for more information.
 * </p>
 *
 * @author Santhosh Kumar T, Leo Welsch
 */
public class ToolbarMoreButton extends JToggleButton implements ActionListener {

  private static JToolBar moreToolbar;
  JToolBar toolbar;

  protected ToolbarMoreButton(final JToolBar toolbar) {
    super(IconManager.getIcon(IconManager.COMMON_ICON_SET, "more.png"));
    this.toolbar = toolbar;
    addActionListener(this);
    setFocusPainted(false);

    setMargin(new Insets(0, 0, 0, 0));
    setContentAreaFilled(false);
    setBorderPainted(false);
//    setMinimumSize(new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight()));
    // Use new JButton decorations introduced in Mac OS X 10.5 (Leopard) with
    // Java 1.5 and up
    if (OsFamilies.MAC_OS_X.isCurrent()
        && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher()
        && JavaVersions.JAVA_1_5.isCurrentOrHigher()) {
      putClientProperty("JComponent.sizeVariant", "small");
      putClientProperty("JButton.buttonType", "textured");
    }

    // paint border only when necessary
    addMouseListener(new MouseAdapter() {

      public void mouseExited(MouseEvent e) {
        setBorderPainted(false);
      }

      public void mouseEntered(MouseEvent e) {
        setBorderPainted(true);
      }
    });

    // hide & seek
    toolbar.addComponentListener(new ComponentAdapter() {

      public void componentResized(ComponentEvent e) {
        final boolean aFlag = !isVisible(toolbar.getComponent(toolbar.getComponentCount() - 1),
                              null);
        setVisible(aFlag);
        moreToolbar.setVisible(aFlag);
      }
    });
  }

    // check visibility
  // partially visible is treated as not visible
  private boolean isVisible(Component comp, Rectangle rect) {
    if (rect == null) {
      rect = toolbar.getVisibleRect();
    }
    return comp.getLocation().x + comp.getWidth() <= rect.getWidth();
  }

  public void actionPerformed(ActionEvent e) {
    Component[] comp = toolbar.getComponents();
    Rectangle visibleRect = toolbar.getVisibleRect();
    for (int i = 0; i < comp.length; i++) {
      if (!isVisible(comp[i], visibleRect)) {
        JPopupMenu popup = new JPopupMenu();
        for (; i < comp.length; i++) {
          if (comp[i] instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) comp[i];
            if (button.getAction() != null) {
              popup.add(button.getAction());
            }
          } else if (comp[i] instanceof JSeparator) {
            popup.addSeparator();
          }
        }

        // on popup close make more-button unselected
        popup.addPopupMenuListener(new PopupMenuListener() {

          public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            setSelected(false);
          }

          public void popupMenuCanceled(PopupMenuEvent e) {
          }

          public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          }
        });
        popup.show(this, 0, getHeight());
      }
    }
  }

  public static Component wrapToolBar(JToolBar toolbar) {
    moreToolbar = new JToolBar();
    moreToolbar.setRollover(true);
    moreToolbar.setFloatable(false);
    moreToolbar.add(new ToolbarMoreButton(toolbar));
    moreToolbar.setBorderPainted(false);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(toolbar, BorderLayout.CENTER);
    panel.add(moreToolbar, BorderLayout.EAST);

    return panel;
  }
}