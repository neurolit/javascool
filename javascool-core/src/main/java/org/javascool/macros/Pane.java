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
package org.javascool.macros;

import org.javascool.core.ProgletApplet;
import org.javascool.widgets.Console;

import java.awt.*;

/**
 * Cette factory contient des fonctions de contrôle du panel de la proglet
 * rendues visibles à l'utilisateur de proglets.
 *
 * @serial exclude
 * @see <a href="Pane.java.html">code source</a>
 */
public class Pane {
    // @factory

    private Pane() {
    }

    /**
     * Rend visible le panneau graphique de la proglet.
     */
    public static void focusOnProgletPanel() {
        ProgletApplet instance = ProgletApplet.getInstance();
        if (instance != null) {
            instance.focusOnProgletPanel();
        }
    }

    /**
     * Rend visible la console de la proglet.
     */
    public static void focusOnConsolePanel() {
        ProgletApplet instance = ProgletApplet.getInstance();
        if (instance != null) {
            instance.focusOnConsolePanel();
        }
    }

    // Renvoie la console actuelle si elle existe
    static Console.Listener getConsoleListener() {
        ProgletApplet instance = ProgletApplet.getInstance();
        return instance == null ? null : instance.getConsole() == null ? null
                : instance.getConsole().getListener();
    }

    /**
     * Renvoie le panneau graphique de la proglet courante.
     *
     * @return Le panneau graphique de la proglet courante ou null si il n'est
     *         pas défini.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> T getProgletPane() {
        ProgletApplet instance = ProgletApplet.getInstance();
        Component c = instance != null ? instance.getPane() : null;
        return (T) c;
    }
}
