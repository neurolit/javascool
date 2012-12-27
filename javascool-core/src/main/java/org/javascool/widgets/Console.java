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

import org.javascool.tools.FileManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Définit une zone d'affichage qui permet de recevoir les messages de la
 * console.
 *
 * @author Philippe Vienne
 * @serial exclude
 * @see <a href="Console.java.html">code source</a>
 */
public class Console extends JPanel {
    private static final long serialVersionUID = 1L;

    /**
     * Écouteur de ce qui est affiché à la console.
     */
    public static abstract class Listener {
        /**
         * Routine appellée à chaque sortie standard.
         */
        public abstract void print(String string);

        /**
         * Efface le contenu de ce qui a été écouté de la console.
         */
        public void clear() {
            text = "";
        }

        /**
         * Retourne le contenu de ce qui a été écouté de la console.
         */
        public String getText() {
            return text;
        }

        /**
         * Sauve le contenu de ce qui a été écouté de la console dans un
         * fichier.
         *
         * @param location La localisation (chemin du fichier ou localisation
         *                 internet) où sauver le texte.
         */
        public void save(String location) {
            FileManager.save(location, text);
        }

        private String text = "";
    }

    private static ArrayList<Listener> listeners = new ArrayList<Listener>();

    /**
     * Ajoute un écouteur de ce qui est affiché à la console.
     *
     * @param listener Un écouteur de ce qui est affiché à la console.
     */
    public static void addPrintListener(Listener listener) {
        if (Console.listeners.size() == 0) {
            Console.startPrintListening();
        }
        Console.listeners.add(listener);
    }

    /**
     * Enlève un écouteur de ce qui est affiché à la console.
     *
     * @param listener Un écouteur de ce qui est affiché à la console.
     */
    public static void removePrintListener(Listener listener) {
        Console.listeners.remove(listener);
    }

    /**
     * Affiche du texte dans la console.
     *
     * @param text Le texte à afficher.
     */
    public static void print(String text) {
        Console.stdout.print(text);
        for (String p : Console.prefixes) {
            if (text.startsWith(p)) {
                return;
            }
        }
        for (Listener listener : Console.listeners) {
            listener.text += text;
            listener.print(text);
        }
    }

    private static PrintStream stdout = new PrintStream(System.out, true);

    // Messages parasites supprimés à l'affichage
    private static final String prefixes[] = {"=== Minim Error ===",
            "=== Likely buffer underrun in AudioOutput.",
            "==== JavaSound Minim Error ====",
            "<<<< debug >>>> "};

    // Mets en place le mécanisme d'écoute de la console
    private static void startPrintListening() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                Console.print(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                Console.print(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
        System.setOut(new PrintStream(out, true));
    }

    /**
     * Construit un panneau graphique d'affichage de la console.
     */
    public Console() {
        setLayout(new BorderLayout());
        // Construit la zone d'affichage
        outputPane = new JTextArea();
        outputPane.setEditable(false);
        outputPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        float[] bg = Color.RGBtoHSB(200, 200, 200, null);
        outputPane.setBackground(Color.getHSBColor(bg[0], bg[1], bg[2]));
        JScrollPane scrolledOutputPane = new JScrollPane(outputPane);
        add(scrolledOutputPane, BorderLayout.CENTER);
        // Construit la zone des bouttons
        toolbar = new ToolBar();
        toolbar.addTool("Effacer", "org/javascool/widgets/icons/erase.png",
                new Runnable() {
                    @Override
                    public void run() {
                        listener.clear();
                    }
                });
        toolbar.addTool("Copier tout",
                "org/javascool/widgets/icons/copyAll.png", new Runnable() {
            @Override
            public void run() {
                outputPane.selectAll();
                outputPane.copy();
            }
        });
        toolbar.addTool("Copier sélection",
                "org/javascool/widgets/icons/copySelection.png",
                new Runnable() {
                    @Override
                    public void run() {
                        outputPane.copy();
                    }
                });
        toolbar.addSeparator();
        this.add(toolbar, BorderLayout.NORTH);
        Console.addPrintListener(listener = new Listener() {
            @Override
            public void print(String text) {
                outputPane.setText(outputPane.getText() + text);
            }

            @Override
            public void clear() {
                super.clear();
                outputPane.setText("");
            }
        });
    }

    // Listener
    private Listener listener;
    // Barre de menu
    private final ToolBar toolbar;
    // Zone d'affichage
    private final JTextArea outputPane;

    /**
     * Renvoie l'écouteur de console associé à ce panneau grapghique.
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * Renvoie la barre de menu de la console pour ajouter des éléments.
     *
     * @return La barre de menu de la console.
     */
    public ToolBar getToolBar() {
        return toolbar;
    }
}
