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
import org.apache.commons.io.IOUtils;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de compilation pour un code ayant une relation avec une Proglet. Gère d'un bout à l'autre la compilation
 * et l'execution d'un code JVS dépendant d'une proglet. Si aucune proglet n'est communiqué alors on peut considérer que
 * c'est un Java pur à compiler.
 * <p/>
 * La proglet JVS par défaut est "ingredients" est en fait un code JVS qui est retranscrit en Java sans ajout de
 * fonctions.
 * 
 * @eprecated Vous devez utiliser le compilateur du package org.javascool.manager.proglets. Celui ci n'est plus maintenu.
 *
 * @author Philippe VIENNE (PhilippeGeek@gmail.com)
 * @since 5.0
 */
public class ProgletCodeCompiler {

    /**
     * Le répertoire pour les compilations temporaires
     */
    private static final File tmpDirectory = new File(FileUtils.getTempDirectory(), "javascool-compiler");

    static {
        tmpDirectory.mkdirs();
    }

    /**
     * L'identificateur de la proglet native de la version compilé.
     */
    public static final String DEFAULT_PROGLET = "ingrediants";
    /**
     * L'identificateur de la proglet utilisé.
     */
    private String proglet;
    /**
     * Le translator de la proget. Si la proglet n'en déclare pas, alors on prend l'officiel {@link
     * DefaultJVSTranslator}
     */
    private JVSTranslator translator;
    /**
     * Le compilateur utilisé.
     */
    private Compiler compiler;
    /**
     * Le fichier à compiler.
     */
    private File jvsFile;


    /**
     * Crée un compilateur pour une proglet désigné
     *
     * @param progletName Le nom de code de la proglet
     */
    protected ProgletCodeCompiler(String progletName) {
        if (isDefaultProglet(progletName)) return; // On ne charge rien si c'est la proglet native
        assertProgletExists(progletName);
        proglet = progletName;
    }

    /**
     * Créer un compilateur pour une proglet avec un code JVS passé en argument. Ce constructeur permet de faire de la
     * compilation à la volé sans être obligé de passé par un Fichier. Les resources du code seront alors considéré
     * comme étant dans le répertoire racine de l'utilisateur.
     *
     * @param progletName Le nom de code de la proglet
     * @param code        Le code JVS à compiler
     */
    public ProgletCodeCompiler(String progletName, String code) {
        this(progletName);
        try {
            File tmpFolder = new File(tmpDirectory, code.hashCode() + "-compile");
            tmpFolder.mkdirs();
            jvsFile = File.createTempFile("JVSCompileSource", ".jvs", tmpFolder);
            FileUtils.writeStringToFile(jvsFile, code);
        } catch (IOException e) {
            throw new IllegalStateException("On ne peut pas créer le fichier temporaire dans "
                    + new File(tmpDirectory, code.hashCode() + "-compile"));
        }
    }

    /**
     * Crée un compilateur pour une proglet avec un Fichier JVS passé en argument. Ce constructeur va permêtre de
     * controller une compilation au sein d'un répertoire donnée qui sera par la suite le répertoire de resource du
     * programme.
     *
     * @param progletName Le nom de code de la proglet
     * @param jvsFile     Le fichier JVS à compiler
     */
    public ProgletCodeCompiler(String progletName, File jvsFile) {
        this(progletName);
        assertFileExists(jvsFile);
        this.jvsFile = jvsFile.getAbsoluteFile();
    }

    /**
     * Permet de déterminer si la proglet est celle native ou pas. <p>La proglet Native est une proglet implémentant
     * uniquement les specs par défaut. C'est la proglet "ABCDAlgo" qui est aussi appelé "ingrediants". Cette proglet
     * native n'a aucun Translator, Functions et autres artifacts définit.</p> <p/> <p>A contrario, les autres proglets
     * peuvent avoir des artifacts définits selon les specifications de Java's Cool. Pour les connaîtres, il faut aller
     * sur : <a href="http://javascool.github.com/doc/developper/specification.html">Le site officiel</a></p>
     *
     * @param progletName Le nom de la proglet à tester
     * @return vrai si c'est la proglet native
     */
    private static boolean isDefaultProglet(String progletName) {
        return progletName == null || progletName.equals(DEFAULT_PROGLET);
    }

    /**
     * Vérifie qu'un fichier existe bel et bien et que c'est un fichier
     *
     * @param file Le fichier à vérifier
     */
    private static void assertFileExists(File file) {
        try {
            if (file == null) throw new Exception();
            if (!file.exists()) throw new Exception();
            if (file.isDirectory()) throw new Exception();
        } catch (Exception e) {
            throw new IllegalArgumentException("Le fichier " + file + " n'existe pas");
        }
    }

    /**
     * Vérifie si une proglet est présente dans le Classpath actuelle
     *
     * @param progletName Le nom de la proglet à valider
     */
    private static void assertProgletExists(String progletName) {
        try {
            IOUtils.toString(ProgletCodeCompiler.class.getClassLoader().getResourceAsStream("org/javascool/proglets/"
                    + progletName + "/proglet.json"));
        } catch (Exception e) {
            throw new IllegalArgumentException("La proglet " + progletName + " n'existe pas dans le Classpath actuel.");
        }
    }

    /**
     * Lance la compilation du code.
     *
     * @return Les erreurs de compilations provenant de Java
     */
    public ArrayList<Diagnostic<? extends JavaFileObject>> compile() {
        translator = getTranslatorForProglet(proglet);
        setUpTranslator();


        translator.getJavaCode(); // On lance un premier Parse

        // On établie là où sera le fichier compilé
        String[] path = translator.getFullClassname().split("\\.");
        path[path.length - 1] = path[path.length - 1] + ".class";
        File classFile = FileUtils.getFile(jvsFile.getParentFile(), path);
        classFile.getParentFile().mkdirs();

        // On en fait de même pour le fichier java
        path = translator.getFullClassname().split("\\.");
        path[path.length - 1] = path[path.length - 1] + ".java";
        File javaFile = FileUtils.getFile(jvsFile.getParentFile(), path);
        javaFile.getParentFile().mkdirs();

        // On traduit le JVS vers du Java
        try {
            FileUtils.writeStringToFile(javaFile, translator.getJavaCode());
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'écrire le fichier Java", e);
        }

        compiler = new Compiler(jvsFile.getParentFile(), translator.getFullClassname());

        // On compile le Java et on retourne le résultat
        return compiler.compile();
    }

    /**
     * Configure le translator pour lui ajouter les Imports necessaires
     */
    private void setUpTranslator() {
        translator.addDefaultImports(); // On vérifie bien qu'on est dans un monde Java's Cool (Imports, Runnable ...)
        try {
            ProgletCodeCompiler.class.getClassLoader().loadClass("org.javascool.proglets." + this.proglet + ".Functions");
            translator.addImport("org.javascool.proglets." + this.proglet + ".Functions.*", true);
        } catch (ClassNotFoundException e) {
            Logger.getAnonymousLogger().log(Level.INFO, "Aucun fonction à ajouter avec la proglet " + this.proglet);
        } // Dans ce cas on ne fais rien car il n'y a pas de Functions.java
    }

    /**
     * Permet d'obtenir un instance du Runnable compilé.
     *
     * @return Une instance du runnable
     * @throws ClassNotFoundException Bien souvent, cela veut dire que la classe n'as pas pu être compilé
     * @throws IllegalStateException  Dans le cas où une erreur impromptu survient.
     */
    public Runnable getCompiledRunnable() throws ClassNotFoundException, IllegalStateException {
        Class<?> compiledClass = compiler.getClassLoader().loadClass(translator.getFullClassname());
        try {
            return (Runnable) compiledClass.newInstance();
        } catch (ClassCastException e) {
            throw new IllegalStateException("La classe compilé n'est pas un Runnable", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("La classe compilé n'est pas accessible", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("La classe compilé ne peut pas être créé comme objet", e);
        }
    }

    /**
     * Cherche si la proglet définit un Translator. Permet d'acceder aux translators des package du Classpath.
     *
     * @param progletName L'identificateur de la proglet
     * @return Le Translator à utiliser pour cette proglet
     */
    protected JVSTranslator getTranslatorForProglet(String progletName) {
        if (!isDefaultProglet(progletName)) {
            try {
                Class<?> translatorClass = ProgletCodeCompiler.class.getClassLoader()
                        .loadClass("org.javascool.proglets." + this.proglet + ".Translator");
                translator = (JVSTranslator) translatorClass.getDeclaredConstructor(File.class).newInstance(jvsFile);
            } catch (ClassNotFoundException e) {
                Logger.getAnonymousLogger().log(Level.INFO, "Aucun Translator pour " + progletName);
            } // Dans ce cas on ne fais rien car il n'y a pas de Functions.java
            catch (Exception e) {
                throw new IllegalStateException("Impossible de créer un translator.");
            }
        }
        try {
            return new DefaultJVSTranslator(jvsFile);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer un translator sur le fichier à ouvrir");
        }
    }

}
