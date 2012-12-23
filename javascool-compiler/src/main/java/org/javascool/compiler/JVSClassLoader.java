/*
 * Java's Cool, IDE for French Computer Sciences Students
 * Copyright (C) 2012  Philippe VIENNE, INRIA
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

package org.javascool.compiler;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chargeur de Classe pour Java's Cool. Il permet de charger une classe qui n'&eacute;tait pas dans le classpath
 * d'origine.
 *
 * @author Philippe VIENNE (PhilippeGeek@gmail.com)
 * @version 5.0
 */
public class JVSClassLoader extends ClassLoader {
    /**
     * Le lieu où travail le ClassLoader
     */
    private File location = null;
    /**
     * Petit cache pour les classes déjà chargés.
     */
    private final Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();

    /**
     * Construit un chargeur de classe. Il prend comme ClassLoader parent son propre ClassLoader. Il vérifie aussi
     * l'existance du dossier où serais les classes à charger.
     *
     * @param location Le dossier où sont stocké les .class
     */
    public JVSClassLoader(File location) {
        super(JVSClassLoader.class.getClassLoader());
        this.location = location;
        if (location.isFile())
            throw new IllegalArgumentException("Le chargeur de classe ne peut pas fonctionner dans un fichier," +
                    " il faut un dossier");
        if (!location.exists())
            throw new IllegalArgumentException("Le dossier " + location + " n'existe pas !");
    }

    /**
     * Charge une classe. Cette fonction prend soit la classe dans le cache local soit va tenter de la charger avec
     * {@link #findClass(String)}.
     *
     * @param className Le nom de la classe
     * @return L'objet représentant une classe Java
     * @throws ClassNotFoundException Voir {@link #findClass(String)}
     * @see #findClass(String)
     */
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (classes.get(className) != null) // Si on a déjà chargé cette classe, alors elle est dans le cache
            return classes.get(className);
        else // Sinon on tente de la charger
            return findClass(className);
    }

    /**
     * Cherche une classe dans le classpath actuel et dans le répertoire fournit au classloader
     *
     * @param className La classe à charger
     * @return L'objet représentant une classe en Java
     * @throws ClassNotFoundException Dans le cas où aucune classe n'as pu être trouvé.
     */
    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        byte classByte[];
        Class<?> result;

        result = classes.get(className); // On vérifie qu'elle n'est pas dans le cache
        if (result != null) {
            return result;
        }

        try { // On cherche si c'est une classe système
            return findSystemClass(className);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.OFF, className + " n'est pas une classe système");
        }

        try { // Ou qu'elle est dans le chargeur parent
            return JVSClassLoader.class.getClassLoader().loadClass(className);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.OFF, className + " n'est pas une classe du chargeur parent");
        }

        try { // On regarde après si elle n'est pas chez nous.
            // On en crée le pointeur vers le fichier de la classe
            File clazz = FileUtils.getFile(location, decomposePathForAClass(className));
            if (!clazz.exists()) { // Si le fichier de la classe n'existe pas, alors on crée une erreur
                throw new FileNotFoundException("Le fichier : " + clazz.toString() + " n'existe pas");
            }
            // On lit le fichier
            classByte = FileUtils.readFileToByteArray(clazz);
            // On définit la classe
            result = defineClass(className, classByte, 0, classByte.length, null);
            // On la stocke en cache
            classes.put(className, result);
            // On retourne le résultat
            return result;
        } catch (Exception e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        }
        //
    }

    /**
     * Décompose un nom de classe vers le chemin de la classe. Permet de passer de "org.javascool.compiler.Main" à
     * ["org","javascool","compiler","Main.class"] qui une représentation du chemin de la classe sous la forme d'un
     * tableau de chaines.
     *
     * @param className Le nom de la classe au format Java
     * @return Le tableau représentant le chemin.
     */
    private static String[] decomposePathForAClass(String className) {
        ArrayList<String> path = new ArrayList<String>();
        for (String s : className.split("\\.")) {
            if (className.endsWith(s))
                path.add(s + ".class");
            else
                path.add(s);
        }
        return path.toArray(new String[path.size()]);
    }
}
