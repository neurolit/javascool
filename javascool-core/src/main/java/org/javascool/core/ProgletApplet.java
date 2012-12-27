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

package org.javascool.core;

// Running management

import org.javascool.macros.Macros;
import org.javascool.tools.Invoke;
import org.javascool.widgets.Console;
import org.javascool.widgets.StartStopButton;
import org.javascool.widgets.ToolBar;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Wrapper d'une proglet, applet ou composant graphique pour son exécution.
 * <p>
 * Permet de wrapper un objet graphique dans une page HTML avec une construction
 * de la forme <div>
 * <tt>&lt;applet code="ProgletApplet" archive="<i>les-classes-java-utilisés.jar</i>" width="600" height="800"&gt;</tt>
 * </div> <div>
 * <tt>&lt;param name="label" value="<i>titre-de-présentation-du-grain-logiciel (par défaut le nom  de la proglet)</i>"/&gt;</tt>
 * </div> <div>
 * <tt>&lt;param name="proglet" value="<i>nom-complet-qualifié-de-la-classe-java-de-l-objet-graphique</i>"/&gt;</tt>
 * </div> <div>
 * <tt>&lt;param name="program" value="<i>nom-complet-du-fichier-.class-ou-nom-qualifié-du-runnable-correspondant-au-programme-exécuté-dans-la-proglet</i>"/&gt;</tt>
 * </div> <div>
 * <tt>&lt;param name="notoolbar" value="true ou false (défaut)"/&gt;</tt></div>
 * <div><tt>&lt;/applet></tt></div>
 * </p>
 * <ul>
 * <li>L'option <tt>notoolbar</tt> permet de masquer la barre de contrôle de
 * l'applet.</li>
 * <li>L'objet graphique doit être un instance de <tt>java.awt.Component</tt>
 * donc n'importe quel composant «Swing» ou «AWT».</li>
 * <li>Si l'objet possède des méthodes <tt>init<tt>, <tt>destroy</tt>,
 * <tt>start</tt>, <tt>stop</tt> elles sont invoquées par les méthodes
 * correspondandes de l'applet.</li>
 * <li>Pour bloquer la fermeture intempestive du composant graphique ouvert en
 * application, il suffit que la méthode <tt>destroy</tt> lève une exception.</li>
 * </ul>
 *
 * @serial exclude
 * @see <a href="ProgletApplet.java.html">source code</a>
 */
public class ProgletApplet extends JApplet implements Runnable {
    public ProgletApplet() {
    }

    private static final long serialVersionUID = 1L;

    /**
     * Usage en tant qu'application.
     * Usage:<tt>$java org.javascool.core.ProgletApplet [label] &lt;proglet>
     */
    public static void main(String usage[]) {
        if (usage.length > 0) {
            ProgletApplet.open(usage.length == 1 ? "" : usage[0], 600, 800,
                    new ProgletApplet(usage.length == 1 ? null : usage[0],
                            usage.length == 1 ? usage[0] : usage[1]));
        }
    }

    // Construction de l'objet à partir du main
    private ProgletApplet(String l, String p) {
        hasAppletContext = false;
        label = l;
        proglet = program = p;
    }

    // Récupère les paramètres de l'applet
    private void initParameters() {
        if (hasAppletContext) {
            try {
                label = getParameter("label");
            } catch (Throwable e) {
            }
            try {
                proglet = getParameter("proglet");
            } catch (Throwable e) {
            }
            try {
                program = getParameter("program");
            } catch (Throwable e) {
            }
            try {
                notoolbar = getParameter("notoolbar").toLowerCase().equals(
                        "true");
            } catch (Throwable e) {
            }
            try {
                verbose = getParameter("verbose").toLowerCase().equals("true");
            } catch (Throwable e) {
            }
        }
        if (label == null && proglet != null) {
            label = "Proglet «"
                    + proglet
                    .replaceAll("(org.javascool.proglets.|.Panel)", "")
                    + "»";
        }
        doVerbose("Parameters : -label '" + label + "' -proglet '" + proglet
                + "' -program '" + program + "' -notoolbar '" + notoolbar + "'");
    }

    private boolean hasAppletContext = true;
    private String label = null;
    private String proglet = null;
    private String program = null;
    private boolean notoolbar = false;
    // Paramètre non commenté, pour la mise au point
    private boolean verbose = true;

    private void doVerbose(String message) {
        if (verbose) {
            System.err.println("ProgletApplet> " + message);
        }
    }

    // Construction du cadre graphique de l'affichage
    private void initContentPane() {
        getContentPane().removeAll();
        if (notoolbar) {
            getContentPane().add(
                    pane != null ? pane
                            : new JLabel("Pas de «proglet» à montrer",
                            SwingConstants.CENTER));
        } else {
            getContentPane().add(new ToolBar() {
                private static final long serialVersionUID = 1L;

                {
                    addTool("label", new JLabel(label));
                    if (runnable != null) {
                        addTool("run", startstop = new StartStopButton() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void start() {
                                ProgletApplet.this.run();
                            }

                            @Override
                            public void stop() {
                            }
                        });
                    }
                    if (pane != null) {
                        bt = addTool("show", new Runnable() {
                            @Override
                            public void run() {
                                if ("panel".equals(((JButton) getTool("show"))
                                        .getText())) {
                                    focusOnProgletPanel();
                                } else {
                                    focusOnConsolePanel();
                                }
                            }
                        });
                    }
                }
            }, BorderLayout.NORTH);
            getContentPane().add(cd = new JPanel(cl = new CardLayout()) {
                private static final long serialVersionUID = 1L;

                {
                    if (pane != null) {
                        add(pane, "panel");
                    }
                    add(console = new Console(), "console");
                }
            }, BorderLayout.CENTER);
        }
        focusOnProgletPanel();
    }

    private JButton bt = null;
    private JPanel cd;
    private CardLayout cl;
    private StartStopButton startstop = null;

    /**
     * Renvoie le panneau graphique de la proglet courante.
     *
     * @return Le panneau graphique de la proglet courante ou null si il n'est
     *         pas défini.
     */
    public Component getPane() {
        return pane;
    }

    private void initPane() {
        if (proglet != null && !"".equals(proglet)) {
            try {
                pane = (Component) Class.forName(proglet).newInstance();
                doVerbose("The component " + pane
                        + " has been constructed from the class-name "
                        + proglet);
            } catch (Throwable e) {
                pane = new JLabel("La proglet «" + proglet
                        + "» n'est pas définie", SwingConstants.CENTER);
                doVerbose("Unable to construct a component from the class-name "
                        + proglet);
            }
        }
    }

    private Component pane = null;

    /**
     * Renvoie le runnable exécuté dans la proglet courante.
     *
     * @return L'objet Java contenant une méthode run() à exécuter dans la
     *         proglet courante.
     */
    public Object getRunnable() {
        return runnable;
    }

    private void initRunnable() {
        if (program != null) {
            try {
                if (new File(program).exists()) {
                    File fRun = new File(program).getAbsoluteFile();
                    // Mise en place à partir du chargement d'un fichier .class
                    Class<?> jClass = new JVSClassLoader(fRun.getParentFile())
                            .loadClass(fRun.getName().replaceAll("\\.class$",
                                    ""));
                    runnable = jClass.newInstance();
                    doVerbose("The runnable " + runnable
                            + " has been constructed from the class-name "
                            + jClass + "\n\tloaded from the file " + fRun
                            + " of the program " + program);
                } else {
                    // Mise en place à partir d'un nom de classe déjà chargé
                    runnable = Class.forName(program).newInstance();
                    doVerbose("The runnable " + runnable
                            + " has been constructed from the class-name "
                            + program);
                }
                if (!Invoke.run(runnable, "run", false)) {
                    runnable = null;
                    doVerbose("But the runnable has no run() method !");
                }
            } catch (Throwable e) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Pas de programme à lancer ici.");
                    }
                };
                doVerbose("Unable to construct a runnable from the class-name "
                        + program);
            }
        }
    }

    private Object runnable = null;

    /**
     * Renvoie la console utilisée.
     *
     * @return La console utilisée si elle a été définie.
     */
    public Console getConsole() {
        return console;
    }

    private Console console = null;

    @Override
    public void init() {
        initParameters();
        initPane();
        initRunnable();
        if (pane != null) {
            Invoke.run(pane, "init");
        }
        initContentPane();
    }

    @Override
    public void destroy() {
        if (pane != null) {
            Invoke.run(pane, "destroy");
        }
    }

    @Override
    public void start() {
        setFocusable(true);
        requestFocus();
        if (pane != null) {
            Invoke.run(pane, "start");
        }
        if (notoolbar) {
            run();
        }
    }

    @Override
    public void stop() {
        if (startstop != null) {
            startstop.doStop();
        }
        if (pane != null) {
            Invoke.run(pane, "stop");
        }
    }

    @Override
    public void run() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                if (runnable != null)
                    ((Runnable) runnable).run();
                return null;
            }
        });
    }

    /**
     * Rend visible le panneau graphique de la proglet.
     */
    public void focusOnProgletPanel() {
        if (pane != null) {
            cl.show(cd, "panel");
            bt.setText("console");
        }
    }

    /**
     * Rend visible le panneau graphique de la console.
     */
    public void focusOnConsolePanel() {
        if (pane != null) {
            cl.show(cd, "console");
            bt.setText("panel");
        }
    }

    /**
     * Renvoie l'instance actuelle du ProgletPanel ou null si indéfini.
     */
    public static ProgletApplet getInstance() {
        return ProgletApplet.instance;
    }

    {
        ProgletApplet.instance = this;
    }

    private static ProgletApplet instance = null;

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Implémentation d'une frame
    //

    /**
     * Construit et ouvre une fenêtre principale pour lancer une application.
     *
     * @param title  Le titre de la fenêtre, sinon le nom de la classe du composant
     *               graohique.
     * @param icon   L'icône de la fenêtre (optionel).
     * @param width  Largeur de la fenêtre. Si 0 on prend tout l'écran.
     * @param height Hauteur de la fenêtre. Si 0 on prend tout l'écran.
     * @param pane   Le composant graphique à afficher. Si ce composant est une
     *               Applet ces méthodes init/start/stop/destroy sont gérées ici.
     * @return Renvoie la fenêtre graohique crée.
     */
    public static JFrame open(String title, String icon, int width, int height,
                              Component pane) {
        return new MainFrame(title, icon, width, height, pane);
    }

    /**
     * @see #open(String, String, int, int, java.awt.Component)
     */
    public static JFrame open(String title, int width, int height,
                              Component pane) {
        return ProgletApplet.open(title, null, width, height, pane);
    }

    /**
     * @see #open(String, String, int, int, java.awt.Component)
     */
    public static JFrame open(String title, String icon, Component pane) {
        return ProgletApplet.open(title, icon, 0, 0, pane);
    }

    /**
     * @see #open(String, String, int, int, java.awt.Component)
     */
    public static JFrame open(String title, Component pane) {
        return ProgletApplet.open(title, null, 0, 0, pane);
    }

    /**
     * @see #open(String, String, int, int, java.awt.Component)
     */
    public static JFrame open(int width, int height, Component pane) {
        return ProgletApplet.open(pane.getClass().toString(), null, width,
                height, pane);
    }

    /**
     * @see #open(String, String, int, int, java.awt.Component)
     */
    public static JFrame open(Component pane) {
        return ProgletApplet.open(pane.getClass().toString(), null, 0, 0, pane);
    }

    // Définition d'une frame d'affichage d'un composant graphique

    private static class MainFrame extends JFrame {
        // @todo Quid de remettre le joli look de jvs4 que il est un peu lourd
        // mais sympa ?
        private static final long serialVersionUID = 1L;
        private Component pane;

        public MainFrame(String title, String icon, int width, int height,
                         Component pane) {
            if (title != null) {
                setTitle(title);
            }
            if (System.getProperty("os.name").toUpperCase().contains("MAC")) {
                try {
                    System.setProperty(
                            "com.apple.mrj.application.apple.menu.about.name",
                            title);
                } catch (Exception e) {
                }
            }
            if (icon != null) {
                ImageIcon image = Macros.getIcon(icon);
                if (image != null) {
                    setIconImage(image.getImage());
                }
            }
            add(this.pane = pane);
            if (pane instanceof Applet) {
                ((Applet) pane).init();
            }
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    close();
                }
            });
            pack();
            if ((width > 0) && (height > 0)) {
                setSize(width, height);
            } else {
                setExtendedState(Frame.MAXIMIZED_BOTH);
            }
            setVisible(true);
            if (pane instanceof Applet) {
                ((Applet) pane).start();
            }
            ProgletApplet.frameCount++;
        }

        // Ferme la fenêtre principale
        private void close() {
            if (MainFrame.this.pane instanceof Applet) {
                try {
                    ((Applet) MainFrame.this.pane).stop();
                } catch (Throwable e) {
                }
                // Ici la fermeture est interrompue si destroy lève une
                // exception
                try {
                    ((Applet) MainFrame.this.pane).destroy();
                } catch (Throwable e) {
                    return;
                }
                setVisible(false);
                dispose();
                ProgletApplet.frameCount--;
                if (ProgletApplet.frameCount == 0) {
                    System.exit(0);
                }
            }
        }
    }

    private static int frameCount = 0;

    // Définit le look and feel de l'application.
    // note: le thème nimbus sature les CPU's de certaines cartes graphiques
    static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err
                    .println("Note: Utilisaton du thème Java (et non du système)");
        }
    }

    {
        ProgletApplet.setSystemLookAndFeel();
    }
}
