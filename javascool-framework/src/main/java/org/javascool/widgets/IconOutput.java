/*
 * $file.name
 * Copyright (C) 2012 Philippe VIENNE
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.javascool.widgets;

// Used to define the gui

import org.javascool.tools.FileManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

// Used to manipulate the image
// Used to define a click

/**
 * Panneau pour le tracé d'images pixeliques.
 *
 * @serial exclude
 * @see <a href="IconOutput.java.html">source code</a>
 */
public class IconOutput extends JPanel {
    private static final long serialVersionUID = 1L;

    // @bean
    public IconOutput() {
        setBackground(Color.GRAY);
        setPreferredSize(new Dimension(550, 550));
        reset(550, 550);
    }

    /**
     * Routine interne de tracé, ne pas utiliser.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        setBounds();
        g.setPaintMode();
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int ij = i + j * width;
                if ((0 <= ij) && (ij < image.length)) {
                    g.setColor(image[ij]);
                    g.fillRect(i0 + i * dij, j0 + j * dij, dij, dij);
                }
            }
        }
        Graphics2D g2d = (Graphics2D) g;
        paint2D(g2d);
    }

    /**
     * Cette routine est appellée à chaque tracé et permet de définir un tracé
     * spécifique au dessus de l'image affichée.
     * - Pour utiliser cette foncctionnalité, il faut définir:
     * <p/>
     * <pre>
     * class MyIconInput extends IconInput {
     *     public void paint2D(Graphics2D g) {
     *         // Ici ajouter les g.drawLine g.fillOval g.drawRect g.fillRect souhaité.
     *     }
     * }
     * </pre>
     *
     * @param g2d L'environnement graphique 2D à utiliser pour peindre.
     */
    public void paint2D(Graphics2D g2d) {
    }

    private void setBounds() {
        int di = width > 0 && getWidth() >= width && zoom ? getWidth() / width
                : 1;
        int dj = height > 0 && getHeight() >= height && zoom ? getHeight()
                / height : 1;
        dij = di < dj ? di : dj;
        i0 = (getWidth() - width * dij) / 2;
        j0 = (getHeight() - height * dij) / 2;
    }

    /**
     * Efface et initialize l'image.
     *
     * @param width  Taille horizontale de l'image.
     * @param height Taille verticale de l'image.
     * @param zoom   Ajuste automatiquement la taille de l'image au display si true
     *               (par défaut), sinon fixe 1 pixel de l'image à 1 pixel de
     *               l'affichage.
     * @return Cet objet, permettant de définir la construction
     *         <tt>new IconOutput().reset(..)</tt>.
     */
    public final IconOutput reset(int width, int height, boolean zoom) {
        if ((width > 550) || (height > 550) || (width * height > 550 * 550)) {
            throw new IllegalArgumentException("L'image est trop grande ("
                    + width + ", " + height + ") !");
        }
        this.zoom = zoom;
        
        /*
         * if (width <= 0)
         * width = 300;
         * if (height <= 0)
         * height = 300;
         * if(width % 2 == 0)
         * width++;
         * if(height % 2 == 0)
         * height++;
         */
        image = new Color[(this.width = width) * (this.height = height)];
        for (int ij = 0; ij < this.width * this.height; ij++) {
            image[ij] = Color.WHITE;
        }
        repaint(0, 0, getWidth(), getHeight());
        return this;
    }

    /**
     * @see #reset(int, int, boolean)
     */
    public final IconOutput reset(int width, int height) {
        return reset(width, height, true);
    }

    /**
     * Initialize l'image à partir d'un fichier.
     *
     * @param location L'URL (Universal Resource Location) de l'image.
     * @param zoom     Ajuste automatiquement la taille de l'image au display si true
     *                 (par défaut), sinon fixe 1 pixel de l'image à 1 pixel de
     *                 l'affichage.
     * @return Cet objet, permettant de définir la construction
     *         <tt>new IconOutput().reset(..)</tt>.
     */
    public IconOutput reset(String location, boolean zoom) throws IOException {
        // Fait 2//3 essais sur l'URL si besoin
        for (int n = 0; n < 3; n++) {
            BufferedImage img = ImageIO.read(FileManager
                    .getResourceURL(location));
            if (img != null) {
                return reset(img, zoom);
            }
        }
        throw new IOException("Unable to load the image " + location);
    }

    /**
     * @see #reset(String, boolean)
     */
    public final IconOutput reset(String location) throws IOException {
        return reset(location, true);
    }

    /**
     * Initialize l'image à partir d'une image en mémoire.
     *
     * @param img  L'image qui va initialiser le tracé.
     * @param zoom Ajuste automatiquement la taille de l'image au display si true
     *             (par défaut), sinon fixe 1 pixel de l'image à 1 pixel de
     *             l'affichage.
     * @return Cet objet, permettant de définir la construction
     *         <tt>new IconOutput().reset(..)</tt>.
     */
    public IconOutput reset(BufferedImage img, boolean zoom) {
        reset(img.getWidth(), img.getHeight(), zoom);
        for (int j = 0; j < img.getHeight(); j++) {
            for (int i = 0; i < img.getWidth(); i++) {
                image[i + width * j] = new Color(img.getRGB(i, j));
            }
        }
        repaint(0, 0, getWidth(), getHeight());
        return this;
    }

    /**
     * @see #reset(java.awt.image.BufferedImage, boolean)
     */
    public final IconOutput reset(BufferedImage img) {
        return reset(img, true);
    }

    /**
     * Renvoie les dimensions de l'image.
     */
    public Dimension getDimension() {
        return new Dimension(width, height);
    }

    /**
     * Renvoie une image dans laquelle le contenu de l'affichage est copié.
     *
     * @return Le contenu de l'affichage sous forme d'image.
     */
    public BufferedImage getImage() {
        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int j = 0; j < img.getHeight(); j++) {
            for (int i = 0; i < img.getWidth(); i++) {
                img.setRGB(i, j, image[i + width * j].getRGB());
            }
        }
        return img;
    }

    /**
     * Définit la valeur d'un pixel.
     *
     * @param x Abscisse du pixel, dans {0, width{.
     * @param y Ordonnée du pixel, dans {0, height{.
     * @param c Couleur: "black" (default), "blue", "cyan", "gray", "green",
     *          "magenta", "orange", "pink", "red", "white", "yellow".
     * @return La valeur true si le pixel est dans les limites de l'image, false
     *         sinon.
     */
    public boolean set(int x, int y, String c) {
        return set(x, y, IconOutput.getColor(c));
    }

    /**
     * Définit la valeur d'un pixel.
     *
     * @param x Abscisse du pixel, dans {0, width{.
     * @param y Ordonnée du pixel, dans {0, height{.
     * @param v L'intensité en niveau de gris du pixel de 0 (noir) à 255
     *          (blanc).
     * @return La valeur true si le pixel est dans les limites de l'image, false
     *         sinon.
     */
    public boolean set(int x, int y, int v) {
        v = v < 0 ? 0 : v > 255 ? 255 : v;
        return set(x, y, new Color(v, v, v));
    }

    /**
     * Définit la valeur d'un pixel.
     *
     * @param x Abscisse du pixel, dans {0, width{.
     * @param y Ordonnée du pixel, dans {0, height{.
     * @param c L'intensité en couleur du pixel.
     * @return La valeur true si le pixel est dans les limites de l'image, false
     *         sinon.
     */
    public boolean set(int x, int y, Color c) {
        if ((0 <= x) && (x < width) && (0 <= y) && (y < height)) {
            setBounds();
            int ij = x + y * width;
            image[ij] = c;
            repaint(i0 + x * dij, j0 + y * dij, dij, dij);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Renvoie la valeur d'un pixel.
     *
     * @param x Abscisse du pixel, dans {0, width{.
     * @param y Ordonnée du pixel, dans {0, height{.
     * @return L'intensite du pixel entre 0 et 255 ou 0 si le pixel n'est pas
     *         dans l'image.
     */
    public int getIntensity(int x, int y) {
        if ((0 <= x) && (x < width) && (0 <= y) && (y < height)) {
            Color c = image[x + y * width];
            return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        } else {
            return 0;
        }
    }

    /**
     * Renvoie la valeur d'un pixel.
     *
     * @param x Abscisse du pixel, dans {0, width{.
     * @param y Ordonnée du pixel, dans {0, height{.
     * @return La couleur du pixel ou "undefined" si le pixel n'est pas dans
     *         l'image.
     */
    public String getColor(int x, int y) {
        if ((0 <= x) && (x < width) && (0 <= y) && (y < height)) {
            Color c = image[x + y * width];
            return IconOutput.colors.containsKey(c) ? IconOutput.colors.get(c)
                    : c.toString();
        } else {
            return "undefined";
        }
    }

    /**
     * Renvoie la valeur d'un pixel.
     *
     * @param x Abscisse du pixel, dans {0, width{.
     * @param y Ordonnée du pixel, dans {0, height{.
     * @return La couleur du pixel ou black si le pixel n'est pas dans l'image.
     */
    public Color getPixelColor(int x, int y) {
        if ((0 <= x) && (x < width) && (0 <= y) && (y < height)) {
            Color c = image[x + y * width];
            return c;
        } else {
            return Color.BLACK;
        }
    }

    private Color image[];
    private int width, height, i0, j0, dij;
    boolean zoom = true;

    private static HashMap<Color, String> colors = new HashMap<Color, String>();

    private static Color getColor(String color) {
        try {
            return (Color) Class.forName("java.awt.Color").getField(color)
                    .get(null);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    private static void putColors(String color) {
        IconOutput.colors.put(IconOutput.getColor(color), color);
    }

    static {
        IconOutput.putColors("black");
        IconOutput.putColors("blue");
        IconOutput.putColors("cyan");
        IconOutput.putColors("gray");
        IconOutput.putColors("green");
        IconOutput.putColors("magenta");
        IconOutput.putColors("orange");
        IconOutput.putColors("pink");
        IconOutput.putColors("red");
        IconOutput.putColors("white");
        IconOutput.putColors("yellow");
    }

    /**
     * Renvoie la position horizontale du dernier clic de souris dans l'image.
     */
    public int getClicX() {
        return clicX;
    }

    /**
     * Renvoie la position verticale du dernier clic de souris dans l'image.
     */
    public int getClicY() {
        return clicY;
    }

    private int clicX = 0, clicY = 0;

    {
        addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // x = i0 + i * dij, y = j0 + j * dij
                clicX = (e.getX() - i0) / dij;
                clicY = (e.getY() - j0) / dij;
                if (runnable != null) {
                    new Thread(runnable).start();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    /**
     * Définit une portion de code appellée à chaque clic de souris.
     *
     * @param runnable La portion de code à appeler, ou null si il n'y en a pas.
     * @return Cet objet, permettant de définir la construction
     *         <tt>new CurveOutput().setRunnable(..)</tt>.
     */
    public IconOutput setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    private Runnable runnable = null;
}
