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

import org.javascool.tools.FileManager;

import java.io.*;
import java.util.jar.*;

/**
 * Met à disposition des fonctions de gestion de jar et répertoires de
 * déploiement.
 */
public class JarManager {
    // @factory
    private JarManager() {
    }

    /**
     * Extrait une arborescence d'un jar.
     *
     * @param jarFile  Jarre dont on extrait les fichiers.
     * @param destDir  Dossier où on déploie les fichiers.
     * @param jarEntry Racine des sous-dossiers à extraire. Si null extrait tout les
     *                 fichiers.
     */
    public static void jarExtract(String jarFile, String destDir,
                                  String jarEntry) {
        try {
            JarInputStream jip = new JarInputStream(
                    new FileInputStream(jarFile));
            JarEntry je;
            while ((je = jip.getNextJarEntry()) != null) {
                if ((jarEntry.isEmpty() || je.getName().startsWith(
                        jarEntry))
                        && !je.isDirectory()
                        && !je.getName().contains("META-INF")) {
                    File dest = new File(destDir + File.separator
                            + je.getName());
                    dest.getParentFile().mkdirs();
                    JarManager.copyStream(jip, new FileOutputStream(dest));
                }
            }
            jip.close();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * @see #jarExtract(String, String, String)
     */
    public static void jarExtract(String jarFile, String destDir) {
        JarManager.jarExtract(jarFile, destDir, "");
    }

    /**
     * Crée un jar à partir d'une arborescence.
     *
     * @param jarFile    Jar à construire. Elle est détruite avant d'être crée.
     * @param manifest   Fichier de manifeste (obligatoire).
     * @param srcDir     Dossier source avec les fichiers à mettre en jarre.
     * @param jarEntries Racine des sous-dossiers à extraire. Si null extrait tout les
     *                   fichiers.
     */
    public static void jarCreate(String jarFile, Manifest manifest,
                                 String srcDir, String[] jarEntries) {
        try {
            File parent = new File(jarFile).getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            new File(jarFile).delete();
            srcDir = new File(srcDir).getCanonicalPath();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
                    "1.0");
            JarOutputStream target = new JarOutputStream(new FileOutputStream(
                    jarFile), manifest);
            JarManager.copyFileToJar(new File(srcDir), target,
                    new File(srcDir), jarEntries);
            target.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * @see #jarCreate(String, java.util.jar.Manifest, String, String[])
     */
    public static void jarCreate(String jarFile, Manifest manifest,
                                 String srcDir) {
        JarManager.jarCreate(jarFile, manifest, srcDir, null);
    }

    /**
     * Copie un répertoire/fichier dans un autre en oubliant les .svn.
     *
     * @param srcDir  Dossier source.
     * @param dstDir  Dossier cible.
     * @param recurse Si true (valeur par défaut) copie les sous-répertoires.
     */
    public static void copyFiles(String srcDir, String dstDir, boolean recurse)
            throws IOException {
        if (new File(srcDir).isDirectory()) {
            if (!new File(srcDir).getName().equals(".svn")) {
                for (String s : FileManager.list(srcDir)) {
                    String d = dstDir + File.separator
                            + new File(s).getAbsoluteFile().getName();
                    if (recurse) {
                        JarManager.copyFiles(s, d, true);
                    } else if (!new File(s).isDirectory()) {
                        JarManager.copyFile(s, d);
                    }
                }
            }
        } else {
            JarManager.copyFile(srcDir, dstDir);
        }
    }

    /**
     * @see #copyFiles(String, String, boolean)
     */
    public static void copyFiles(String srcDir, String dstDir)
            throws IOException {
        JarManager.copyFiles(srcDir, dstDir, true);
    }

    /**
     * Copie un fichier vers un destination.
     *
     * @param srcFile Le fichier à copier
     * @param dstDir  Le fichier de destination, s'il n'existe pas, il sera créé
     * @throws IOException
     */
    public static void copyFile(String srcFile, String dstDir)
            throws IOException {
        new File(dstDir).getParentFile().mkdirs();
        JarManager.copyStream(new FileInputStream(srcFile),
                new FileOutputStream(dstDir));
    }

    /**
     * Télécharge un fichier dans un répertoire local.
     *
     * @param location Une URL (Universal Resource Location) de la forme: <div
     *                 id="load-format">
     *                 <table align="center">
     *                 <tr>
     *                 <td><tt>http:/<i>path-name</i></tt></td>
     *                 <td>pour aller chercher le contenu sur un site web</td>
     *                 </tr>
     *                 <tr>
     *                 <td><tt>http:/<i>path-name</i>?param_i=value_i&amp;..</tt></td>
     *                 <td>pour le récupérer sous forme de requête HTTP</td>
     *                 </tr>
     *                 <tr>
     *                 <td><tt>file:/<i>path-name</i></tt></td>
     *                 <td>pour le charger du système de fichier local ou en tant que
     *                 ressource Java dans le CLASSPATH</td>
     *                 </tr>
     *                 <tr>
     *                 <td><tt>jar:/<i>jar-path-name</i>!/<i>jar-entry</i></tt></td>
     *                 <td>pour le charger d'une archive <div>(exemple:
     *                 <tt>jar:http://javascool.gforge.inria.fr/javascool.jar!/META-INF/MANIFEST.MF</tt>
     *                 )</div></td>
     *                 </tr>
     *                 </table>
     *                 </div>
     * @param target   Nom du fichier cible local. Par défaut un fichier temporaire.
     * @param listener Ecouteur du download. Par défaut null.
     * @return Le nom du fichier cible local.
     */
    public static String downloadFile(String location, String target,
                                      DownloadListener listener) throws IOException {
        JarManager.copyStream(
                FileManager.getResourceURL(location).openStream(),
                new FileOutputStream(target), listener,
                (int) FileManager.getSize(location));
        return target;
    }

    /**
     * @see #downloadFile(String, String, DownloadListener)
     */
    public static String downloadFile(String location, String target)
            throws IOException {
        return JarManager.downloadFile(location, target, null);
    }

    /**
     * @see #downloadFile(String, String, DownloadListener)
     */
    public static String downloadFile(String location, DownloadListener listener)
            throws IOException {
        return JarManager.downloadFile(location,
                File.createTempFile("jvs", null).getAbsolutePath(), listener);
    }

    /**
     * @see #downloadFile(String, String, DownloadListener)
     */
    public static String downloadFile(String location) throws IOException {
        return JarManager.downloadFile(location,
                File.createTempFile("jvs", null).getAbsolutePath(), null);
    }

    /**
     * Ecouteur de la progession d'un téléchargement de fichier.
     */
    public static interface DownloadListener {
        /**
         * Cette routine est appellée à chaque chargement de paquet.
         */
        public void progressPerformed(int currentSize, int totalSize);
    }

    // Ajoute un stream a un jar
    private static void copyFileToJar(File source, JarOutputStream target,
                                      File root, String[] jarEntries) throws IOException {
        // Teste si la source est dans les fichier à extraire
        if (jarEntries != null) {
            boolean skip = true;
            for (String jarEntry : jarEntries) {
                String entry = root.toString() + File.separator + jarEntry;
                skip &= !(entry.startsWith(source.toString()) | source
                        .toString().startsWith(entry));
            }
            if (skip) {
                return;
            }
        }
        try {
            if (source.isDirectory()) {
                String name = source.getPath()
                        .replace(root.getAbsolutePath() + File.separator, "")
                        .replace(File.separator, "/");
                if (!name.isEmpty() && (!source.equals(root))) {
                    if (!name.endsWith("/")) {
                        name += "/";
                    }
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles()) {
                    JarManager.copyFileToJar(nestedFile, target, root,
                            jarEntries);
                }
            } else {
                JarEntry entry = new JarEntry(source.getPath()
                        .replace(root.getAbsolutePath() + File.separator, "")
                        .replace(File.separator, "/"));
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                JarManager.copyStream(new BufferedInputStream(
                        new FileInputStream(source)), target);
            }
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            throw new IllegalStateException(e);
        }
    }

    // Copy un stream dans un autre
    private static void copyStream(InputStream in, OutputStream out,
                                   DownloadListener listener, int size) throws IOException {
        InputStream i = in instanceof JarInputStream ? in
                : new BufferedInputStream(in, 2048);
        OutputStream o = out instanceof JarOutputStream ? out
                : new BufferedOutputStream(out, 2048);
        byte data[] = new byte[2048];
        for (int c, l = 0; (c = i.read(data, 0, 2048)) != -1; ) {
            o.write(data, 0, c);
            if (listener != null) {
                listener.progressPerformed(l += c, size);
            }
        }
        if (o instanceof JarOutputStream) {
            ((JarOutputStream) o).closeEntry();
        } else {
            o.close();
        }
        if (i instanceof JarInputStream) {
            ((JarInputStream) i).closeEntry();
        } else {
            i.close();
        }
    }

    private static void copyStream(InputStream in, OutputStream out)
            throws IOException {
        JarManager.copyStream(in, out, null, 0);
    }

    /**
     * Détruit récursivement un fichier ou répertoire.
     * <p>Irréversible: à utiliser avec la plus grande prudence.</p>
     */
    public static void rmDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                JarManager.rmDir(f);
            }
        }
        dir.delete();
    }

    /**
     * Copie un fichier présent en resource vers une destination donné
     *
     * @param resource    La resource à copier
     * @param destination L'endroit où il faut la copier (le fichier)
     * @throws IOException Dans le cas où la destination est introuvable où que la
     *                     resource n'existe pas.
     */
    public static void copyResource(String resource, String destination) throws IOException {
        copyStream(JarManager.class.getClassLoader().getResourceAsStream(resource),
                new FileOutputStream(new File(destination)));
    }

}
