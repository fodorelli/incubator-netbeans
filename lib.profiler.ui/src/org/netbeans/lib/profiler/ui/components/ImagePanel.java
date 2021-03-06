/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.profiler.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.PixelGrabber;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 *
 * @author Jiri Sedlacek
 */
public class ImagePanel extends JPanel {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static MediaTracker mTracker = new MediaTracker(new JPanel());

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Image image;
    private int imageAlign; // Use SwingConstants.TOP, BOTTOM (LEFT & RIGHT not implemented)

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ImagePanel(Image image) {
        this(image, SwingConstants.TOP);
    }

    public ImagePanel(Image image, int imageAlign) {
        setImage(image);
        setImageAlign(imageAlign);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setImage(Image image) {
        this.image = loadImage(image);

        if (this.image == null) {
            throw new RuntimeException("Failed to load image"); // NOI18N
        }

        setPreferredBackground();
        setPreferredSize(new Dimension(this.image.getWidth(null), this.image.getHeight(null)));

        refresh();
    }

    public void setImageAlign(int imageAlign) {
        this.imageAlign = imageAlign;

        setPreferredBackground();

        refresh();
    }

    protected static Image loadImage(Image image) {
        mTracker.addImage(image, 0);

        try {
            mTracker.waitForID(0);
        } catch (InterruptedException e) {
            return null;
        }

        mTracker.removeImage(image, 0);

        return image;
    }

    protected void setPreferredBackground() {
        int[] pixels = new int[1];

        PixelGrabber pg = null;

        switch (imageAlign) {
            case (SwingConstants.TOP):
                pg = new PixelGrabber(image, 0, image.getHeight(null) - 1, 1, 1, pixels, 0, 1);

                break;
            case (SwingConstants.BOTTOM):
                pg = new PixelGrabber(image, 0, 0, 1, 1, pixels, 0, 1);

                break;
            default:
                pg = new PixelGrabber(image, 0, image.getHeight(null) - 1, 1, 1, pixels, 0, 1);
        }

        try {
            if ((pg != null) && pg.grabPixels()) {
                setBackground(new Color(pixels[0]));
            }
        } catch (InterruptedException e) {
        }
    }

    protected void paintComponent(Graphics graphics) {
        graphics.setColor(getBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        switch (imageAlign) {
            case (SwingConstants.TOP):
                graphics.drawImage(image, (getWidth() - image.getWidth(null)) / 2, 0, this);

                break;
            case (SwingConstants.BOTTOM):
                graphics.drawImage(image, (getWidth() - image.getWidth(null)) / 2, getHeight() - image.getHeight(null), this);

                break;
            default:
                graphics.drawImage(image, (getWidth() - image.getWidth(null)) / 2, 0, this);
        }
    }

    private void refresh() {
        if (isShowing()) {
            invalidate();
            repaint();
        }
    }
}
