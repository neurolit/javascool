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

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

/**
 * Java PolyFileWriter Applet Canvas.
 * This class is a kind of Applet designed to talk with JavaScript. So this
 * applet implement security manager (We are on web so secure all) and an JSGate
 * to send event to JS.
 *
 * @author Philippe Vienne
 */
public class JavaGate extends JApplet {
    private static final long serialVersionUID = 1L;

    protected JApplet thisApplet = this;

    /**
     * Execute an Runnable in a new thread.
     *
     * @param run The runnable to put in the thread
     */
    protected void runInNewThread(Runnable run) {
        Thread thread = new Thread(run);
        thread.start();
    }

    /**
     * Execute an Runnable in a new thread with All rights on system like full
     * file access (beware jar has to be signed).
     *
     * @param run The runnable to put in the all rights thread
     */
    protected void runInNewThreadWithAllRights(final Runnable run) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AccessController.doPrivileged(new PrivilegedAction<Integer>() {
                    @Override
                    public Integer run() {
                        run.run();
                        return 0;
                    }
                });
            }
        });
        thread.start();
    }

    /**
     * Pop the last Java exception which happened
     *
     * @return The Exception
     */
    public Exception popException() {
        if (lastError != null) {
            Exception e = lastError;
            lastError = null;
            return e;
        }
        return null;
    }

    private Exception lastError;

    /**
     * Add a new error in Applet.
     *
     * @param e The Exception to give to JavaScript
     */
    protected void popException(Exception e) {
        lastError = e;
    }

    /**
     * Create a File object from the Path.
     *
     * @param path
     * @return le fichier créé.
     */
    protected File getFile(final String path) {
        return AccessController.doPrivileged(new PrivilegedAction<File>() {
            @Override
            public File run() {
                return new File(path);
            }
        });
    }

    /**
     * Create a temporary File object with a content
     *
     * @param content The data to write into
     * @param suffix  The extension ( '.java' e.g.)
     * @param prefix  The prefix for the file
     * @return The path of file
     */
    protected String getTmpFile(final String content, final String suffix,
                                final String prefix) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                try {
                    // Create temp file.
                    File temp = File.createTempFile("javaTempFile", ".java");

                    // Delete temp file when program exits.
                    temp.deleteOnExit();

                    if (content != null) {
                        // Write to temp file
                        BufferedWriter out = new BufferedWriter(new FileWriter(
                                temp));
                        out.write(content);
                        out.close();
                    }

                    return temp.toString();
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Can't create a tempory file", e);
                }
            }
        });
    }

    /**
     * @see java.applet.Applet#init()
     */
    @Override
    public void init() {
        checkAppletSecurity();
    }

    /**
     * Security flag.
     * If the applet have to be locked for security reasons, put this variable
     * to true.
     */
    protected boolean appletLocked = true;

    /**
     * Vérification de l'environement.
     * La fonction vérifie que l'applet est invoqué soit depuis le système local
     * de fichiers (file://)
     * ou alors depuis l'un des site web autorisé dans la liste
     * {authorizedHosts}.
     *
     * @see #authorizedHosts
     */
    private final void checkAppletSecurity() {
        ArrayList<String> hosts = new ArrayList<String>();
        for (String h : authorizedHosts) {
            hosts.add(h);
        }
        if ((getDocumentBase().getProtocol().equals("file"))
                || (hosts.contains(getDocumentBase().getHost()) && hosts
                .contains(getCodeBase().getHost()))) {
            appletLocked = false;
        } else {
            appletLocked = true;
        }
    }

    /**
     * Liste des sites web autorisés à executer l'applet.
     */
    protected String[] authorizedHosts = {"javascool.github.com",
            "javascool.fr", "www.javascool.fr", "javascool.gforge.inria.fr"};

    /**
     * Message non-spam flag.
     * This flag is used to be sure that we show the security message only one
     * time.
     */
    protected boolean showMessage = true;

    /**
     * S'assure que l'environement est propice à des actions modifiant le
     * système de l'utilisateur.
     * Si l'applet est bloqué à cause d'un environement non prévue (voir la
     * fonction checkAppletSecurity()), elle
     * affiche un message à l'utilisateur et déclanche une exception.
     *
     * @throws SecurityException Dans le cas où appletLocked est à true
     * @see #checkAppletSecurity()
     * @see #authorizedHosts
     * @see #appletLocked
     */
    protected final void assertSafeUsage() {
        checkAppletSecurity();
        if (appletLocked) {
            if (showMessage) {
                JOptionPane
                        .showMessageDialog(
                                this,
                                "This website ("
                                        + getCodeBase().getHost()
                                        + ") tried to hack"
                                        + " your computer by accessing to the local file system (Attack stopped)",
                                "Error", JOptionPane.ERROR_MESSAGE);
                showMessage = false;
            }
            SecurityException e = new SecurityException(
                    "This website is not authorized to use this applet");
            popException(e);
            throw e;
        } else {
        }
    }

}
